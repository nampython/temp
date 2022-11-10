package org.example.configs.configurations;

import org.example.configs.BaseSubConfiguration;
import org.example.configs.Configuration;
import org.example.constants.Constants;

public class InstantiationConfiguration extends BaseSubConfiguration {

    public int maximumNumberOfAllowedIterations;

    public InstantiationConfiguration(Configuration parenConfiguration) {
        super(parenConfiguration);
        this.maximumNumberOfAllowedIterations = Constants.MAX_NUMBER_OF_INSTANTIATION_ITERATIONS;
    }

    public InstantiationConfiguration setMaximumNumberOfAllowedIterations(int num) {
        this.maximumNumberOfAllowedIterations = num;
        return this;
    }

    public int getMaximumNumberOfAllowedIterations() {
        return this.maximumNumberOfAllowedIterations;
    }
}
