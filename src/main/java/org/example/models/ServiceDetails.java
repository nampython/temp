package org.example.models;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceDetails<T> {
    private Class<T> serviceType;
    private Annotation annotation;
    private Constructor<T> targetConstructor;
    private T instance;
    private Method postConstructdMethod;
    private Method preDestroyMethod;
    private Method[] beans;
    private final List<ServiceDetails<?>> dependantServices;

    public ServiceDetails() {
        this.dependantServices = new ArrayList<>();
    }

    public ServiceDetails(Class<T> serviceType,
                          Annotation annotation,
                          Constructor<T> targetConstructor,
                          Method postConstructdMethod,
                          Method preDestroyMethod,
                          Method[] beans) {
        this();
        this.setAnnotation(annotation);
        this.setTargetConstructor(targetConstructor);
        this.setPostConstructdMethod(postConstructdMethod);
        this.setPreDestroyMethod(preDestroyMethod);
        this.setBeans(beans);
    }


    public Class<T> getServiceType() {
        return serviceType;
    }

    public void setServiceType(Class<T> serviceType) {
        this.serviceType = serviceType;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Constructor<T> getTargetConstructor() {
        return targetConstructor;
    }

    public void setTargetConstructor(Constructor<T> targetConstructor) {
        this.targetConstructor = targetConstructor;
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = (T) instance;
    }

    public Method getPostConstructdMethod() {
        return postConstructdMethod;
    }

    public void setPostConstructdMethod(Method postConstructdMethod) {
        this.postConstructdMethod = postConstructdMethod;
    }

    public Method getPreDestroyMethod() {
        return preDestroyMethod;
    }

    public void setPreDestroyMethod(Method preDestroyMethod) {
        this.preDestroyMethod = preDestroyMethod;
    }

    public Method[] getBeans() {
        return beans;
    }

    public void setBeans(Method[] beans) {
        this.beans = beans;
    }

    public List<ServiceDetails<?>> getDependantServices() {
        return Collections.unmodifiableList(this.dependantServices);
    }

    public void addDependantService(ServiceDetails<?> serviceDetails) {
        this.dependantServices.add(serviceDetails);
    }

    @Override
    public int hashCode() {
        if (this.serviceType == null) {
            return super.hashCode();
        }
        return this.serviceType.hashCode();
    }
}
