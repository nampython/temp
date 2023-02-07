package org.example.directory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * DirectoryResolver implementation.
 * Determines the type of the directory, from which the application
 * is started.
 */
public class DirectoryResolverImpl implements DirectoryResolver {
    private static final String JAR_FILE_EXTENSION = ".jar";
    @Override
    public Directory resolveDirectory(Class<?> initClass) {
        String pathDir = this.getDirectory(initClass);
        DirectoryType dirType = this.getDirectoryType(pathDir);
        return new Directory(pathDir, dirType);
    }

    @Override
    public Directory resolveDirectory(File directory) {
        try {
            return new Directory(directory.getCanonicalPath(), this.getDirectoryType(directory.getCanonicalPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Get the root dir where the given class resides.
     *
     * @param initClass - the given class.
     */
    private String getDirectory(Class<?> initClass) {
//        return initClass
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation()
//                .getFile();
        return URLDecoder.decode(initClass.getProtectionDomain().getCodeSource().getLocation().getFile(), StandardCharsets.UTF_8);
    }

    /**
     * @param pathDir given directory.
     * @return JAR_FILE or DIRECTORY.
     */
    private DirectoryType getDirectoryType(String pathDir) {
        final File file = new File(pathDir);
        boolean isJarFIle = this.checkJarFile(file, pathDir);
        return isJarFIle ? DirectoryType.JAR_FILE : DirectoryType.DIRECTORY;
    }

    private boolean checkJarFile(File file, String pathDir) {
        return !file.isDirectory() && pathDir.endsWith(JAR_FILE_EXTENSION);
    }
}
