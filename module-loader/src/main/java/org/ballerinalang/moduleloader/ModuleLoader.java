package org.ballerinalang.moduleloader;


public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId);

    ModuleResolution resolveModule(ModuleId moduleId);
}
