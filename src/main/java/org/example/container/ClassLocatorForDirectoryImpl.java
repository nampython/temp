package org.example.container;

import org.example.constant.Constants;
import org.example.exceptions.ClassLocationException;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ClassLocator implementation for directories.
 *
 * User recursion to scan all files in the source root directory and filters
 * those that are classes (end with ".class")
 */
public class ClassLocatorForDirectoryImpl implements ClassLocator {
    private static final String INVALID_DIRECTORY_MSG = "Invalid directory '%s'.";
    private final Set<Class<?>> locatedClasses;
    public ClassLocatorForDirectoryImpl() {
        this.locatedClasses = new HashSet<>();
    }

    /**
     * @param directory the given directory.
     * @return a set of located classes.
     */
    @Override
    public Set<Class<?>> locatedClass(String directory) {
        this.init();
        File file = new File(directory);
        File[] listFiles = Objects.requireNonNull(file.listFiles());
        String initialPackage = "";

        if (!file.isDirectory()) {
            throw new ClassLocationException(String.format(INVALID_DIRECTORY_MSG, directory));
        }
        else {
            this.processInnerFiles(listFiles, initialPackage);
        }
        return this.locatedClasses;
    }

    private void processInnerFiles(File[] listFiles, String initialPackage) {
        try {
            for (File innerFile : listFiles) {
                this.scanDir(innerFile, initialPackage);
            }
        } catch (ClassNotFoundException e) {
            throw new ClassLocationException(e.getMessage(), e);
        }
    }

    /**
     * Recursive method for listing all files in a directory.
     *
     * Starts with empty package name - ""
     * If the file is directory, for each sub file calls this method again
     * with the package name having the current file's name and a dot "." appended
     * in order to build a proper package name.
     *
     * If the file is file and its name ends with ".class" it is loaded using the
     * built package name and it is added to a set of located classes.
     *
     * @param file        the current file.
     * @param packageName the current package name.
     */
    private void scanDir(File file, String packageName) throws ClassNotFoundException {
        if (file.isDirectory()) {
            packageName += file.getName() + ".";
            File[] listFiles = Objects.requireNonNull(file.listFiles());
            for (File innerFile : listFiles) {
                this.scanDir(innerFile, packageName);
            }
        } else {
            if (!file.getName().endsWith(Constants.JAVA_BINARY_EXTENSION)) {
                return;
            }
            final String className = packageName + file
                    .getName()
                    .replace(Constants.JAVA_BINARY_EXTENSION, "");
            this.locatedClasses.add(Class.forName(className));
        }
    }

    private void init() {
        this.locatedClasses.clear();
    }
}
