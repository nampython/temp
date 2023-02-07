package org.example.configs;

import org.example.constant.Constants;
import org.example.container.ServiceDetails;

import java.util.ArrayList;
import java.util.Collection;

public class InstantiationConfiguration extends BaseSubConfiguration {
    private int maximumAllowedIterations;
    private final Collection<ServiceDetails> providedServices;

    public InstantiationConfiguration(Configuration parentConfig) {
        super(parentConfig);
        this.providedServices = new ArrayList<>();
        this.maximumAllowedIterations = Constants.MAX_NUMBER_OF_INSTANTIATION_ITERATIONS;
    }

    public InstantiationConfiguration setMaximumNumberOfAllowedIterations(int num) {
        this.maximumAllowedIterations = num;
        return this;
    }

    public int getMaximumAllowedIterations() {
        return this.maximumAllowedIterations;
    }
    public InstantiationConfiguration addProvidedServices(Collection<ServiceDetails> serviceDetails) {
        this.providedServices.addAll(serviceDetails);
        return this;
    }

    public Collection<ServiceDetails> getProvidedServices() {
        return this.providedServices;
    }

}
