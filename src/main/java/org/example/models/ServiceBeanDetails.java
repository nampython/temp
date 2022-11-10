package org.example.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ServiceBeanDetails<T> extends ServiceDetails<T> {
    private final Method originMethod;
    private final ServiceDetails<?> rootService;

    public ServiceBeanDetails(Class<T> beanType, Method originMethod, ServiceDetails<?> rootService) {
        this.setServiceType(beanType);
        this.setBeans(new Method[0]);
        this.originMethod = originMethod;
        this.rootService = rootService;
    }

    public Method getOriginMethod() {
        return this.originMethod;
    }

    public ServiceDetails<?> getRootService() {
        return rootService;
    }
}

