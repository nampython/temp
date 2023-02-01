package org.example.container;

import java.util.Set;

public interface ClassLocator {
    Set<Class<?>>locatedClass(String directory);
}
