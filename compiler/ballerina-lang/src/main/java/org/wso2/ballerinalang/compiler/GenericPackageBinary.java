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

    public GenericPackageBinary(PackageID pkgId, CompilerInput binaryContent, RepoHierarchy repoHierarchy) {
        this.pkgId = pkgId;
        this.binaryContent = binaryContent;
        this.repoHierarchy = repoHierarchy;
    }

    @Override
    public PackageID getPackageId() {
        return pkgId;
    }

    @Override
    public Kind getKind() {
        return Kind.COMPILED;
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
