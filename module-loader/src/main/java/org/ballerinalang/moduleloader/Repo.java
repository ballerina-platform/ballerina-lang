package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;

import java.io.IOException;
import java.util.List;

public interface Repo {

    List<String> resolveVersions(ModuleId moduleId, String filter) throws IOException;

    boolean isModuleExists(ModuleId moduleId);

    /**
     * Pull module from Repo to given Cache
     *
     * @param moduleId id of the module need to be pulled
     * @param cache    module should be pulled to this cache
     */
    void pullModule(ModuleId moduleId, Cache cache);
}
