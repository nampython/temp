package org.example.instantiations;

import org.example.annotations.Bean;
import org.example.annotations.Nullable;
import org.example.annotations.ScopeType;
import org.example.configs.InstantiationConfiguration;
import org.example.container.DependencyContainerInternal;
import org.example.container.DependencyContainerV2;
import org.example.container.DependencyResolveService;
import org.example.container.ServiceDetails;
import org.example.exceptions.ServiceInstantiationException;
import org.example.util.AliasFinder;
import org.example.util.ProxyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link ServicesInstantiationService} implementation.
 * <p>
 * Responsible for creating the initial instances or all services and beans.
 */
public class ServicesInstantiationServiceImpl implements ServicesInstantiationService {
    private static final String MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED = "Maximum number of allowed iterations was reached '%s'. Remaining services: \n %s";
    private static final String COULD_NOT_FIND_CONSTRUCTOR_PARAM_MSG = "Could not create instance of '%s'. Parameter '%s' implementation was not found";
    private static final String COULD_NOT_FIND_FIELD_PARAM_MSG = "Could not create instance of '%s'. Implementation was not found for Autowired field '%s'.";
    private final InstantiationService instantiationService;
    /**
     * Configuration containing the maximum number or allowed iterations.
     */
    private final InstantiationConfiguration instantiationConfiguration;
    private final DependencyContainerV2 tempContainer;
    private final DependencyResolveService dependencyResolveService;
    private final LinkedList<EnqueuedServiceDetails> enqueuedServiceDetails;

