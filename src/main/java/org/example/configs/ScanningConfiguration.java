package org.example.configs;

import java.lang.annotation.Annotation;
import java.util.*;

public class ScanningConfiguration extends BaseSubConfiguration {
    private final Set<Class<? extends Annotation>> serviceAnnotations;
    private final Set<Class<? extends Annotation>> beanAnnotations;
    private final Map<Class<?>, Class<? extends Annotation>> additionalClasses;

    public ScanningConfiguration(Configuration configuration) {
        super(configuration);
        this.serviceAnnotations = new HashSet<>();
        this.beanAnnotations = new HashSet<>();
        this.additionalClasses = new HashMap<>();
    }

    public ScanningConfiguration addServiceAnnotation(Class<? extends Annotation> annotation) {
        this.serviceAnnotations.add(annotation);
        return this;
    }

    public ScanningConfiguration addServiceAnnotations(Collection<Class<? extends Annotation>> annotations) {
        this.serviceAnnotations.addAll(Set.copyOf(annotations));
        return this;
    }

    public ScanningConfiguration addBeanAnnotation(Class<? extends Annotation> annotation) {
        this.beanAnnotations.add(annotation);
        return this;
    }

    public ScanningConfiguration addBeanAnnotations(Collection<Class<? extends Annotation>> annotations) {
        this.beanAnnotations.addAll(Set.copyOf(annotations));
        return this;
    }

    public ScanningConfiguration addAdditionalClassesForScanning(Map<Class<?>, Class<? extends Annotation>> additionalClasses) {
        this.additionalClasses.putAll(additionalClasses);
        return this;
    }

    public Set<Class<? extends Annotation>> getBeanAnnotations() {
        return this.beanAnnotations;
    }

    public Set<Class<? extends Annotation>> getServiceAnnotations() {
        return this.serviceAnnotations;
    }

    public Map<Class<?>, Class<? extends Annotation>> getAdditionalClasses() {
        return this.additionalClasses;
    }
}