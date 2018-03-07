package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

public interface Repo<I> {
    Patten calculate(PackageID pkg);

    Resolver<I> getResolverInstance();
}
