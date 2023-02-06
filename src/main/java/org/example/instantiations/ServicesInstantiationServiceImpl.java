package org.example.instantiations;

import org.example.configs.InstantiationConfiguration;
import org.example.container.ServiceDetails;
import org.example.exceptions.ServiceInstantiationException;
import org.example.util.ProxyUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link ServicesInstantiationService} implementation.
 * <p>
 * Responsible for creating the initial instances or all services and beans.
 */
public class ServicesInstantiationServiceImpl implements ServicesInstantiationService {
    private static final String MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED = "Maximum number of allowed iterations was reached '%s'.";
    private static final String COULD_NOT_FIND_CONSTRUCTOR_PARAM_MSG = "Could not create instance of '%s'. Parameter '%s' implementation was not found";
    private final InstantiationService instantiationService;
    /**
     * Configuration containing the maximum number or allowed iterations.
     */
    private final InstantiationConfiguration instantiationConfiguration;
    private final List<ServiceDetails> instantiatedServices;
    /**
     * Contains all available types that will be loaded from this service.
     * This includes all services and all beans.
     */
    private final List<Class<?>> allAvailableServices;
    /**
     * The storage for all services that are waiting to the instantiated.
     */
    private final LinkedList<EnqueuedServiceDetails> enqueuedServiceDetails;


    public ServicesInstantiationServiceImpl(InstantiationService instantiationService, InstantiationConfiguration instantiationConfiguration) {
        this.instantiationService = instantiationService;
        this.instantiationConfiguration = instantiationConfiguration;
        this.instantiatedServices = new ArrayList<>();
        this.allAvailableServices = new ArrayList<>();
        enqueuedServiceDetails = new LinkedList<>();
    }

    /**
     * Starts looping and for each cycle gets the first element in the enqueuedServiceDetails since they are ordered
     * by the number of constructor params in ASC order.
     * <p>
     * If the {@link EnqueuedServiceDetails} is resolved (all of its dependencies have instances or there are no params)
     * then the service is being instantiated, registered and its beans are also being registered.
     * Otherwise the element is added back to the enqueuedServiceDetails at the last position.
     *
     * @param mappedClasses provided services and their details.
     * @return list of all instantiated services and beans.
     * @throws ServiceInstantiationException if maximum number of iterations is reached
     *                                       One iteration is added only of a service has not been instantiated in this cycle.
     */
    @Override
    public List<ServiceDetails> instantiateServicesAndBeans(Set<ServiceDetails> mappedClasses) {
        this.init(mappedClasses);
        this.checkForMissingServices(mappedClasses);

        int maxNumberOfIterations = instantiationConfiguration.getMaximumAllowedIterations();
        int counter = 0;
        while (!this.enqueuedServiceDetails.isEmpty()) {
            if (counter > maxNumberOfIterations) {
                throw new ServiceInstantiationException(String.format(MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED, maxNumberOfIterations));
            }
            EnqueuedServiceDetails enqueuedServiceDetail = this.enqueuedServiceDetails.removeFirst();
            if (enqueuedServiceDetail.isResolved()) {
                ServiceDetails serviceDetails = enqueuedServiceDetail.getServiceDetails();
                Object[] dependencyInstances = enqueuedServiceDetail.getDependencyInstances();
                this.createInstance(serviceDetails, dependencyInstances);
                ProxyUtils.createProxyInstance(serviceDetails, dependencyInstances);
                this.registerInstantiatedService(serviceDetails);
                this.registerBeans(serviceDetails);

            } else {
                this.enqueuedServiceDetails.addLast(enqueuedServiceDetail);
                counter++;
            }
        }
        return this.instantiatedServices;
    }

    /**
     * Iterates all bean methods for the given service.
     * Creates {@link ServiceBeanDetails} and then creates instance of the bean.
     * Finally, calls registerInstantiatedService so that enqueued services are aware of the
     * newly created bean.
     *
     * @param serviceDetails given service.
     */
    private void registerBeans(ServiceDetails serviceDetails) {
        for (Method bean : serviceDetails.getBeans()) {
            ServiceBeanDetails serviceBeanDetails = new ServiceBeanDetails(
                    bean.getReturnType(),
                    bean,
                    serviceDetails);
            this.instantiationService.createBean(serviceBeanDetails);
            this.registerInstantiatedService(serviceBeanDetails);
        }
    }

