package org.example.instantiations;

import org.example.container.ServiceDetails;

import java.util.List;
import java.util.Set;

public interface ServicesInstantiationService {
    List<ServiceDetails> instantiateServicesAndBeans(Set<ServiceDetails> mappedClasses);
}