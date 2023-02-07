package org.example.instantiations;

import org.example.annotations.ScopeType;
import org.example.container.ServiceDetails;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

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

    public ServiceBeanDetails(Class<?> beanType,
                              Method beanMethod,
                              ServiceDetails serviceDetails,
                              Annotation annotation,
                              ScopeType scopeType,
                              String instanceName) {
        super.setServiceType(beanType);
        super.setBeans(new ArrayList<>(0));
        this.beanMethod = beanMethod;
        this.rootService = serviceDetails;
        super.setAnnotation(annotation);
        super.setScopeType(scopeType);
        super.setInstanceName(instanceName);
    }
    public Method getOriginMethod() {
        return this.beanMethod;
    }

    public ServiceDetails getRootService() {
        return this.rootService;
    }

//    @Override
//    public Object getProxyInstance() {
//        if (super.getProxyInstance() != null) {
//            return super.getProxyInstance();
//        }
//
//        return this.getActualInstance();
//    }
//
//    public boolean hasProxyInstance() {
//        return super.getProxyInstance() != null;
//    }
}
