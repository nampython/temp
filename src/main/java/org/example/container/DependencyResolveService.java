package org.example.container;

import org.example.instantiations.EnqueuedServiceDetails;

import java.util.Collection;
import java.util.List;

public interface DependencyResolveService {
    List<EnqueuedServiceDetails> resolveDependencies(Collection<ServiceDetails> serviceDetails);
}
