package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;

import java.util.List;

/**
 * Combines list of sources to a single package source.
 */
public class GenericPackageSource implements PackageSource {
    private final PackageID pkgId;
    private final List<CompilerInput> sourceFiles;
    private final RepoHierarchy hierarchy;

    public GenericPackageSource(PackageID pkgId, List<CompilerInput> sourceFiles, RepoHierarchy hierarchy) {
        this.pkgId = pkgId;
        this.sourceFiles = sourceFiles;
        this.hierarchy = hierarchy;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public String getName() {
        return pkgId.getName().value;
    }

    @Override
    public PackageID getPackageId() {
        return pkgId;
    }

    @Override
    public List<String> getEntryNames() {
        return null;
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        return hierarchy;
    }

    @Override
    public CompilerInput getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<CompilerInput> getPackageSourceEntries() {
        return sourceFiles;
    }

}
