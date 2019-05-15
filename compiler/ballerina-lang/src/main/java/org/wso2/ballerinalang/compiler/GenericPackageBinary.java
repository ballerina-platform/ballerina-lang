package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageBinary;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

/**
 * Concrete package entry for binary (balo) file.
 */
public class GenericPackageBinary implements PackageBinary {

    private final PackageID pkgId;
    private final CompilerInput binaryContent;
    private final RepoHierarchy repoHierarchy;
    private final Kind kind;

    public GenericPackageBinary(PackageID pkgId, CompilerInput binaryContent, RepoHierarchy repoHierarchy, Kind kind) {
        this.pkgId = pkgId;
        this.binaryContent = binaryContent;
        this.repoHierarchy = repoHierarchy;
        this.kind = kind;
    }

    @Override
    public PackageID getPackageId() {
        return pkgId;
    }

    @Override
    public Kind getKind() {
        return this.kind;
    }

    @Override
    public String getName() {
        return pkgId.getName().value;
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        return repoHierarchy;
    }

    @Override
    public CompilerInput getCompilerInput() {
        return binaryContent;
    }
}
