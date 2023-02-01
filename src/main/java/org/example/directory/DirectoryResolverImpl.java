package org.example.directory;

import java.io.File;

public class DirectoryResolverImpl implements DirectoryResolver {
    private static final String JAR_FILE_EXTENSION = ".jar";
    @Override
    public Directory resolveDirectory(Class<?> initClass) {
        String pathDir = this.getDirectory(initClass);
        DirectoryType dirType = this.getDirectoryType(pathDir);
        return new Directory(pathDir, dirType);
    }

    private String getDirectory(Class<?> initClass) {
        return initClass
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();
    }

    private DirectoryType getDirectoryType(String pathDir) {
        File file = new File(pathDir);
        boolean isJarFIle = this.checkJarFile(file, pathDir);
        return isJarFIle ? DirectoryType.JAR_FILE : DirectoryType.DIRECTORY;
    }

    private boolean checkJarFile(File file, String pathDir) {
        return !file.isDirectory() && pathDir.endsWith(JAR_FILE_EXTENSION);
    }
}
