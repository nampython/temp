package org.example.configs;

public class Configuration {
    private final ScanningConfiguration annotations;
    private final InstantiationConfiguration instantiationConfiguration;
    private final GeneralConfiguration generalConfiguration;

    public Configuration() {
        this.annotations = new ScanningConfiguration(this);
        this.instantiationConfiguration = new InstantiationConfiguration(this);
        this.generalConfiguration = new GeneralConfiguration(this);

    }

    public ScanningConfiguration scanning() {
        return this.annotations;
    }

    public InstantiationConfiguration getInstantiationConfiguration() {
        return this.instantiationConfiguration;
    }
    public Configuration build() {
        return this;
    }
    public GeneralConfiguration general() {
        return this.generalConfiguration;
    }
}
