package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectSourceRepo implements Repo {

    private final PathResolver resolver;

    public ProjectSourceRepo(Path projectRoot) {
        this.resolver = new PathResolver(projectRoot);
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.BAL_SANS_TEST_AND_RES);
    }

    @Override
    public Resolver getResolverInstance() {
        return resolver;
    }
}
