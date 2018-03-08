package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

public abstract class NonSysRepo<I> implements Repo<I> {
    private final Resolver<I> resolver;

    public NonSysRepo(Resolver<I> resolver) {
        this.resolver = resolver;
    }

    @Override
    public Patten calculate(PackageID pkg) {
        // TODO: remove pkg name check, only org should be checked.
        if ("ballerina".equals(pkg.getOrgName().getValue()) ||
                pkg.getName().getValue().startsWith("ballerina.")) {
            return Patten.NULL;
        } else {
            return calculateNonSysPkg(pkg);
        }
    }

    @Override
    public Resolver<I> getResolverInstance() {
        return resolver;
    }

    public abstract Patten calculateNonSysPkg(PackageID pkg);
}
