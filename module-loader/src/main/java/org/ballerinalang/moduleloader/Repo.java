package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ReleaseVersion;

public interface Repo {

    ModuleId getLatestVersion(ModuleId moduleId, ReleaseVersion releaseVersion);

    boolean isModuleExists(ModuleId moduleId);

    /**
     * Pull module from Repo to given Cache
     *
     * @param moduleId id of the module need to be pulled
     * @param cache    module should be pulled to this cache
     */
    void pullModule(ModuleId moduleId, Cache cache);
}
