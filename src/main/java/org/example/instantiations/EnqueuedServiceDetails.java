package org.example.instantiations;

import org.example.annotations.Qualifier;
import org.example.container.ServiceDetails;
import org.example.model.DependencyParam;
import org.example.model.DependencyParamCollection;
import org.example.util.AliasFinder;
import org.example.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Simple POJO class that keeps information about a service, its
 * required dependencies and the ones that are already resolved.
 */
public class EnqueuedServiceDetails {
    /**
     * Reference to the target service.
     */
    private final ServiceDetails serviceDetails;
    /**
     * List of dependencies that the target constructor of the service requires.
     */
    private final LinkedList<DependencyParam> constructorParams;
    /**
     * List of dependencies that are required from {@link Autowired} annotated fields.
     */
    private final LinkedList<DependencyParam> fieldDependencies;
//    /**
//     * Array of dependencies that the target constructor of the service requires.
//     */
//    private final Class<?>[] dependencies;
//    /**
//     * Keeps track for each dependency whether it is required
//     */
//    private final boolean[] dependenciesRequirement;
//    /**
//     * Array of instances matching the types in @dependencies.
//     */
//    private final Object[] dependencyInstances;

//    public ServiceDetails getServiceDetails() {
//        return this.serviceDetails;
//    }
//
//    public Class<?>[] getDependencies() {
//        return this.dependencies;
//    }
//
//    public Object[] getDependencyInstances() {
//        return this.dependencyInstances;
//    }
//
//    /**
//     * Array of dependencies that are required from {@link Autowired} annotated fields.
//     */
//    private final Class<?>[] fieldDependencies;
//
//    /**
//     * Array of instances matching the types in @fieldDependencies
//     */
//    private final Object[] fieldDependencyInstances;

    public EnqueuedServiceDetails(ServiceDetails serviceDetails) {
//        this.serviceDetails = serviceDetails;
//        this.dependencies = serviceDetails.getTargetConstructor().getParameterTypes();
//        this.dependenciesRequirement = new boolean[this.dependencies.length];
//        this.dependencyInstances = new Object[this.dependencies.length];
//        this.fieldDependencies = new Class[this.serviceDetails.getAutowireAnnotatedFields().length];
//        this.fieldDependencyInstances = new Object[this.serviceDetails.getAutowireAnnotatedFields().length];
//        Arrays.fill(this.dependenciesRequirement, true);
//        this.fillFieldDependencyTypes();
        this.serviceDetails = serviceDetails;
        this.constructorParams = new LinkedList<>();
        this.fieldDependencies = new LinkedList<>();
        this.fillConstructorParams();
        this.fillFieldDependencyTypes();

    }
    public ServiceDetails getServiceDetails() {
        return this.serviceDetails;
    }

    public LinkedList<DependencyParam> getConstructorParams() {
        return this.constructorParams;
    }

    public Object[] getConstructorInstances() {
        return this.constructorParams.stream()
                .map(DependencyParam::getInstance)
                .toArray(Object[]::new);
    }

    public LinkedList<DependencyParam> getFieldDependencies() {
        return this.fieldDependencies;
    }

    public Object[] getFieldInstances() {
        return this.fieldDependencies.stream()
                .map(DependencyParam::getInstance)
                .toArray(Object[]::new);
    }

    private void fillConstructorParams() {
        for (Parameter parameter : this.serviceDetails.getTargetConstructor().getParameters()) {
            this.constructorParams.add(this.createDependencyParam(
                    parameter.getType(),
                    this.getInstanceName(parameter.getDeclaredAnnotations()),
                    parameter.getDeclaredAnnotations(),
                    parameter.getParameterizedType()
            ));
        }
    }

