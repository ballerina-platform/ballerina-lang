package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;

public class HomeRepo implements Repo {
    public HomeRepo(Path projectRoot) {
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return null;
    }

    @Override
    public Resolver getResolverInstance() {
        return null;
    }
}
