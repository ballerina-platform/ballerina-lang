package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;

/**
 * Calculate path pattens for project and home cache.
 */
public class CacheRepo extends NonSysRepo<Path> {

    public CacheRepo(Converter<Path> converter) {
        super(converter);
    }

    public CacheRepo(Path path) {
        this(new PathConverter(path));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path("caches"),
                          Patten.LATEST_VERSION_DIR,
                          Patten.path(orgName, pkgName, pkgVersion, "src"),
                          Patten.WILDCARD_SOURCE);
    }
}
