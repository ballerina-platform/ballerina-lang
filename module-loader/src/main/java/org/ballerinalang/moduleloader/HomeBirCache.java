package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ReleaseVersion;

public class HomeBirCache implements Cache {
    @Override public ModuleId getLatestVersion(ModuleId moduleId, ReleaseVersion releaseVersion) {
        return null;
    }

    @Override public boolean isModuleExists(ModuleId moduleId) {
        return false;
    }

    @Override public Module getModule(ModuleId moduleId) {
        return null;
    }
}
