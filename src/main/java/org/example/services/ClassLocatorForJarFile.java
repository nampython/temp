package org.example.services;

import org.example.constants.Constants;
import org.example.exceptions.ClassLocationException;
import org.example.models.Directory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLocatorForJarFile implements ClassLocator{

    final Set<Class<?>> locatedClass = new HashSet<>();

    @Override
    public Set<Class<?>> locateClasses(String directory) throws ClassLocationException {

        try {
            JarFile jarFile = new JarFile(new File(directory));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                if(!jarEntry.getName().endsWith(Constants.JAVA_BINARY_EXTENSION)) {
                    continue;
                }
                final String name = jarEntry.getName()
                        .replace(Constants.JAVA_BINARY_EXTENSION, "")
                        .replaceAll("\\\\", "")
                        .replaceAll("/", ".");
                locatedClass.add(Class.forName(name));
            }

        } catch (IOException | ClassLocationException e) {
            throw new ClassLocationException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return locatedClass;
    }
}
