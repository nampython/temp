package org.example.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Simple POJO class that holds information about a given class.
 * <p>
 * This is needed since that way we have the data scanned only once and we
 * will improve performance at runtime since the data in only collected once
 * at startup.
 */
public class ServiceDetails {

    private static final String PROXY_ALREADY_CREATED_MSG = "Proxy instance already created.";

    /**
     * The type of the service.
     */
    private Class<?> serviceType;
    /**
     * Service instance.
     */
    private Object instance;
    /**
     * Proxy instance that will be injected into services instead of actual instance.
     */
    private Object proxyInstance;
    /**
     * The constructor that will be used to create an instance of the service.
     */
    private Constructor<?> targetConstructor;
    /**
     * The annotation used to map the service (@Service or a custom one).
     */
    private Annotation annotation;
    /**
     * The reference to all @Bean (or a custom one) annotated methods.
     */
    private Method[] beans;
    /**
     * Reference to the post construct method if any.
     */
    private Method postConstructMethod;
    /**
     * Reference to the pre destroy method if any.
     */
    private Method preDestroyMethod;
    /**
     * List of all services that depend on this one.
     */
    private final List<ServiceDetails> dependantServices;

    public ServiceDetails() {
        this.dependantServices = new ArrayList<>();
    }

    public ServiceDetails(Class<?> serviceType,
                          Annotation annotation,
                          Constructor<?> targetConstructor,
                          Method postConstructMethod,
                          Method preDestroyMethod,
                          Method[] beans) {
        this();
        this.setServiceType(serviceType);
        this.setAnnotation(annotation);
        this.setTargetConstructor(targetConstructor);
        this.setPostConstructMethod(postConstructMethod);
        this.setPreDestroyMethod(preDestroyMethod);
        this.setBeans(beans);
    }

    public Class<?> getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(Class<?> serviceType) {
        this.serviceType = serviceType;
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Constructor<?> getTargetConstructor() {
        return this.targetConstructor;
    }

    public void setTargetConstructor(Constructor<?> targetConstructor) {
        this.targetConstructor = targetConstructor;
    }

    public Object getActualInstance() {
        return this.instance;
    }
    public Object getProxyInstance() {
        return this.proxyInstance;
    }

    public void setProxyInstance(Object proxyInstance) {
        if (this.proxyInstance != null) {
            throw new IllegalArgumentException(PROXY_ALREADY_CREATED_MSG);
        }

        this.proxyInstance = proxyInstance;
    }

    public void setInstance(Object instance) {
        this.instance =  instance;
    }

    public Method getPostConstructMethod() {
        return this.postConstructMethod;
    }

    public void setPostConstructMethod(Method postConstructMethod) {
        this.postConstructMethod = postConstructMethod;
    }

    public Method getPreDestroyMethod() {
        return this.preDestroyMethod;
    }

    public void setPreDestroyMethod(Method preDestroyMethod) {
        this.preDestroyMethod = preDestroyMethod;
    }

    public Method[] getBeans() {
        return this.beans;
    }

    public void setBeans(Method[] beans) {
        this.beans = beans;
    }

    public List<ServiceDetails> getDependantServices() {
        return Collections.unmodifiableList(this.dependantServices);
    }

    public void addDependantService(ServiceDetails serviceDetails) {
        this.dependantServices.add(serviceDetails);
    }

    @Override
    public int hashCode() {
        if (this.serviceType == null) {
            return super.hashCode();
        }

        return this.serviceType.hashCode();
    }

    @Override
    public String toString() {
        if (this.serviceType == null) {
            return super.toString();
        }

        return this.serviceType.getName();
    }
}
