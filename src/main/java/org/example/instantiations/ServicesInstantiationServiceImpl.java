package org.example.instantiations;

import org.example.configs.InstantiationConfiguration;
import org.example.container.ServiceDetails;
import org.example.exceptions.ServiceInstantiationException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ServicesInstantiationServiceImpl implements ServicesInstantiationService {
    private static final String MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED = "Maximum number of allowed iterations was reached '%s'.";
    private static final String COULD_NOT_FIND_CONSTRUCTOR_PARAM_MSG = "Could not create instance of '%s'. Parameter '%s' implementation was not found";
    private final InstantiationService instantiationService;
    private final InstantiationConfiguration instantiationConfiguration;
    private final List<ServiceDetails<?>> instantiatiedServices;
    private final List<Class<?>> allAvailableServices;
    private final LinkedList<EnqueuedServiceDetails> enqueuedServiceDetails;


    public ServicesInstantiationServiceImpl(InstantiationService instantiationService, InstantiationConfiguration instantiationConfiguration) {
        this.instantiationService = instantiationService;
        this.instantiationConfiguration = instantiationConfiguration;
        this.instantiatiedServices = new ArrayList<>();
        this.allAvailableServices = new ArrayList<>();
        enqueuedServiceDetails = new LinkedList<>();
    }

    @Override
    public List<ServiceDetails<?>> instantiateServicesAndBeans(Set<ServiceDetails<?>> mappedClasses) {
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
                ServiceDetails<?> serviceDetails = enqueuedServiceDetail.getServiceDetails();
                Object[] dependencyInstances = enqueuedServiceDetail.getDependencyInstances();
                this.createInstance(serviceDetails, dependencyInstances);
                this.registerInstantiatedService(serviceDetails);
                this.registerBeans(serviceDetails);

            } else {
                this.enqueuedServiceDetails.addLast(enqueuedServiceDetail);
                counter++;
            }
        }
        return this.instantiatiedServices;
    }

    private void registerBeans(ServiceDetails<?> serviceDetails) {
        for (Method bean : serviceDetails.getBeans()) {
            ServiceBeanDetails<?> serviceBeanDetails = new ServiceBeanDetails<>(
                    bean.getReturnType(),
                    bean,
                    serviceDetails);
            this.instantiationService.createBean(serviceBeanDetails);
            this.registerInstantiatedService(serviceBeanDetails);
        }
    }

    private void registerInstantiatedService(ServiceDetails<?> serviceDetails) {
        if (!(serviceDetails instanceof  ServiceBeanDetails)) {
            this.updatedDependantServices(serviceDetails);
        }
        this.instantiatiedServices.add(serviceDetails);
        this.findSuitableDependencyInstance(serviceDetails);
    }

    private void findSuitableDependencyInstance(ServiceDetails<?> serviceDetails) {
        for (EnqueuedServiceDetails enqueuedServiceDetail : this.enqueuedServiceDetails) {
            if (enqueuedServiceDetail.isDependencyRequired(serviceDetails.getServiceType())) {
                enqueuedServiceDetail.addDependencyInstance(serviceDetails.getInstance());
            }
        }
    }

    private void updatedDependantServices(ServiceDetails<?> selectedService) {
        for (Class<?> parameterType : selectedService.getTargetConstructor().getParameterTypes()) {
            for (ServiceDetails<?> serviceDetails : this.instantiatiedServices) {
                if (parameterType.isAssignableFrom(serviceDetails.getServiceType())) {
                    serviceDetails.addDependantService(selectedService);
                }
            }
        }
    }

    /**
     *
     * @param mappedServices
     * @throws ServiceInstantiationException
     */
    private void checkForMissingServices(Set<ServiceDetails<?>> mappedServices) throws ServiceInstantiationException {
        for (ServiceDetails<?> serviceDetails : mappedServices) {
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
     *
     * @param parameterType
     * @return
     */
    private boolean isAssignableTypePresent(Class<?> parameterType) {
        for (Class<?> serviceType : this.allAvailableServices) {
            if (parameterType.isAssignableFrom(serviceType)) {
                return true;
            }
        }

        return false;
    }

    private void createInstance(ServiceDetails<?> serviceDetails, Object ... dependencyInstances) {
        this.instantiationService.createInstance(serviceDetails, dependencyInstances);
    }

    private void init(Set<ServiceDetails<?>> mappedClass) {
        this.clear();
        this.getAllAvailableServices(mappedClass);
    }

    private void getAllEnqueuedServices(ServiceDetails<?> serviceDetail) {
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
    private void getAllAvailableServices(Set<ServiceDetails<?>> mappedClass) {
        for (ServiceDetails<?> serviceDetail : mappedClass) {
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
     * Use to clear all fields before processing
     */
    private void clear() {
        this.allAvailableServices.clear();
        this.instantiatiedServices.clear();
    }
}
