package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;

import java.nio.file.Path;

public class ProjectSourceRepo extends NonSysRepo<Path> {

    public ProjectSourceRepo(PathResolver resolver) {
        super(resolver);
    }

    public ProjectSourceRepo(Path projectRoot) {
        this(new PathResolver(projectRoot));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_BAL);
    }
}
