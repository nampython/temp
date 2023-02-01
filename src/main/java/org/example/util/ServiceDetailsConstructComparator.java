package org.example.util;

import org.example.container.ServiceDetails;

import java.util.Comparator;

public class ServiceDetailsConstructComparator implements Comparator<ServiceDetails> {
    @Override
    public int compare(ServiceDetails serviceDetails1, ServiceDetails serviceDetails2) {
        return Integer.compare(
                serviceDetails1.getTargetConstructor().getParameterCount(),
                serviceDetails2.getTargetConstructor().getParameterCount()
        );
    }
}
