package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * Concrete package entry for Balo source.
 */
public class PackageBalo implements PackageSource {
    private final PackageID moduleId;
    private final List<CompilerInput> sourceFiles;
    private final Path sourcePath;

    public PackageBalo(PackageID moduleId, Path sourcePath) {
        // If sourcePath not exists return null
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.sourceFiles = getSourceFiles();
    }

    private List<CompilerInput> getSourceFiles() {
        List<CompilerInput> compilerInputs = new ArrayList<>();

        URI balo = URI.create("jar:" + this.sourcePath.toUri());
        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(balo, env)) {
            for (Path rootDirectory : zipFileSystem.getRootDirectories()) {
                Files.walk(rootDirectory).forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString().endsWith(BLANG_SOURCE_EXT)) {
                        try {
                            byte[] code = Files.readAllBytes(path);
                            CompilerInput compilerInput = new CompilerInputImpl(code, path);
                            compilerInputs.add(compilerInput);
                        } catch (IOException e) {
                            throw new ModuleResolveException("reading balo source files failed");
                        }
                    }
                });
            }
        } catch (IOException e) {
            throw new ModuleResolveException("reading balo failed");

        }
        return compilerInputs;
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

    public Path getSourcePath() {
        return this.sourcePath;
    }
}
