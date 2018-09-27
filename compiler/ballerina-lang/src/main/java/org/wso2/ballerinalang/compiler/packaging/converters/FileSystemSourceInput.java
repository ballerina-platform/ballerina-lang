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
    private Path rootPath;

    @Deprecated
    public FileSystemSourceInput(Path path) {
        this.path = path;
    }

    public FileSystemSourceInput(Path filePath, Path rootPath) {
        this.path = filePath;
        this.rootPath = rootPath;
    }

    @Override
    public String getEntryName() {
        Path fileName = path.getFileName();
        return rootPath != null ?
                new File(rootPath.toString()).toURI().relativize(new File(path.toString()).toURI()).getPath() :
                (fileName != null ? fileName.toString() : path.toString());
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
