package org.example.instantiations;

import org.example.container.ServiceDetails;
import org.example.exceptions.ServiceInstantiationException;

import java.util.Collection;
import java.util.Set;

public interface ServicesInstantiationService {
    Collection<ServiceDetails> instantiateServicesAndBeans(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException;
}
