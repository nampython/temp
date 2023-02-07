package org.example.container;

import org.example.annotations.*;
import org.example.configs.ScanningConfiguration;
import org.example.util.AliasFinder;
import org.example.util.ServiceDetailsConstructComparator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    private final ScanningConfiguration scanningConfiguration;
    private final Set<ServiceDetails> serviceDetailsStorage;

    public ServicesScanningServiceImpl(ScanningConfiguration scanningConfiguration) {
        this.scanningConfiguration = scanningConfiguration;
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
        final Map<Class<?>, Annotation> onlyServiceClasses = this.filterServiceClasses(locatedClasses);
        final Set<ServiceDetails> serviceDetailsStorage = new HashSet<>();

        for (Map.Entry<Class<?>, Annotation> serviceAnnotationEntry : onlyServiceClasses.entrySet()) {
            final Class<?> cls = serviceAnnotationEntry.getKey();
            final Annotation annotation = serviceAnnotationEntry.getValue();
            final ServiceDetails serviceDetails = new ServiceDetails(
                    cls,
                    annotation,
                    this.findSuitableConstructor(cls),
                    this.findVoidMethodWithZeroParamsAndAnnotations(cls, PostConstruct.class),
                    this.findVoidMethodWithZeroParamsAndAnnotations(cls, PreDestroy.class),
                    this.findBeans(cls),
                    this.findAutowireAnnotatedFields(cls, new ArrayList<>()).toArray(new Field[0])
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
    private Map<Class<?>, Annotation> filterServiceClasses(Collection<Class<?>> scannedClasses) {
        final Set<Class<? extends Annotation>> serviceAnnotations = this.scanningConfiguration.getServiceAnnotations();
        final Map<Class<?>, Annotation> locatedClasses = new HashMap<>();

        for (Class<?> scannedClass : scannedClasses) {
            if (scannedClass.isInterface() || scannedClass.isEnum() || scannedClass.isAnnotation()) {
                continue;
            }
            for (Annotation annotation : scannedClass.getAnnotations()) {
                if (serviceAnnotations.contains(annotation.annotationType())) {
                    locatedClasses.put(scannedClass, annotation);
                    break;
                }
            }
        }
        this.scanningConfiguration.getAdditionalClasses().forEach((cls, a) -> {
            Annotation annotation = null;
            if (a != null && cls.isAnnotationPresent(a)) {
                annotation = cls.getAnnotation(a);
            }

            locatedClasses.put(cls, annotation);
        });

        return locatedClasses;
    }

    /**
     * Scans a given class for methods that are considered beans.
     *
     * @param locatedClass the given class.
     * @return array or method references that are bean compliant.
     */
    private Method[] findBeans(Class<?> locatedClass) {
        final Set<Class<? extends Annotation>> beanAnnotations = this.scanningConfiguration.getBeanAnnotations();
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
            }
        }
        return beanMethods.toArray(Method[]::new);
    }

    private boolean isNotBean(Method beanMethod) {
        int countParameter = beanMethod.getParameterCount();
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
            if (AliasFinder.isAnnotationPresent(ctr.getDeclaredAnnotations(), Autowired.class)) {
                ctr.setAccessible(true);
                return ctr;
            }
        }
        return locatedClass.getConstructors()[0];
    }

    /**
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
            if (AliasFinder.isAnnotationPresent(method.getDeclaredAnnotations(), annotation)) {
                method.setAccessible(true);
                return method;
            }
        }
        if (locatedClass.getSuperclass() != null) {
            return this.findVoidMethodWithZeroParamsAndAnnotations(locatedClass.getSuperclass(), annotation);
        }
        return null;
    }

    private List<Field> findAutowireAnnotatedFields(Class<?> cls, List<Field> fields) {
        for (Field declaredField : cls.getDeclaredFields()) {
            if (AliasFinder.isAnnotationPresent(declaredField.getDeclaredAnnotations(), Autowired.class)) {
                declaredField.setAccessible(true);
                fields.add(declaredField);
            }
        }

        if (cls.getSuperclass() != null) {
            return this.findAutowireAnnotatedFields(cls.getSuperclass(), fields);
        }

        return fields;
    }
    /**
     * Adds the platform's default annotations for services and beans on top of the
     * ones that the client might have provided.
     */
    private void init() {
        this.scanningConfiguration.getBeanAnnotations().add(Bean.class);
        this.scanningConfiguration.getServiceAnnotations().add(Service.class);
    }
}
