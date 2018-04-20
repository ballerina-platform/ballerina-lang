package org.wso2.ballerinalang.compiler.packaging.converters;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters directories.
 */
class FilterSearch extends SimpleFileVisitor {

    private final Path excludeDir;
    private List<Path> pathList = new ArrayList<>();

    FilterSearch(Path exclude) {
        this.excludeDir = exclude;
    }

    @Override
    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
        if (!isExcluded((Path) dir)) {
            return FileVisitResult.CONTINUE;
        } else {
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    @Override
    public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
        if (isBal((Path) file, Files.readAttributes((Path) file, BasicFileAttributes.class))) {
            pathList.add((Path) file);
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Exclude the given directory when traversing through the directories.
     *
     * @param path directory path to be traversed
     * @return if the directory is to be excluded or not
     * @throws IOException exception
     */
    private boolean isExcluded(Path path) throws IOException {
        Path name = path.getFileName();
        return (name != null) && name.equals(excludeDir);
    }

    /**
     * Checks if the file is a bal file.
     *
     * @param path       file path
     * @param attributes basic file attributes
     * @return if a file is a bal file or not
     */
    private boolean isBal(Path path, BasicFileAttributes attributes) {
        Path fileName = path.getFileName();
        return attributes.isRegularFile() && fileName != null && fileName.toString().endsWith(".bal");
    }

    /**
     * Get all paths of files inside the directory.
     *
     * @return list of paths
     */
    public List<Path> getPathList() {
        return pathList;
    }
}
