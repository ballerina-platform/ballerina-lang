package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;

/**
 * Calculate path pattens for project and home cache.
 */
public class CacheRepo extends BinaryRepo {

    public CacheRepo(Path path, String cacheName, CompilerPhase compilerPhase) {
        super(path, Path.of(ProjectDirConstants.CACHES_DIR_NAME, cacheName), compilerPhase);
    }
}
