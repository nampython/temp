package org.example.configs;

import org.example.configs.configurations.CustomAnnotationsConfiguration;

public class BaseSubConfiguration {
    private final Configuration parentConfiguration;

    protected BaseSubConfiguration(Configuration parenConfiguration) {
        this.parentConfiguration = parenConfiguration;
    }

    public Configuration and() {
        return this.parentConfiguration;
    }

}
