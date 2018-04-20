package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.repository.CompilerInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Source file in the real file system (as opposed to in memory).
 */
public class FileSystemSourceInput implements CompilerInput {

    private final Path path;

    public FileSystemSourceInput(Path path) {
        this.path = path;
    }

    @Override
    public String getEntryName() {
        Path fileName = path.getFileName();
        return fileName == null ? path.toString() : fileName.toString();
    }

    @Override
    public byte[] getCode() {
        try {
            return Files.readAllBytes(path);
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
}
