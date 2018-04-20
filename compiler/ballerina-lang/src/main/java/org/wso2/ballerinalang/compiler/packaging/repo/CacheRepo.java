package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.WILDCARD_SOURCE;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Calculate path pattens for project and home cache.
 */
public class CacheRepo extends NonSysRepo<Path> {

    private final String cacheName;

    public CacheRepo(Converter<Path> converter, String cacheName) {
        super(converter);
        this.cacheName = cacheName;
    }

    public CacheRepo(Path path, String cacheName) {
        this(new ZipConverter(path), cacheName);
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;
        if (pkgVersion.isEmpty()) {
            return new Patten(path(ProjectDirConstants.CACHES_DIR_NAME, cacheName, orgName, pkgName),
                              LATEST_VERSION_DIR, path(pkgName + ".zip"), path("src"), WILDCARD_SOURCE);
        } else {
            return new Patten(path(ProjectDirConstants.CACHES_DIR_NAME, cacheName, orgName, pkgName, pkgVersion),
                              path(pkgName + ".zip"), path("src"), WILDCARD_SOURCE);
        }
    }
}
