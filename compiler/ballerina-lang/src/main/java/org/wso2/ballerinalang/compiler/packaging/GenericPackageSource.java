package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;

import java.util.List;

/**
 * Combines list of sources to a single package source.
 */
public class GenericPackageSource implements PackageSource {
    private final PackageID pkgId;
    private final List<PackageSourceEntry> sourceFiles;
    private final RepoHierarchy hierarchy;

    public GenericPackageSource(PackageID pkgId, List<PackageSourceEntry> sourceFiles, RepoHierarchy hierarchy) {
        this.pkgId = pkgId;
        this.sourceFiles = sourceFiles;
        this.hierarchy = hierarchy;
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public String getName() {
        return pkgId.getName().value;
    }

    @Override
    public PackageRepository getPackageRepository() {
        return null;
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
    public PackageSourceEntry getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<PackageSourceEntry> getPackageSourceEntries() {
        return sourceFiles;
    }

}
