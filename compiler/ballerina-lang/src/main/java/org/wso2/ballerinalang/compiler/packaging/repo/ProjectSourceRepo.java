package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectSourceRepo implements Repo<Path> {

    private final PathResolver resolver;

    public ProjectSourceRepo(PathResolver resolver) {
        this.resolver = resolver;
    }

    public ProjectSourceRepo(Path projectRoot) {
        this(new PathResolver(projectRoot));
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_BAL);
    }

    @Override
    public Resolver<Path> getResolverInstance() {
        return resolver;
    }
}
