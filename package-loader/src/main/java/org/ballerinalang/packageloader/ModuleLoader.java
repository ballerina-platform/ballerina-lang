package org.ballerinalang.packageloader;


public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId);

    ModuleResolution resolveModule(ModuleId moduleId);
}
