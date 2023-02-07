package org.example.middleware;

import org.example.model.DependencyParam;

public interface DependencyResolver {
    boolean canResolve(DependencyParam dependencyParam);
    Object resolve(DependencyParam dependencyParam);
}
