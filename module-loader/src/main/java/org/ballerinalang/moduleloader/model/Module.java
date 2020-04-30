package org.ballerinalang.moduleloader.model;

import java.nio.file.Path;

public class Module {
    ModuleId moduleId;
    Path sourcePath;

    public Module(ModuleId moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
    }
}
