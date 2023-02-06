package org.example.configs;

public class Configuration {
    private final ScanningConfiguration annotations;
    private final InstantiationConfiguration instantiationConfiguration;
    public Configuration() {
        this.annotations = new ScanningConfiguration(this);
        this.instantiationConfiguration = new InstantiationConfiguration(this);
    }

    public ScanningConfiguration getAnnotations() {
        return this.annotations;
    }

    public InstantiationConfiguration getInstantiationConfiguration() {
        return this.instantiationConfiguration;
    }
}
