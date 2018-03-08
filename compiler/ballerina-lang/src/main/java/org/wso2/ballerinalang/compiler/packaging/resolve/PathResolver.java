package org.wso2.ballerinalang.compiler.packaging.resolve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class PathResolver implements Resolver<Path> {

    private final Path root;

    public PathResolver(Path root) {
        this.root = root;
    }

    private static boolean isBal(Path path, BasicFileAttributes attributes) {
        return !(attributes.isDirectory() || attributes.isOther())
                && path.getFileName().toString().endsWith(".bal");
    }

    @Override
    public Path combine(Path path, String pathPart) {
        return path.resolve(pathPart);
    }

    @Override
    public Stream<Path> expand(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<Path> expandBal(Path path) {
        if (Files.exists(path) && Files.isDirectory(path)) {
            try {
                return Files.find(path, Integer.MAX_VALUE, PathResolver::isBal);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Stream.of();
        }
    }

    @Override
    public Path start() {
        return root;
    }

    @Override
    public Stream<Path> finalize(Path path) {
        return Stream.of(path);
    }
}
