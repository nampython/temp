package org.example.instantiations;

import org.example.container.ServiceDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface ServicesInstantiationService {
    Collection<ServiceDetails> instantiateServicesAndBeans(Set<ServiceDetails> mappedClasses);
}