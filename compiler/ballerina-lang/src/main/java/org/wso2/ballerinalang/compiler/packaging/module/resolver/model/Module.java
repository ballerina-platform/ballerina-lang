package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;

import java.nio.file.Path;

public class Module {
    private PackageID moduleId;
    private Path sourcePath;

    public Module(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
    }

    public PackageID getModuleId() {
        return moduleId;
    }

    public void setModuleId(PackageID moduleId) {
        this.moduleId = moduleId;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }
}
