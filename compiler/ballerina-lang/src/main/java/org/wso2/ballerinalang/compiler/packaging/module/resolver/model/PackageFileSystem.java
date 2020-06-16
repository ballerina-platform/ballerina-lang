package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * Concrete package entry for project module.
 */
public class PackageFileSystem implements PackageSource {
    private final PackageID moduleId;
    private final List<CompilerInput> sourceFiles;
    private final Path sourcePath;

    public PackageFileSystem(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.sourceFiles = getSourceFiles();
    }

    private List<CompilerInput> getSourceFiles() {
        List<CompilerInput> compilerInputs = new ArrayList<>();
        File[] listOfFiles = new File(String.valueOf(this.sourcePath)).listFiles();
        if (listOfFiles != null && listOfFiles.length > 0) {
            traverseDirectory(listOfFiles, compilerInputs);
        }
        return compilerInputs;
    }

    private void traverseDirectory(File[] files, List<CompilerInput> compilerInputs) {
        for (File file : files) {
            if (file.isDirectory()) {
                File[] listOfFiles = file.listFiles();
                if (listOfFiles != null && listOfFiles.length > 0) {
                    traverseDirectory(listOfFiles, compilerInputs);
                }
            } else if (file.getPath().endsWith(BLANG_SOURCE_EXT)) {
                try {
                    compilerInputs.add(new CompilerInputImpl(Files.readAllBytes(file.toPath()), file.toPath(),
                            this.sourcePath));
                } catch (IOException e) {
                    throw new ModuleResolveException("reading module source files failed");
                }
            }
        }
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
}
