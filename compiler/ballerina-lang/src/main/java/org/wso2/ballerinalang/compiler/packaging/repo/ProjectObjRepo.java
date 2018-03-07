package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectObjRepo implements Repo<Path> {
    private final Path path;

    public ProjectObjRepo(Path path) {
        this.path = path;
    }

    @Override
    public Patten calculate(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path(".ballerina",
                                      "repo",
                                      orgName,
                                      pkgName,
                                      pkgVersion,
                                      pkgName + ".balo"));
    }

    @Override
    public Resolver<Path> getResolverInstance() {
        return new PathResolver(path);
    }
}
