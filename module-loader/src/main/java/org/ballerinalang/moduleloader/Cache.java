package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.util.List;

public abstract class Cache implements Repo {

    public abstract List<String> resolveVersions(ModuleId moduleId, String filter);

    // if exact version is there, use this method
    public abstract boolean isModuleExists(ModuleId moduleId);

    public void pullModule(ModuleId moduleId, Cache cache) {
        throw new UnsupportedOperationException("pullModule is not supported in Cache implementation");
    }

    abstract Module getModule(ModuleId moduleId);
}
