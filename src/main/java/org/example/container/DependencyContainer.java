package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public interface DependencyContainer {
    void init(Collection<Class<?>> localClasses,Collection<ServiceDetails> servicesAndBeans, InstantiationService instantiationService) throws AlreadyInitializedException;
    void update(Object service);
    void update(Class<?> serviceType, Object serviceInstance);
    void update(Class<?> serviceType, Object serviceInstance, boolean destroyOldInstance);

    <T> T reload(T service);
    <T> T reload(T service, boolean reloadDependantServices);
    <T> T getServiceInstance(Class<T> classService);
    <T> T getService(Class<?> serviceType, String instanceName);
    <T> T getNewInstance(Class<?> serviceType);
    <T> T getNewInstance(Class<?> serviceType, String instanceName);

    ServiceDetails getSingleService(Class<?> serviceType);
    ServiceDetails getServiceDetails(Class<?> serviceType, String instanceName);

    Collection<ServiceDetails> getAllServiceDetails();
    Collection<ServiceDetails> getImplementations(Class<?> serviceType);
    Collection<ServiceDetails> getServiceDetailByAnnotation(Class<? extends Annotation> annotationType);
    Collection<Class<?>> getAllLocatedClasses();
}
