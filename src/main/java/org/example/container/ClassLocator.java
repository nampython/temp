package org.example.container;

import java.util.Set;

/**
 * Used to locating classes in the application context.
 */
public interface ClassLocator {
    Set<Class<?>>locatedClass(String directory);
}
