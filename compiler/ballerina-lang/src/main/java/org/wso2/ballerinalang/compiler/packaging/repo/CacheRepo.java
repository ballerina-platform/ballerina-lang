package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class CacheRepo extends NonSysRepo<Path> {

    public CacheRepo(Resolver<Path> resolver) {
        super(resolver);
    }

    public CacheRepo(Path path) {
        this(new PathResolver(path));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path("caches"),
                          Patten.WILDCARD_DIR,
                          Patten.path(orgName, pkgName, pkgVersion, "src"),
                          Patten.WILDCARD_BAL);
    }
}
