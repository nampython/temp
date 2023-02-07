package org.example.instantiations;

import org.example.container.ServiceDetails;

import java.lang.reflect.Method;

/**
 * Descendant of @ServiceDetails that is made to contain additional bean details.
 * <p>
 * In this case some fields in the ServiceDetails will be left null.
 */
public class ServiceBeanDetails extends ServiceDetails {
    /**
     * Reference to the method that returns instance of this type of bean.
     */
    private final Method beanMethod;
    /**
     * The service from this bean was created.
     */
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

    @Override
    public Object getProxyInstance() {
        if (super.getProxyInstance() != null) {
            return super.getProxyInstance();
        }

        return this.getActualInstance();
    }

    public boolean hasProxyInstance() {
        return super.getProxyInstance() != null;
    }
}
