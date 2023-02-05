package org.example.container;

import org.example.annotations.*;
import org.example.configs.AnnotationsConfiguration;
import org.example.util.ServiceDetailsConstructComparator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link ServicesScanningService} implementation.
 * Iterates all located classes and looks for classes with @Service
 * annotation or one provided by the client and then collects data for that class.
 */
public class ServicesScanningServiceImpl implements ServicesScanningService {
    /**
     * Configuration containing annotations provided by the client.
     */
    private final AnnotationsConfiguration annotationsConfiguration;
    private final Set<ServiceDetails> serviceDetailsStorage;

    public ServicesScanningServiceImpl(AnnotationsConfiguration annotationsConfiguration) {
        this.annotationsConfiguration = annotationsConfiguration;
        this.serviceDetailsStorage = new LinkedHashSet<>();
        this.init();
    }

    /**
     * Iterates all given classes and filters those that have {@link Service} annotation
     * or one prided by the client and collects details for those classes.
     *
     * @param locatedClasses given set of classes.
     * @return set or services and their collected details.
     */
    @Override
    public Set<ServiceDetails> mappingClass(Set<Class<?>> locatedClasses) {
        final Map<Class<?>, List<Class<? extends Annotation>>> onlyServiceClasses = this.filterServiceClasses(locatedClasses);
        final Set<ServiceDetails> serviceDetailsStorage = new HashSet<>();

        for (Map.Entry<Class<?>, List<Class<? extends Annotation>>> serviceAnnotationEntry : onlyServiceClasses.entrySet()) {
            final Class<?> cls = serviceAnnotationEntry.getKey();
            final List<Class<? extends Annotation>> annotations = serviceAnnotationEntry.getValue();
            final ServiceDetails serviceDetails = new ServiceDetails(
                    cls,
                    annotations,
                    this.findSuitableConstructor(cls),
                    this.findVoidMethodWithZeroParamsAndAnnotations(cls, PostConstruct.class),
                    this.findVoidMethodWithZeroParamsAndAnnotations(cls, PreDestroy.class),
                    this.findBeans(cls)
            );
            this.serviceDetailsStorage.add(serviceDetails);
        }
        return this.serviceDetailsStorage
                .stream()
                .sorted(new ServiceDetailsConstructComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Iterates all given classes and filters those that have {@link Service} annotation
     * or one prided by the client.
     *
     * @return service annotated classes.
     */
    private Map<Class<?>, List<Class<? extends  Annotation>>> filterServiceClasses(Collection<Class<?>> scannedClasses) {
        final Set<Class<? extends  Annotation>> serviceAnnotations = this.annotationsConfiguration.getServiceAnnotations();
        final Map<Class<?>, List<Class<? extends Annotation>>> locatedClass = new HashMap<>();
        for (Class<?> scannedClass : scannedClasses) {
            if (scannedClass.isInterface() || scannedClass.isEnum() || scannedClass.isAnnotation()) {
                continue;
            }
            for (Annotation annotation : scannedClass.getAnnotations()) {
                if (serviceAnnotations.contains(annotation.annotationType())) {
                    locatedClass.put(scannedClass, Collections.singletonList(annotation.annotationType()));
                    break;
                }

                if (annotation.annotationType().isAnnotationPresent(AliasFor.class)) {
                    final Class<? extends Annotation> aliasValue = annotation.annotationType().getAnnotation(AliasFor.class).value();
                    if (serviceAnnotations.contains(aliasValue)) {
                        locatedClass.put(scannedClass, List.of(aliasValue, annotation.annotationType()));
                    }
                }
            }
        }
        return locatedClass;
    }

    /**
     * Scans a given class for methods that are considered beans.
     *
     * @param locatedClass the given class.
     * @return array or method references that are bean compliant.
     */
    private Method[] findBeans(Class<?> locatedClass) {
        final Set<Class<? extends Annotation>> beanAnnotations = this.annotationsConfiguration.getBeanAnnotations();
        final Set<Method> beanMethods = new HashSet<>();

        for (Method method : locatedClass.getDeclaredMethods()) {
            if (this.isNotBean(method)) {
                continue;
            }
            for (Class<? extends Annotation> beanAnnotation : beanAnnotations) {
                if (method.isAnnotationPresent(beanAnnotation)) {
                    method.setAccessible(true);
                    beanMethods.add(method);
                    break;
                }
                for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
                    if (declaredAnnotation.annotationType().isAnnotationPresent(AliasFor.class)) {
                        final Class<? extends Annotation> aliasValue = declaredAnnotation.annotationType().getAnnotation(AliasFor.class).value();

                        if (aliasValue == beanAnnotation) {
                            method.setAccessible(true);
                            beanMethods.add(method);

                            break;
                        }
                    }
                }
            }
        }
        return beanMethods.toArray(Method[]::new);
    }

    private boolean isNotBean(Method beanMethod) {
        int countParameter =  beanMethod.getParameterCount();
        Class<?> beanReturnType = beanMethod.getReturnType();
        return countParameter != 0 || beanReturnType == void.class || beanReturnType == Void.class;
    }

    /**
     * Looks for a constructor from the given class that has {@link Autowired} annotation
     * or gets the first one.
     *
     * @param locatedClass - the given class.
     * @return suitable constructor.
     */
    private Constructor<?> findSuitableConstructor(Class<?> locatedClass) {
        for (Constructor<?> ctr : locatedClass.getDeclaredConstructors()) {
            if (ctr.isAnnotationPresent(Autowired.class)) {
                ctr.setAccessible(true);
                return ctr;
            }
            for (Annotation declaredAnnotation : ctr.getDeclaredAnnotations()) {
                final Class<? extends Annotation> aliasValue = declaredAnnotation
                        .annotationType()
                        .getAnnotation(AliasFor.class)
                        .value();
                if (aliasValue.isAnnotationPresent(Autowired.class)) {
                    ctr.setAccessible(true);
                    return ctr;
                }
            }
        }
        return locatedClass.getConstructors()[0];
    }

    /**
     *
     * @param locatedClass
     * @param annotation
     * @return
     */
    private Method findVoidMethodWithZeroParamsAndAnnotations(Class<?> locatedClass, Class<? extends Annotation> annotation) {
        for (Method method : locatedClass.getDeclaredMethods()) {
            int countParams = method.getParameterCount();
            Class<?> getReturnType = method.getReturnType();
            if (countParams != 0 || (getReturnType != void.class && getReturnType != Void.class)) {
                continue;
            }
            if (method.isAnnotationPresent(annotation)) {
                method.setAccessible(true);
                return method;
            }
            for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
                if (declaredAnnotation.annotationType().isAnnotationPresent(AliasFor.class)) {
                    final Class<? extends Annotation> aliasValue = declaredAnnotation.annotationType().getAnnotation(AliasFor.class).value();
                    if (aliasValue == annotation) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Adds the platform's default annotations for services and beans on top of the
     * ones that the client might have provided.
     */
    private void init() {
        this.annotationsConfiguration.addBeanAnnotation(Bean.class);
        this.annotationsConfiguration.addServiceAnnotations(Service.class);
    }
}
