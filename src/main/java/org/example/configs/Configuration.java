package org.example.configs;

public class Configuration {
    private final AnnotationsConfiguration annotations;
    private final InstantiationConfiguration instantiationConfiguration;
    public Configuration() {
        this.annotations = new AnnotationsConfiguration(this);
        this.instantiationConfiguration = new InstantiationConfiguration(this);
    }

    public AnnotationsConfiguration getAnnotations() {
        return this.annotations;
    }

    public InstantiationConfiguration getInstantiationConfiguration() {
        return this.instantiationConfiguration;
    }
}
