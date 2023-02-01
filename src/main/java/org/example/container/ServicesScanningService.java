package org.example.container;

import java.util.Set;

public interface ServicesScanningService {
    Set<ServiceDetails<?>> mappingClass(Set<Class<?>> locatedClasses);
}
