package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class CacheRepo implements Repo {
    private static final String PROJECT_REPO_HIDDEN_DIR = ".ballerina";
    private final Path projectDir;

    public CacheRepo(Path projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path(pkg.getOrgName().value));
    }

    @Override
    public Resolver getResolverInstance() {
        return null;
    }
}
