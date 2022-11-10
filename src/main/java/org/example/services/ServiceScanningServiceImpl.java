package org.example.services;

import org.example.annotations.*;
import org.example.configs.configurations.CustomAnnotationsConfiguration;
import org.example.models.ServiceDetails;
import org.example.utils.ServiceDetailsConstructorComparator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceScanningServiceImpl implements ServiceScanningService{
    private final CustomAnnotationsConfiguration configuration;

    public ServiceScanningServiceImpl(CustomAnnotationsConfiguration configuration) {
        this.configuration = configuration;
        this.init();
    }

    @Override
    public Set<ServiceDetails<?>> mapServices(Set<Class<?>> locatedClass) {
        final Set<ServiceDetails<?>> serviceDetailsStorage = new HashSet<>();
        final Set<Class<? extends Annotation>> customServiceAnnotations = configuration.getCustomeServiceAnnotations();

        for (Class<?> cls : locatedClass) {
            if (cls.isInterface()) {
                continue;
            }

            for (Annotation annotation : cls.getAnnotations()) {
                if (customServiceAnnotations.contains(annotation.annotationType())) {
                    ServiceDetails<?> serviceDetails = new ServiceDetails(
                            cls,
                            annotation,
                            this.findSuitableConstructor(cls),
                            this.findVoidMethodWithZeroParamsAndAnnotations(PostConstruct.class, cls),
                            this.findVoidMethodWithZeroParamsAndAnnotations(PreDestroy.class, cls),
                            this.findBeans(cls)
                    );
                    serviceDetailsStorage.add(serviceDetails);
                }
            }
        }
        return serviceDetailsStorage.stream()
                .sorted(new ServiceDetailsConstructorComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Constructor<?> findSuitableConstructor(Class<?> cls) {
        for (Constructor<?> ctr : cls.getDeclaredConstructors()) {
            if (ctr.isAnnotationPresent(Autowired.class)) {
                ctr.setAccessible(true);
                return ctr;
            }
        }
        return cls.getDeclaredConstructors()[0];
    }

    private Method findVoidMethodWithZeroParamsAndAnnotations(Class<? extends Annotation> annotation, Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.getParameterCount() != 0 ||
                    (method.getReturnType() != void.class && method.getReturnType() != Void.class) ||
                    !method.isAnnotationPresent(annotation)) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }
        return null;
    }

    private Method[] findBeans(Class<?> cls) {
        final Set<Class<? extends Annotation>> beanAnnotations = this.configuration.getCustomBeanAnnotations();
        final Set<Method> beanMethods = new HashSet<>();

        for (Method method : cls.getDeclaredMethods()) {
            if (method.getParameterCount() != 0 || method.getReturnType() == void.class || method.getReturnType() == Void.class) {
                continue;
            }
            for (Class<? extends Annotation> beanAnnotation : beanAnnotations) {
                if (method.isAnnotationPresent(beanAnnotation)) {
                    method.setAccessible(true);
                    beanMethods.add(method);
                    break;
                }
            }
        }
        return beanMethods.toArray(Method[]::new);
    }


    private void init() {
        this.configuration.getCustomeServiceAnnotations().add(Service.class);
        this.configuration.getCustomBeanAnnotations().add(Bean.class);
    }
}
