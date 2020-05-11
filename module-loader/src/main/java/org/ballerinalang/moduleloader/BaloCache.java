package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.nio.file.Path;

/**
 * Balo Cache representation for Home Balo Cache and Distribution Balo Cache.
 */
public class BaloCache extends FileSystemCache {

    private Path fileSystemCachePath;

    public BaloCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
        this.fileSystemCachePath = fileSystemCachePath;
    }

    @Override
    public Module getModule(ModuleId moduleId) {
        return new Module(moduleId, fileSystemCachePath);
    }
}
