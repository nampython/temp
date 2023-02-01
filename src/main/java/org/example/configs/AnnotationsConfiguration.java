package org.example.configs;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationsConfiguration extends BaseSubConfiguration {
    private final Set<Class<? extends Annotation>> serviceAnnotations;
    private final Set<Class<? extends Annotation>> beanAnnotations;

    public AnnotationsConfiguration(Configuration configuration) {
        super(configuration);
        this.serviceAnnotations = new HashSet<>();
        this.beanAnnotations = new HashSet<>();
    }

    public AnnotationsConfiguration addServiceAnnotation(Class<? extends Annotation> annotation) {
        this.serviceAnnotations.add(annotation);
        return this;
    }

    @SafeVarargs
    public final AnnotationsConfiguration addServiceAnnotations(Class<? extends Annotation>... annotations) {
        this.serviceAnnotations.addAll(Arrays.asList(annotations));
        return this;
    }

    public AnnotationsConfiguration addBeanAnnotation(Class<? extends Annotation> annotation) {
        this.beanAnnotations.add(annotation);
        return this;
    }

    @SafeVarargs
    public final AnnotationsConfiguration addBeanAnnotations(Class<? extends Annotation>... annotations) {
        this.beanAnnotations.addAll(Arrays.asList((annotations)));
        return this;
    }

    public Set<Class<? extends Annotation>> getBeanAnnotations() {
        return this.beanAnnotations;
    }

    public Set<Class<? extends Annotation>> getServiceAnnotations() {
        return this.serviceAnnotations;
    }
}
