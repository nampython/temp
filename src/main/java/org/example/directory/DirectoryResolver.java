package org.example.directory;

import java.io.File;

public interface DirectoryResolver {
    Directory resolveDirectory(Class<?> initClass);
    Directory resolveDirectory(File directory);
}
