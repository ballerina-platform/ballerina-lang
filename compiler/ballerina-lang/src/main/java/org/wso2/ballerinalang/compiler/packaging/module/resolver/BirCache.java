package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import java.nio.file.Path;

/**
 * BIR Cache representation for Home BIR Cache and Distribution BIR Cache.
 */
public class BirCache extends FileSystemCache {

    public BirCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }
}
