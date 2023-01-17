package org.example.services;

import org.example.enums.DirectoryType;
import org.example.models.Directory;

import java.io.File;

public class DirectoryResolverImpl implements  DirectoryResolver{

    private static final String JAR_FILE_EXTENSION = ".jar";

    @Override
    public Directory resolverDirectory(Class<?> startupClass) {
        final String directory = this.getDirectory(startupClass);
        System.out.println(directory);
        return new Directory(directory, this.getDirectoryType(directory));
    }

    private String getDirectory(Class<?> cls) {
        return cls.getProtectionDomain().getCodeSource().getLocation().getFile();
    }
    private DirectoryType getDirectoryType(String directory) {
        File file = new File(directory);

        if (!file.isDirectory() && directory.endsWith(JAR_FILE_EXTENSION)) {
            return DirectoryType.JAR_FILE;
        }
        return DirectoryType.DIRECTORY;
    }
}
