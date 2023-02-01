package org.example.configs;

import org.example.constant.Constants;

public class InstantiationConfiguration extends BaseSubConfiguration {
    private int maximumAllowedIterations;
    public InstantiationConfiguration(Configuration parentConfig) {
        super(parentConfig);
        this.maximumAllowedIterations = Constants.MAX_NUMBER_OF_INSTANTIATION_ITERATIONS;
    }

    public InstantiationConfiguration setMaximumNumberOfAllowedIterations(int num) {
        this.maximumAllowedIterations = num;
        return this;
    }

    public int getMaximumAllowedIterations() {
        return this.maximumAllowedIterations;
    }
}
