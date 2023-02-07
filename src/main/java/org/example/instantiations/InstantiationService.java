package org.example.instantiations;

import org.example.container.ServiceDetails;
import org.example.exceptions.BeanInstantiationException;
import org.example.exceptions.PreDestroyExecutionException;
import org.example.exceptions.ServiceInstantiationException;

public interface InstantiationService {
    void createInstance(ServiceDetails serviceDetails, Object[] constructorParams, Object[] autowiredFieldInstances) throws ServiceInstantiationException;
    void createBean(ServiceBeanDetails serviceBeanDetails) throws BeanInstantiationException;
    void destroyInstance(ServiceDetails serviceDetails) throws PreDestroyExecutionException;
}
