package org.example.container;

import org.example.exceptions.AlreadyInitializedException;
import org.example.instantiations.InstantiationService;
import org.example.instantiations.ServiceBeanDetails;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyContainerImpl implements DependencyContainer{
    private static final String ALREADY_INITIALIZED_MSG = "Dependency container already initialized.";
    private boolean isInit;
    private  List<ServiceDetails> allServiceAndBean;
    private InstantiationService instantiationService;

    public DependencyContainerImpl() {
        this.isInit = false;
    }

    @Override
    public void init(List<ServiceDetails> servicesAndBeans, InstantiationService instantiationService) throws AlreadyInitializedException {
        if (this.isInit) {
            throw new AlreadyInitializedException(ALREADY_INITIALIZED_MSG);
        }
        this.allServiceAndBean = servicesAndBeans;
        this.instantiationService = instantiationService;
        this.isInit = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getServiceInstance(Class<T> classService) {
        return (T) this.getSingleService(classService).getInstance();
    }

    @Override
    public <T> ServiceDetails getSingleService(Class<T> serviceClass) {
        return  this.allServiceAndBean.stream()
                .filter(sd -> serviceClass.isAssignableFrom(sd.getServiceType()))
                .findFirst().orElse(null);
    }

    @Override
    public List<ServiceDetails> getAllServiceDetails() {
        return Collections.unmodifiableList(this.allServiceAndBean);
    }

    @Override
    public <T> T reload(T service) {
        return this.reload(service, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T reload(T service, boolean reloadDependantServices) {
        ServiceDetails serviceDetails =  this.getSingleService(service.getClass());
        if (serviceDetails == null) {
            return null;
        }
        this.handleReload(serviceDetails, reloadDependantServices);
        return (T) serviceDetails.getInstance();
    }

    private <T> void handleReload(ServiceDetails serviceDetails, boolean isReloadDependent) {
        this.instantiationService.destroyInstance(serviceDetails);
        this.afterDestroy(serviceDetails);
    }

    private <T> void afterDestroy(ServiceDetails serviceDetails) {
        if (serviceDetails instanceof ServiceBeanDetails) {
            this.instantiationService.createBean((ServiceBeanDetails)serviceDetails);
        } else {
            this.instantiationService.createInstance(serviceDetails, this.collectDependencies(serviceDetails));
        }

    }

    private <T> Object collectDependencies(ServiceDetails serviceDetails) {
        Class<?>[] parameterTypes = serviceDetails.getTargetConstructor().getParameterTypes();
        Object[] dependencyInstances = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            dependencyInstances[i] = this.getServiceInstance(parameterTypes[i]);
        }
        return dependencyInstances;
    }

    @Override
    public List<ServiceDetails> getServiceDetailByAnnotation(Class<? extends Annotation> annotationType) {
        return this.allServiceAndBean.stream()
                .filter(sd -> sd.getAnnotation().annotationType() == annotationType)
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> getAllServicesInstance() {
        return this.allServiceAndBean.stream()
                .map(ServiceDetails::getInstance)
                .collect(Collectors.toList());
    }
}
