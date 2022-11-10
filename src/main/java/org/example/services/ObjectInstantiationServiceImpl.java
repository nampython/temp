package org.example.services;

import org.example.exceptions.BeanInstantiationException;
import org.example.exceptions.PostConstructorException;
import org.example.exceptions.PreDestroyExecutionException;
import org.example.exceptions.ServiceInstantiationException;
import org.example.models.ServiceBeanDetails;
import org.example.models.ServiceDetails;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectInstantiationServiceImpl implements ObjectInstantiationService{

    private static final String INVALID_PARAMETERS_COUNT_MSG = "Invalid parameters count for '%s'.";

    @Override
    public void createInstance(ServiceDetails<?> serviceDetails, Object... constructorParams) throws ServiceInstantiationException {
        Constructor<?> targetConstructor = serviceDetails.getTargetConstructor();

        if (constructorParams.length !=  targetConstructor.getParameterCount()) {
            throw new ServiceInstantiationException(String.format(INVALID_PARAMETERS_COUNT_MSG, serviceDetails.getServiceType().getName()));
        }

        try {
            Object instance = targetConstructor.newInstance(constructorParams);
            serviceDetails.setInstance(instance);
            this.invokePostConstruct(serviceDetails);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServiceInstantiationException(e.getMessage(), e);
        }
    }

    @Override
    public void createBeanInstance(ServiceBeanDetails<?> serviceBeanDetails) throws BeanInstantiationException {
        Method originMethod = serviceBeanDetails.getOriginMethod();
        Object rootInstance = serviceBeanDetails.getRootService().getInstance();
        try {
            Object instance = originMethod.invoke(rootInstance);
            serviceBeanDetails.setInstance(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanInstantiationException(e.getMessage(), e);
        }
    }


    private void invokePostConstruct(ServiceDetails<?> serviceDetails) throws PostConstructorException {
        if (serviceDetails.getPostConstructdMethod() == null) {
            return;
        }
        try {
            serviceDetails.getPostConstructdMethod().invoke(serviceDetails.getInstance());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PostConstructorException(e.getMessage(), e);
        }
    }

    @Override
    public void destroyInstance(ServiceDetails<?> serviceDetails) throws PreDestroyExecutionException {
        if (serviceDetails.getPreDestroyMethod() != null) {
            try {
                serviceDetails.getPreDestroyMethod().invoke(serviceDetails.getInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new PreDestroyExecutionException(e.getMessage(), e);
            }
        }

        serviceDetails.setInstance(null);
    }
}
