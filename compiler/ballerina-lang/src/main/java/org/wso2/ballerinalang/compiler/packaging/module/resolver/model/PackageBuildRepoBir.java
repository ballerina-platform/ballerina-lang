package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageBinary;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;

/**
 * Concrete package entry for BIR file.
 */
public class PackageBuildRepoBir implements PackageBinary {
    private final PackageID moduleId;
    private final CompilerInput birContent;
    private final Path sourcePath;

    public PackageBuildRepoBir(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.birContent = getBirContent();
    }

    private CompilerInput getBirContent() {
        try (FileSystem zipFileSystem = FileSystems
                .newFileSystem(URI.create("jar:file:" + new File(String.valueOf(this.sourcePath)).getAbsolutePath()),
                        new HashMap<>())) {
            Path birPath = zipFileSystem.getPath("bir", moduleId.getName().getValue() + BLANG_COMPILED_PKG_BIR_EXT);
            byte[] code = Files.readAllBytes(birPath);
            return new CompilerInputImpl(code, birPath);
        } catch (IOException e) {
            throw new ModuleResolveException("reading bir from build repo failed");
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
