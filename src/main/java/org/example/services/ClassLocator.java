package org.example.services;

import org.example.exceptions.ClassLocationException;
import org.example.models.Directory;

import java.util.Set;

public interface ClassLocator {
    public abstract Set<Class<?>> locateClasses(String directory) throws ClassLocationException;
}
