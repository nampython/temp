package org.example.middleware;

import org.example.container.ServiceDetails;

@FunctionalInterface
public interface ServiceDetailsCreated {
    void serviceDetailsCreated(ServiceDetails serviceDetails);
}
