package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import java.nio.file.Path;

/**
 * Balo Cache representation for Home Balo Cache and Distribution Balo Cache.
 */
public class BaloCache extends FileSystemCache {

    public BaloCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }
}
