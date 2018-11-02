package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.repository.CompilerInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Source file in the real file system (as opposed to in memory).
 */
public class FileSystemSourceInput implements CompilerInput {

    private final Path path;
    private Path packageRoot;

    public FileSystemSourceInput(Path path) {
        this.path = path;
    }

    public FileSystemSourceInput(Path filePath, Path packageRoot) {
        this.path = filePath;
        this.packageRoot = packageRoot;
    }

    @Override
    public String getEntryName() {
        // We need to return the file path relative to the package root.
        // This is to distinguish files with the same name but in different folders.
        if (packageRoot != null) {
            File pkgRoot = new File(packageRoot.toString());
            File file = new File(path.toString());
            // Find the file path relative to the package root.
            return pkgRoot.toURI().relativize(file.toURI()).getPath();
        }
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : path.toString();
    }

    @Override
    public byte[] getCode() {
        try {
            byte[] code = Files.readAllBytes(path);
            if (isBLangBinaryFile(path)) {
                path.getFileSystem().close();
            }
            return code;
        } catch (IOException e) {
            throw new BLangCompilerException("Error reading source file " + path);
        }
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

    private boolean isBLangBinaryFile(Path path) {
        return path.toString().endsWith(BLANG_COMPILED_PKG_BINARY_EXT);
    }
}