    private void fillFieldDependencyTypes() {
        for (Field autowireAnnotatedField : this.serviceDetails.getAutowireAnnotatedFields()) {
            this.fieldDependencies.add(this.createDependencyParam(
                    autowireAnnotatedField.getType(),
                    this.getInstanceName(autowireAnnotatedField.getDeclaredAnnotations()),
                    autowireAnnotatedField.getDeclaredAnnotations(),
                    autowireAnnotatedField.getGenericType()
            ));
        }
    }
    private DependencyParam createDependencyParam(Class<?> type,
                                                  String instanceName,
                                                  Annotation[] annotations,
                                                  Type parameterizedType) {
        if (Collection.class.isAssignableFrom(type)) {
            return new DependencyParamCollection((ParameterizedType) parameterizedType, type, instanceName, annotations);
        }

        return new DependencyParam(type, instanceName, annotations);
    }
    private String getInstanceName(Annotation[] annotations) {
        final Annotation annotation = AliasFinder.getAnnotation(annotations, Qualifier.class);

        if (annotation != null) {
            return AnnotationUtils.getAnnotationValue(annotation).toString();
        }

        return null;
    }
//    private void fillFieldDependencyTypes() {
//        final Field[] autowireAnnotatedFields = this.serviceDetails.getAutowireAnnotatedFields();
//
//        for (int i = 0; i < autowireAnnotatedFields.length; i++) {
//            this.fieldDependencies[i] = autowireAnnotatedFields[i].getType();
//        }
//    }
//    public Class<?>[] getFieldDependencies() {
//        return this.fieldDependencies;
//    }
//
//    public Object[] getFieldDependencyInstances() {
//        return this.fieldDependencyInstances;
//    }
//    /**
//     * Checks if all dependencies have corresponding instances.
//     *
//     * @return true of ann dependency instances are available.
//     */
//    public boolean isResolved() {
//        for (int i = 0; i < this.dependencyInstances.length; i++) {
//            if (this.dependencyInstances[i] == null && this.dependenciesRequirement[i]) {
//                return false;
//            }
//        }
//        for (Object fieldDependencyInstance : this.fieldDependencyInstances) {
//            if (fieldDependencyInstance == null) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Checks if a given class type is present in the array of required
//     * dependencies.
//     *
//     * @param dependencyType - the given class type.
//     * @return true if the given type is present in the array of required dependencies.
//     */
//    public boolean isDependencyRequired(Class<?> dependencyType) {
//        for (Class<?> dependency : this.dependencies) {
//            if (dependency.isAssignableFrom(dependencyType)) {
//                return true;
//            }
//        }
//        for (Class<?> fieldDependency : this.fieldDependencies) {
//            if (fieldDependency.isAssignableFrom(dependencyType)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    /**
//     * Adds the object instance in the array of instantiated dependencies
//     * by keeping the exact same position as the target constructor of the service has it.
//     *
//     * @param instance the given dependency instance.
//     */
//    public void addDependencyInstance(Object instance) {
//        final Class<?> instanceType = instance.getClass();
//        for (int i = 0; i < this.dependencies.length; i++) {
//            if (dependencies[i].isAssignableFrom(instanceType)) {
//                this.dependencyInstances[i] = instance;
//                return;
//            }
//        }
//        for (int i = 0; i < this.fieldDependencies.length; i++) {
//            if (this.fieldDependencies[i].isAssignableFrom(instanceType)) {
//                this.fieldDependencyInstances[i] = instance;
//            }
//        }
//    }
//
//    public boolean isDependencyNotNull(Class<?> dependencyType) {
//        for (int i = 0; i < this.dependenciesRequirement.length; i++) {
//            if (this.dependencies[i].isAssignableFrom(dependencyType)) {
//                return this.dependenciesRequirement[i];
//            }
//        }
//
//        throw new IllegalArgumentException(String.format("Invalid dependency \"%s\".", dependencyType));
//    }
//
//    public void setDependencyNotNull(Class<?> dependencyType, boolean isRequired) {
//        for (int i = 0; i < this.dependenciesRequirement.length; i++) {
//            if (this.dependencies[i].isAssignableFrom(dependencyType)) {
//                this.dependenciesRequirement[i] = isRequired;
//                return;
//            }
//        }
//    }
    @Override
    public String toString() {
        return this.serviceDetails.getServiceType().getName();
    }
}

