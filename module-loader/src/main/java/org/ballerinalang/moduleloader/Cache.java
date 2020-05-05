package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.util.List;

public interface Cache {

    List<String> resolveVersions(ModuleId moduleId, String filter);

    // if exact version is there, use this method
    boolean isModuleExists(ModuleId moduleId);

    Module getModule(ModuleId moduleId);
}
