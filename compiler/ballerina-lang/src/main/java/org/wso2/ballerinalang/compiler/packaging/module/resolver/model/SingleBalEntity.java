package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Concrete package entry for single bal file.
 */
public class SingleBalEntity implements PackageSource {
    private final PackageID moduleId;
    private final List<CompilerInput> sourceFiles;
    private final Path sourcePath;
    private final Path moduleRoot;

    public SingleBalEntity(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.moduleRoot = sourcePath;
        this.sourcePath = sourcePath.resolve(moduleId.sourceFileName.getValue()); //bal file name append to module root
        this.sourceFiles = Collections.singletonList(getSourceFile());
    }

    private CompilerInput getSourceFile() {
        try {
            return new CompilerInputImpl(Files.readAllBytes(this.sourcePath), this.sourcePath, this.moduleRoot);
        } catch (IOException e) {
            throw new ModuleResolveException("reading source file failed: " + this.sourcePath);
        }
    }

    @Override
    public PackageID getPackageId() {
        return this.moduleId;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public String getName() {
        return this.moduleId.getName().getValue();
    }

    @Override
    public List<String> getEntryNames() {
        return null;
    }

    @Override
    public CompilerInput getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<CompilerInput> getPackageSourceEntries() {
        return this.sourceFiles;
    }
}
