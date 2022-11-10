package org.example.services;

import org.example.exceptions.BeanInstantiationException;
import org.example.exceptions.PreDestroyExecutionException;
import org.example.exceptions.ServiceInstantiationException;
import org.example.models.ServiceBeanDetails;
import org.example.models.ServiceDetails;

public interface ObjectInstantiationService {
    public abstract void createInstance(ServiceDetails<?> serviceDetails, Object... constructorParams) throws ServiceInstantiationException;
    public abstract void createBeanInstance(ServiceBeanDetails<?> serviceBeanDetails) throws BeanInstantiationException;
    public abstract void destroyInstance(ServiceDetails<?> serviceDetails) throws PreDestroyExecutionException;
}
