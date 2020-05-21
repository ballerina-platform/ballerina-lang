package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.io.IOException;

public interface ModuleLoader {

    ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException;

    Module resolveModule(ModuleId moduleId);
}
