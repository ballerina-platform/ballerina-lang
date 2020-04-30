package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ReleaseVersion;

public class Central implements Repo {

    @Override public ModuleId getLatestVersion(ModuleId moduleId, ReleaseVersion releaseVersion) {
        return null;
    }

    @Override public boolean isModuleExists(ModuleId moduleId) {
        return false;
    }

    @Override public void pullModule(ModuleId moduleId, Cache cache) {

    }
}
