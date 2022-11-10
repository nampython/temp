package org.example.configs;

import org.example.configs.configurations.CustomAnnotationsConfiguration;
import org.example.configs.configurations.InstantiationConfiguration;

public class Configuration {
    private final CustomAnnotationsConfiguration annotation;
    private final InstantiationConfiguration instantiations;

    public Configuration() {
        this.annotation = new CustomAnnotationsConfiguration(this);
        this.instantiations = new InstantiationConfiguration(this);
    }

    public CustomAnnotationsConfiguration annotations() {
        return this.annotation;
    }

    public InstantiationConfiguration instantiations() {
        return this.instantiations;
    }

    public Configuration build() {
        return this;
    }
}
