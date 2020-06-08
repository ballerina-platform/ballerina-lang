package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.repository.CompilerInput;

import java.io.File;
import java.nio.file.Path;

public class CompilerInputImpl implements CompilerInput {

    private byte[] code;
    private final Path path;
    private Path packageRoot;

    public CompilerInputImpl(byte[] code, Path path) {
        this.code = code;
        this.path = path;
    }

    public CompilerInputImpl(byte[] code, Path path, Path packageRoot) {
        this.code = code;
        this.path = path;
        this.packageRoot = packageRoot;
    }

    @Override
    public String getEntryName() {
        // We need to return the file path relative to the package root.
        // This is to distinguish files with the same name but in different folders.
        String entryName;
        if (packageRoot != null) {
            File pkgRoot = new File(packageRoot.toString());
            File file = new File(path.toString());
            // Find the file path relative to the package root.
            entryName = pkgRoot.toURI().relativize(file.toURI()).getPath();
        } else {
            entryName = path.toString();
        }
        return entryName;
    }

    @Override
    public byte[] getCode() {
        return this.code;
    }

    public Path getPath() {
        return this.path;
    }
}
