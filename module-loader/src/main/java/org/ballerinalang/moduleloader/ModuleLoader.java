package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;

public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId);

    ModuleResolution resolveModule(ModuleId moduleId);
}
