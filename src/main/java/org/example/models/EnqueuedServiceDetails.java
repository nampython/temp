package org.example.models;

public class EnqueuedServiceDetails {
    private final ServiceDetails<?> serviceDetails;
    private final Class<?>[] dependencies;
    private final Object[] dependencyInstances;

    public EnqueuedServiceDetails(ServiceDetails<?> serviceDetails) {
        this.serviceDetails = serviceDetails;
        this.dependencies = serviceDetails.getTargetConstructor().getParameterTypes();
        this.dependencyInstances = new Object[this.dependencies.length];
    }

    public ServiceDetails<?> getServiceDetails() {
        return serviceDetails;
    }

    public Class<?>[] getDependencies() {
        return dependencies;
    }

    public Object[] getDependencyInstances() {
        return dependencyInstances;
    }


    public void addDependencyInstances(Object instance) {
        for (int i = 0; i < this.dependencies.length; i++) {
            if (this.dependencies[i].isAssignableFrom(instance.getClass())) {
                this.dependencyInstances[i] = instance;
                return;
            }
        }
    }

    public boolean isResolved() {
        for (Object dependencyInstance : this.dependencyInstances) {
            if (dependencyInstance == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isDependencyRequired(Class<?> dependencyType) {
        for (Class<?> dependency : this.dependencies) {
            if (dependency.isAssignableFrom(dependencyType)) {
                return true;
            }
        }
        return false;
    }
}
