package org.example.container;

import java.util.Set;

public class ClassLocatorForJarFile implements ClassLocator {
    @Override
    public Set<Class<?>> locatedClass(String directory) {
        //TODO: We need to find all of the located class if we run from jar file.
        return null;
    }
}
