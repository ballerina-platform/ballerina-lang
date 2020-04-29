package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;

public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId);

    ModuleResolution resolveModule(ModuleId moduleId);
}
