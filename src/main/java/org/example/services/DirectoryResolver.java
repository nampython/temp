package org.example.services;

import org.example.models.Directory;

public interface DirectoryResolver {
    public abstract  Directory resolverDirectory(Class<?> startupClass);
}
