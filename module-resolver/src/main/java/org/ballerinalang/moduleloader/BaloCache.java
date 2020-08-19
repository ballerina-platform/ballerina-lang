package org.ballerinalang.moduleloader;

import java.nio.file.Path;

/**
 * Balo Cache representation for Home Balo Cache and Distribution Balo Cache.
 */
public class BaloCache extends FileSystemCache {

    public BaloCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }
}
