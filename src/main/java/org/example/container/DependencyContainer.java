package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;

import java.lang.annotation.Annotation;
import java.util.List;

public interface DependencyContainer {
    void init(List<ServiceDetails<?>> servicesAndBeans, InstantiationService instantiationService) throws AlreadyInitializedException;
    <T> T getServiceInstance(Class<T> classService);
    List<ServiceDetails<?>> getServiceDetailByAnnotation(Class<? extends Annotation> service);
    List<Object> getAllServicesInstance();
    <T> ServiceDetails<T> getSingleService(Class<T> serviceType);
    List<ServiceDetails<?>> getAllServiceDetails();
    <T> T reload(T service);
    <T> T reload(T service, boolean reloadDependantServices);
}