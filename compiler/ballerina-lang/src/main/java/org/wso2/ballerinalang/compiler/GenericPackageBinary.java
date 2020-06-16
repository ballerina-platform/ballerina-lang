package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageBinary;

/**
 * Concrete package entry for binary (balo) file.
 */
public class GenericPackageBinary implements PackageBinary {

    private final PackageID pkgId;
    private final CompilerInput binaryContent;
    private final Kind kind;

    public GenericPackageBinary(PackageID pkgId, CompilerInput binaryContent, Kind kind) {
        this.pkgId = pkgId;
        this.binaryContent = binaryContent;
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
    public CompilerInput getCompilerInput() {
        return binaryContent;
    }
}
