package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public interface DependencyContainer {
    void init(Collection<Class<?>> localClasses,Collection<ServiceDetails> servicesAndBeans, InstantiationService instantiationService) throws AlreadyInitializedException;
    void update(Object service);
    <T> T reload(T service);
    <T> T reload(T service, boolean reloadDependantServices);
    <T> T getServiceInstance(Class<T> classService);
    ServiceDetails getSingleService(Class<?> serviceType);
    Collection<ServiceDetails> getAllServiceDetails();
    Collection<ServiceDetails> getImplementations(Class<?> serviceType);
    Collection<ServiceDetails> getServiceDetailByAnnotation(Class<? extends Annotation> annotationType);
    Collection<Class<?>> getAllLocatedClasses();
}
