package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;
import org.example.instantiations.ServiceBeanDetails;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
     * Replaces instance of a service with a new provided one.
     *
     * @param service new instance of a given service
     */
    @Override
    public void update(Object service) {
        this.update(service.getClass(), service);
//        final ServiceDetails serviceDetails = this.getSingleService(service.getClass());
//        if (serviceDetails == null) {
//            throw new IllegalArgumentException(String.format(SERVICE_NOT_FOUND_FORMAT, service));
//        }
//        this.instantiationService.destroyInstance(serviceDetails);
//        serviceDetails.setInstance(service);
    }

    @Override
    public void update(Class<?> serviceType, Object serviceInstance) {
        this.update(serviceType, serviceInstance, true);
    }

    @Override
    public void update(Class<?> serviceType, Object serviceInstance, boolean destroyOldInstance) {
        final ServiceDetails serviceDetails = this.getSingleService(serviceType);
        if (serviceDetails == null) {
            throw new IllegalArgumentException(String.format(SERVICE_NOT_FOUND_FORMAT, serviceType.getName()));
        }

        if (destroyOldInstance) {
            this.instantiationService.destroyInstance(serviceDetails);
        }
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getNewInstance(Class<?> serviceType) {
        final ServiceDetails serviceDetails = this.getSingleService(serviceType);

        if (serviceDetails == null) {
            throw new IllegalArgumentException(String.format(SERVICE_NOT_FOUND_FORMAT, serviceType.getName()));
        }

        final Object oldInstance = serviceDetails.getActualInstance();

        if (serviceDetails instanceof ServiceBeanDetails) {
            ServiceBeanDetails serviceBeanDetails = (ServiceBeanDetails) serviceDetails;
            this.instantiationService.createBean(serviceBeanDetails);
        } else {
            this.instantiationService.createInstance(
                    serviceDetails,
                    this.collectDependencies(serviceDetails),
                    this.collectAutowiredFieldsDependencies(serviceDetails)
            );
        }

        final Object newInstance = serviceDetails.getActualInstance();
        serviceDetails.setInstance(oldInstance);

        return (T) newInstance;
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
            ServiceBeanDetails serviceBeanDetails = (ServiceBeanDetails) serviceDetails;
            this.instantiationService.createBean(serviceBeanDetails);

            if (!serviceBeanDetails.hasProxyInstance()) {
                //Since bean has no proxy, reload all dependant classes.
                for (ServiceDetails dependantService : serviceDetails.getDependantServices()) {
                    this.reload(dependantService);
                }
            }
        } else {
            this.instantiationService.createInstance(
                    serviceDetails,
                    this.collectDependencies(serviceDetails),
                    this.collectAutowiredFieldsDependencies(serviceDetails)
            );
        }
    }
    /**
     * Gets instances of all {@link Autowired} annotated dependencies for a given service.
     *
     * @param serviceDetails - the given service.
     * @return array of instantiated dependencies.
     */
    private Object[] collectAutowiredFieldsDependencies(ServiceDetails serviceDetails) {
        final Field[] autowireAnnotatedFields = serviceDetails.getAutowireAnnotatedFields();
        final Object[] instances = new Object[autowireAnnotatedFields.length];

        for (int i = 0; i < autowireAnnotatedFields.length; i++) {
            instances[i] = this.getSingleService(autowireAnnotatedFields[i].getType());
        }

        return instances;
    }
    /**
     * Gets instances of all required dependencies for a given service.
     *
     * @param serviceDetails - the given service.
     * @return array of instantiated dependencies.
     */
    private Object[] collectDependencies(ServiceDetails serviceDetails) {
        final Class<?>[] parameterTypes = serviceDetails.getTargetConstructor().getParameterTypes();
        final Object[] dependencyInstances = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            dependencyInstances[i] = this.getSingleService(parameterTypes[i]);
        }

        return dependencyInstances;
    }

}
