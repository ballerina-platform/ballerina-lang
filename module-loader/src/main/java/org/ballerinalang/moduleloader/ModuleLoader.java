package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;

import java.io.IOException;

public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException;

    ModuleResolution resolveModule(ModuleId moduleId);
}
