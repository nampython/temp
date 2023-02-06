package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;
import org.example.instantiations.ServiceBeanDetails;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for all services and beans.
 * <p>
 * Contains functionality for managing the application context
 * by reloading or accessing certain services.
 */
public class DependencyContainerImpl implements DependencyContainer{
    private static final String ALREADY_INITIALIZED_MSG = "Dependency container already initialized.";
    private static final String SERVICE_NOT_FOUND_FORMAT = "Service \"%s\" was not found.";
    private final Map<Class<?>, ServiceDetails> cachedServices;
    private final Map<Class<?>, Collection<ServiceDetails>> cachedImplementations;
    private final Map<Class<? extends Annotation>, Collection<ServiceDetails>> cachedServicesByAnnotation;
    private boolean isInit;
    private Collection<ServiceDetails> servicesAndBeans;
    private Collection<Class<?>> allLocatedClasses;
    private InstantiationService instantiationService;


    public DependencyContainerImpl() {
        this.cachedServices = new HashMap<>();
        this.cachedImplementations = new HashMap<>();
        this.cachedServicesByAnnotation = new HashMap<>();
        this.isInit = false;
    }

    @Override
    public void init(Collection<Class<?>> locatedClass, Collection<ServiceDetails> servicesAndBeans, InstantiationService instantiationService) throws AlreadyInitializedException {
        if (this.isInit) {
            throw new AlreadyInitializedException(ALREADY_INITIALIZED_MSG);
        }
        this.servicesAndBeans = servicesAndBeans;
        this.instantiationService = instantiationService;
        this.allLocatedClasses = locatedClass;
        this.isInit = true;
    }


    /**
     * Gets service instance for a given type.
     *
     * @param classService the given type.
     * @param <T>         generic type.
     * @return instance of the required service or null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getServiceInstance(Class<T> classService) {
        final ServiceDetails serviceDetails = this.getSingleService(classService);
        if (serviceDetails != null) {
            return (T) serviceDetails.getProxyInstance();
        }
        return null;
    }

    /**
     *
     * @param serviceType
     * @return
     */
    @Override
    public ServiceDetails getSingleService(Class<?> serviceType) {
        if (this.cachedServices.containsKey(serviceType)) {
            return this.cachedServices.get(serviceType);
        }
        final ServiceDetails serviceDetails = this.servicesAndBeans.stream()
                .filter(sd -> serviceType.isAssignableFrom(sd.getServiceType()))
                .findFirst().orElse(null);
        if (serviceDetails != null) {
            this.cachedServices.put(serviceType, serviceDetails);
        }
        return serviceDetails;
    }

    /**
     * @return a collection of all classes that were found in the application
     * including even classes that are not annotated with any annotation.
     */
    @Override
    public Collection<Class<?>> getAllLocatedClasses() {
        return this.allLocatedClasses;
    }

    /**
     * @param serviceType given interface.
     * @return collection of service details that implement the given interface.
     */
    @Override
    public Collection<ServiceDetails> getImplementations(Class<?> serviceType) {
        if (this.cachedImplementations.containsKey(serviceType)) {
            return this.cachedImplementations.get(serviceType);
        }
        List<ServiceDetails> implementations = this.servicesAndBeans.stream()
                .filter(sd -> serviceType.isAssignableFrom(sd.getServiceType()))
                .collect(Collectors.toList());
        this.cachedImplementations.put(serviceType, implementations);
        return implementations;
    }

    /**
     * Gets all services that are mapped with a given annotation.
     *
     * @param annotationType the given annotation.
     */
    @Override
    public Collection<ServiceDetails> getServiceDetailByAnnotation(Class<? extends Annotation> annotationType) {
        if (this.cachedServicesByAnnotation.containsKey(annotationType)) {
            return this.cachedServicesByAnnotation.get(annotationType);
        }
        List<ServiceDetails> serviceDetailsByAnnotation = this.servicesAndBeans.stream()
                .filter(sd -> sd.getAnnotation().annotationType() == annotationType)
                .collect(Collectors.toList());
        this.cachedServicesByAnnotation.put(annotationType, serviceDetailsByAnnotation);
        return serviceDetailsByAnnotation;
    }


    @Override
    public Collection<ServiceDetails> getAllServiceDetails() {
        return this.servicesAndBeans;
    }



    @Override
    public <T> T reload(T service) {
        return this.reload(service, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T reload(T service, boolean reloadDependantServices) {
        final ServiceDetails serviceDetails =  this.getSingleService(service.getClass());
        if (serviceDetails == null) {
            return null;
        }
        this.handleReload(serviceDetails, reloadDependantServices);
        return (T) serviceDetails.getActualInstance();
    }


    private <T> void handleReload(ServiceDetails serviceDetails, boolean isReloadDependent) {
        this.instantiationService.destroyInstance(serviceDetails);
        this.afterDestroy(serviceDetails);
    }

    private <T> void afterDestroy(ServiceDetails serviceDetails) {
        if (serviceDetails instanceof ServiceBeanDetails) {
            this.instantiationService.createBean((ServiceBeanDetails)serviceDetails);
        } else {
            this.instantiationService.createInstance(serviceDetails, this.collectDependencies(serviceDetails));
        }

    }

    private <T> Object collectDependencies(ServiceDetails serviceDetails) {
        final Class<?>[] parameterTypes = serviceDetails.getTargetConstructor().getParameterTypes();
        final Object[] dependencyInstances = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            dependencyInstances[i] = this.getServiceInstance(parameterTypes[i]);
        }
        return dependencyInstances;
    }

}
