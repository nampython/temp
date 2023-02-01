package org.example.directory;

public class Directory {
    private String directory;
    private DirectoryType directoryType;
    public Directory(String directory, DirectoryType directoryType) {
        this.directory = directory;
        this.directoryType = directoryType;
    }

    public String getDirectory() {
        return this.directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public DirectoryType getDirectoryType() {
        return this.directoryType;
    }
    public void setDirectoryType(DirectoryType directoryType) {
        this.directoryType = directoryType;
    }
}
