package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Source file in the real file system (as opposed to in memory).
 */
public class FileSystemSourceEntry implements PackageSourceEntry {

    private final Path path;
    private PackageID pkgId;

    public FileSystemSourceEntry(Path path, PackageID pkgId) {
        this.path = path;
        this.pkgId = pkgId;
    }

    @Override
    public PackageID getPackageID() {
        return this.pkgId;
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

    public void setPkgId(PackageID pkgId) {
        this.pkgId = pkgId;
    }
    
    public Path getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return this.path.toString();
    }
}
