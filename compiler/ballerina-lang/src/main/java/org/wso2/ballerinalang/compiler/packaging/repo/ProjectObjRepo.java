package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectObjRepo implements Repo<Path> {
    private final PathResolver resolver;

    public ProjectObjRepo(PathResolver resolver) {
        this.resolver = resolver;
    }

    public ProjectObjRepo(Path path) {
        this(new PathResolver(path));
    }

    @Override
    public Patten calculate(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path(".ballerina_project",
                                      "repo",
                                      orgName,
                                      pkgName,
                                      pkgVersion,
                                      "obj",
                                      pkgName + ".balo"));
    }

    @Override
    public Resolver<Path> getResolverInstance() {
        return resolver;
    }
}
