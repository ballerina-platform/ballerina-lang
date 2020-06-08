package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageBinary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Concrete package entry for BIR file.
 */
public class PackageBir implements PackageBinary {
    private final PackageID moduleId;
    private final CompilerInput birContent;
    private final Path sourcePath;

    public PackageBir(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.birContent = getBirContent();
    }

    private CompilerInput getBirContent() {
        try {
            return new CompilerInputImpl(Files.readAllBytes(this.sourcePath), this.sourcePath);
        } catch (IOException e) {
            throw new ModuleResolveException("reading bir failed");
        }
    }

    @Override
    public CompilerInput getCompilerInput() {
        return this.birContent;
    }

    @Override
    public PackageID getPackageId() {
        return this.moduleId;
    }

    @Override
    public Kind getKind() {
        return Kind.COMPILED_BIR;
    }

    @Override
    public String getName() {
        return moduleId.getName().getValue();
    }
}
