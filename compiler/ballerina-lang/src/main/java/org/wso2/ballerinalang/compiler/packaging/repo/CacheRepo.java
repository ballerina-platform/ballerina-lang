package org.wso2.ballerinalang.compiler.packaging.repo;

import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Calculate path pattens for project and home cache.
 */
public class CacheRepo extends BinaryRepo {

    public CacheRepo(Path path, String cacheName) {
        super(path, Paths.get(ProjectDirConstants.CACHES_DIR_NAME, cacheName));
    }
}
