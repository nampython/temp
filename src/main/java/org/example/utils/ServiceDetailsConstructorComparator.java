package org.example.utils;

import org.example.models.ServiceDetails;

import java.util.Comparator;

public class ServiceDetailsConstructorComparator implements Comparator<ServiceDetails> {

    @Override
    public int compare(ServiceDetails serviceDetails1, ServiceDetails serviceDetails2) {
        return Integer.compare(
                serviceDetails1.getTargetConstructor().getParameterCount(),
                serviceDetails2.getTargetConstructor().getParameterCount()
        );
    }
}
