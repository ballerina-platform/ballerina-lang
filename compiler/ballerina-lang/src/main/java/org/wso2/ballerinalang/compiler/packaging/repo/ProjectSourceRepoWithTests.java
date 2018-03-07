package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class ProjectSourceRepoWithTests implements Repo {
    private final Path projectRoot;

    public ProjectSourceRepoWithTests(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten();
    }

    @Override
    public Resolver getResolverInstance() {
        return null;
    }
}
