package org.example.services;

import org.example.exceptions.BeanInstantiationException;
import org.example.exceptions.ServiceInstantiationException;
import org.example.models.ServiceDetails;

import java.util.List;
import java.util.Set;

public interface ServicesInstantiationService {
    List<ServiceDetails<?>> instantiateServiceAndBean(Set<ServiceDetails<?>> mappedServices) throws ServiceInstantiationException, BeanInstantiationException;
}