    /**
     * Adds the newly created service to the list of instantiated services instantiatedServices.
     * Iterated all enqueued services and if one of them relies on that services, adds its instance
     * to them so they can get resolved.
     *
     * @param serviceDetails - the created service.
     */
    private void registerInstantiatedService(ServiceDetails serviceDetails) {
        if (!(serviceDetails instanceof  ServiceBeanDetails)) {
            this.updatedDependantServices(serviceDetails);
        }
        this.instantiatedServices.add(serviceDetails);
        this.findSuitableDependencyInstance(serviceDetails);
    }

    private void findSuitableDependencyInstance(ServiceDetails serviceDetails) {
        for (EnqueuedServiceDetails enqueuedServiceDetail : this.enqueuedServiceDetails) {
            if (enqueuedServiceDetail.isDependencyRequired(serviceDetails.getServiceType())) {
                enqueuedServiceDetail.addDependencyInstance(serviceDetails.getProxyInstance());
            }
        }
    }
    /**
     * Gets all dependencies of the given new service.
     * <p>
     * For each dependency, in the form of {@link ServiceDetails}
     * adds itself to its dependant services list.
     *
     * @param selectedService - the newly created service.
     */
    private void updatedDependantServices(ServiceDetails selectedService) {
        for (Class<?> parameterType : selectedService.getTargetConstructor().getParameterTypes()) {
            for (ServiceDetails serviceDetails : this.instantiatedServices) {
                if (parameterType.isAssignableFrom(serviceDetails.getServiceType())) {
                    serviceDetails.addDependantService(selectedService);
                }
            }
        }
    }

    /**
     * Checks if the client has a service that will never be instantiated because
     * it has a dependency that is not present in the application context.
     *
     * @param mappedServices set of all mapped services.
     * @throws ServiceInstantiationException if a service has a dependency that is not
     *                                       present in the application context.
     */
    private void checkForMissingServices(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException {
        for (ServiceDetails serviceDetails : mappedServices) {
            Class<?>[] parameterTypesOfTargetConstructor = serviceDetails.getTargetConstructor().getParameterTypes();

            for (Class<?> parameterType : parameterTypesOfTargetConstructor) {
                if (!this.isAssignableTypePresent(parameterType)) {
                    throw new ServiceInstantiationException(
                            String.format(COULD_NOT_FIND_CONSTRUCTOR_PARAM_MSG,
                                    serviceDetails.getServiceType().getName(),
                                    parameterType.getName()
                            )
                    );
                }
            }
        }
    }

    /**
     * @param parameterType given type.
     * @return true if allAvailableClasses contains a type
     * that is compatible with the given type.
     */
    private boolean isAssignableTypePresent(Class<?> parameterType) {
        for (Class<?> serviceType : this.allAvailableServices) {
            if (parameterType.isAssignableFrom(serviceType)) {
                return true;
            }
        }

        return false;
    }

    private void createInstance(ServiceDetails serviceDetails, Object ... dependencyInstances) {
        this.instantiationService.createInstance(serviceDetails, dependencyInstances);
    }

    private void init(Set<ServiceDetails> mappedClass) {
        this.clear();
        this.getAllAvailableServices(mappedClass);
    }

    /**
     * Adds each service to the enqueuedServiceDetails.
     *
     * @param serviceDetail set of mapped services and their information.
     */
    private void getAllEnqueuedServices(ServiceDetails serviceDetail) {
        this.enqueuedServiceDetails.add(
                new EnqueuedServiceDetails(serviceDetail)
        );
    }

    /**
     * Get All ServiceDetails used to check whether a serviceDetails is correct or wrong
     * a serviceDetails correct when the type of constructor in serviceDetails inside the container
     * Container is all serviceDetails
     * @param mappedClass
     */
    private void getAllAvailableServices(Set<ServiceDetails> mappedClass) {
        for (ServiceDetails serviceDetail : mappedClass) {
            this.allAvailableServices.add(serviceDetail.getServiceType());
            this.allAvailableServices.addAll(
                    Arrays.stream(serviceDetail.getBeans())
                            .map(Method::getReturnType)
                            .collect(Collectors.toList())
            );
            this.getAllEnqueuedServices(serviceDetail);
        }
    }

    /**
     *
     */
    private void clear() {
        this.allAvailableServices.clear();
        this.instantiatedServices.clear();
    }
}