    public ServicesInstantiationServiceImpl(
            InstantiationService instantiationService,
            InstantiationConfiguration instantiationConfiguration,
            DependencyResolveService dependencyResolveService) {
        this.instantiationConfiguration = instantiationConfiguration;
        this.instantiationService = instantiationService;
        this.dependencyResolveService = dependencyResolveService;
        this.enqueuedServiceDetails = new LinkedList<>();
        this.tempContainer = new DependencyContainerInternal();
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
    public Collection<ServiceDetails> instantiateServicesAndBeans(Set<ServiceDetails> mappedClasses) {
        this.init(mappedClasses);
//        this.checkForMissingServices(mappedClasses);

        int maxNumberOfIterations = instantiationConfiguration.getMaximumAllowedIterations();
        int counter = 0;
        while (!this.enqueuedServiceDetails.isEmpty()) {
            if (counter > maxNumberOfIterations) {
                throw new ServiceInstantiationException(String.format(
                        MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED,
                        maxNumberOfIterations,
                        this.enqueuedServiceDetails)
                );
            }
            final EnqueuedServiceDetails enqueuedServiceDetails = this.enqueuedServiceDetails.removeFirst();

            if (this.dependencyResolveService.isServiceResolved(enqueuedServiceDetails)) {
                this.handleServiceResolved(enqueuedServiceDetails);
            } else {
                this.enqueuedServiceDetails.addLast(enqueuedServiceDetails);
                counter++;
            }
        }

        return this.tempContainer.getAllServices();
    }
    private void handleServiceResolved(EnqueuedServiceDetails enqueuedServiceDetails) {
        final ServiceDetails serviceDetails = enqueuedServiceDetails.getServiceDetails();
        final Object[] constructorInstances = enqueuedServiceDetails.getConstructorInstances();

        this.instantiationService.createInstance(
                serviceDetails,
                constructorInstances,
                enqueuedServiceDetails.getFieldInstances()
        );

        if (serviceDetails.getScopeType() == ScopeType.PROXY) {
            ProxyUtils.createProxyInstance(serviceDetails, enqueuedServiceDetails.getConstructorInstances());
        }

        this.registerResolvedDependencies(enqueuedServiceDetails);
        this.registerInstantiatedService(serviceDetails);
        this.registerBeans(serviceDetails);
    }
    /**
     * Iterates all bean methods for the given service.
     * Creates {@link ServiceBeanDetails} and then creates instance of the bean.
     * Finally, calls registerInstantiatedService so that enqueued services are aware of the
     * newly created bean.
     *
     * @param serviceDetails given service.
     */
    /**
     * Iterates all bean methods for the given service.
     * Creates {@link ServiceBeanDetails} and then creates instance of the bean.
     * Finally calls registerInstantiatedService so that enqueued services are aware of the
     * newly created bean.
     *
     * @param serviceDetails given service.
     */
    private void registerBeans(ServiceDetails serviceDetails) {
        for (ServiceBeanDetails beanDetails : serviceDetails.getBeans()) {
            this.instantiationService.createBean(beanDetails);
            if (beanDetails.getScopeType() == ScopeType.PROXY) {
                ProxyUtils.createBeanProxyInstance(beanDetails);
            }

            this.registerInstantiatedService(beanDetails);
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
        this.tempContainer.getAllServices().add(serviceDetails);

        for (EnqueuedServiceDetails enqueuedService : this.enqueuedServiceDetails) {
            this.addDependencyIfRequired(enqueuedService, serviceDetails);
        }
    }
    private void addDependencyIfRequired(EnqueuedServiceDetails enqueuedService, ServiceDetails newlyCreatedService) {
        if (this.dependencyResolveService.isDependencyRequired(enqueuedService, newlyCreatedService)) {
            this.dependencyResolveService.addDependency(
                    enqueuedService,
                    this.tempContainer.getServiceDetails(
                            newlyCreatedService.getServiceType(),
                            newlyCreatedService.getInstanceName()
                    )
            );

            this.addDependencyIfRequired(enqueuedService, newlyCreatedService);
        }
    }

    private void registerResolvedDependencies(EnqueuedServiceDetails enqueuedServiceDetails) {
        final ServiceDetails serviceDetails = enqueuedServiceDetails.getServiceDetails();

        serviceDetails.setResolvedConstructorParams(enqueuedServiceDetails.getConstructorParams());
        serviceDetails.setResolvedFields(enqueuedServiceDetails.getFieldDependencies());
    }



//    /**
//     * Checks if the client has a service that will never be instantiated because
//     * it has a dependency that is not present in the application context.
//     *
//     * @param mappedServices set of all mapped services.
//     * @throws ServiceInstantiationException if a service has a dependency that is not
//     *                                       present in the application context.
//     */
//    private void checkForMissingServices(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException {
//        for (ServiceDetails serviceDetails : mappedServices) {
//            Class<?>[] parameterTypesOfTargetConstructor = serviceDetails.getTargetConstructor().getParameterTypes();
//
//            for (Class<?> parameterType : parameterTypesOfTargetConstructor) {
//                if (!this.isAssignableTypePresent(parameterType)) {
//                    throw new ServiceInstantiationException(
//                            String.format(COULD_NOT_FIND_CONSTRUCTOR_PARAM_MSG,
//                                    serviceDetails.getServiceType().getName(),
//                                    parameterType.getName()
//                            )
//                    );
//                }
//            }
//        }
//    }



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
     * Adds each service to the enqueuedServiceDetails.
     * Initializes {@link DependencyResolveService}.
     *
     * @param mappedServices set of mapped services and their information.
     */
    private void init(Set<ServiceDetails> mappedServices) {
        this.enqueuedServiceDetails.clear();
        this.tempContainer.init(new ArrayList<>(), new ArrayList<>(), new InstantiationServiceImpl());

        for (ServiceDetails serviceDetails : mappedServices) {
            this.enqueuedServiceDetails.add(new EnqueuedServiceDetails(serviceDetails));
        }

        for (ServiceDetails instantiatedService : this.instantiationConfiguration.getProvidedServices()) {
            this.registerInstantiatedService(instantiatedService);
        }

        this.dependencyResolveService.init(mappedServices);
        this.dependencyResolveService.checkDependencies(this.enqueuedServiceDetails);
    }
}
