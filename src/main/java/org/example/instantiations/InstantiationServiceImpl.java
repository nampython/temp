package org.example.instantiations;

import org.example.container.ServiceDetails;
import org.example.exceptions.BeanInstantiationException;
import org.example.exceptions.PostConstructException;
import org.example.exceptions.PreDestroyExecutionException;
import org.example.exceptions.ServiceInstantiationException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InstantiationServiceImpl implements InstantiationService{
    private static final String INVALID_PARAMETERS_COUNT_MSG = "Invalid parameters count for '%s'.";
    private static final String ERROR_INSTANTIATION_OBJECT = "Cannot instantiate the object for %s with parameter %s";
    private static final String ERROR_INVOKE_POST_CONSTRUCT_METHOD = "Cannot invoke the method  with annotation @PostConstruct for %s";
    private static final String ERROR_INVOKE_PRE_DESTROY_METHOD = "Cannot invoke the method  with annotation @PreDestroy for %s";
    private static final String ERROR_INSTANTIATION_BEAN = "Cannot instantiate the object bean  with annotation @Bean for %s";

    @Override
    public void createInstance(@NotNull ServiceDetails serviceDetails, Object @NotNull ... constructorParams) throws ServiceInstantiationException {
        Constructor<?> targetConstructor = serviceDetails.getTargetConstructor();
        int parameterCount = targetConstructor.getParameterCount();
        if (!this.validParameterCount(parameterCount, constructorParams.length)) {
            throw new ServiceInstantiationException((String.format(INVALID_PARAMETERS_COUNT_MSG, serviceDetails.getServiceType().getName())));
        }
        try {
            Object instanceServiceDetails = targetConstructor.newInstance(constructorParams);
            serviceDetails.setInstance(instanceServiceDetails);
            this.callPostConstructMethod(serviceDetails);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServiceInstantiationException(String.format(ERROR_INSTANTIATION_OBJECT,serviceDetails.getServiceType().getName(), Arrays.toString(constructorParams)));
        }
    }

    private void callPostConstructMethod(@NotNull ServiceDetails serviceDetails) {
        Method postConstructMethod = serviceDetails.getPostConstructMethod();
        if (postConstructMethod == null) {
            return;
        }
        try {
            postConstructMethod.invoke(serviceDetails.getInstance());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PostConstructException(String.format(ERROR_INVOKE_POST_CONSTRUCT_METHOD, serviceDetails.getServiceType().getName()));
        }

    }

    private boolean validParameterCount(int parameterCount, int length) {
        return parameterCount == length;
    }

    @Override
    public void createBean(@NotNull ServiceBeanDetails serviceBeanDetails) throws BeanInstantiationException {
        Method beanMethod = serviceBeanDetails.getOriginMethod();
        Object instanceService = serviceBeanDetails.getRootService().getInstance();
        try {
            Object beanInstance = beanMethod.invoke(instanceService);
            serviceBeanDetails.setInstance(beanInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanInstantiationException(String.format(ERROR_INSTANTIATION_BEAN, serviceBeanDetails.getServiceType().getName()));
        }
    }

    @Override
    public void destroyInstance(@NotNull ServiceDetails serviceDetails) throws PreDestroyExecutionException {
        if (serviceDetails.getPreDestroyMethod() != null) {
            try {
                serviceDetails.getPreDestroyMethod().invoke(serviceDetails.getInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new PreDestroyExecutionException(String.format(ERROR_INVOKE_PRE_DESTROY_METHOD, serviceDetails.getServiceType().getName()));
            }
        }
        serviceDetails.setInstance(null);
    }
}
