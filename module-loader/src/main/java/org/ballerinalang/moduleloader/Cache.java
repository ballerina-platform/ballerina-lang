package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ReleaseVersion;

public interface Cache {

    ModuleId getLatestVersion(ModuleId moduleId, ReleaseVersion releaseVersion);

    boolean isModuleExists(ModuleId moduleId);

    Module getModule(ModuleId moduleId);
}
