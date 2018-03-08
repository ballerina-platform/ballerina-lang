package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectSourceRepoWithTests extends NonSysRepo<Path> {
    public ProjectSourceRepoWithTests(Resolver<Path> resolver) {
        super(resolver);
    }

    public ProjectSourceRepoWithTests(Path projectDir) {
        super(new PathResolver(projectDir));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_BAL_WITH_TEST);
    }
}
