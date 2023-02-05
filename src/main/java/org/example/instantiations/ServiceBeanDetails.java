package org.example.instantiations;

import org.example.container.ServiceDetails;

import java.lang.reflect.Method;

public class ServiceBeanDetails extends ServiceDetails {
    private final Method beanMethod;
    private final ServiceDetails rootService;

    public ServiceBeanDetails(Class<?> serviceType, Method beanMethod, ServiceDetails serviceDetails) {
        this.setServiceType(serviceType);
        this.setBeans(new Method[0]);
        this.beanMethod = beanMethod;
        this.rootService = serviceDetails;
    }
    public Method getOriginMethod() {
        return this.beanMethod;
    }

    public ServiceDetails getRootService() {
        return this.rootService;
    }
}
