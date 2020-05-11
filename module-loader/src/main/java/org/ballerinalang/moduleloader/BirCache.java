package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.nio.file.Path;

/**
 * BIR Cache representation for Home BIR Cache and Distribution BIR Cache.
 */
public class BirCache extends FileSystemCache {

    private Path fileSystemCachePath;

    public BirCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
        this.fileSystemCachePath = fileSystemCachePath;
    }

    @Override
    public Module getModule(ModuleId moduleId) {
        return new Module(moduleId, fileSystemCachePath);
    }
}
