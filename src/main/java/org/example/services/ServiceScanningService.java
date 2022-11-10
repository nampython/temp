package org.example.services;

import org.example.models.ServiceDetails;

import java.util.Set;

public interface ServiceScanningService {
    Set<ServiceDetails<?>> mapServices(Set<Class<?>> locatedClass);
}
