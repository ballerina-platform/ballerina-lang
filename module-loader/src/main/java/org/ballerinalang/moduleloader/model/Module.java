package org.ballerinalang.moduleloader.model;

import java.nio.file.Path;

public class Module {
    private ModuleId moduleId;
    private Path sourcePath;

    public Module(ModuleId moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
    }

    public ModuleId getModuleId() {
        return moduleId;
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }
}
