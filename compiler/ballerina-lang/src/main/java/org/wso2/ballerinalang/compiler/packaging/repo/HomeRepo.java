package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class HomeRepo extends NonSysRepo<Path> {
    public HomeRepo(Resolver<Path> resolver) {
        super(resolver);
    }

    public HomeRepo(Path path) {
        super(new PathResolver(path));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path("repo", orgName, pkgName, pkgVersion, "src"),
                          Patten.WILDCARD_BAL);
    }
}
