package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.URIResolver;

public class RemoteRepo extends NonSysRepo<StringBuilder> {
    public RemoteRepo(Resolver<StringBuilder> resolver) {
        super(resolver);
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path("repo", orgName, pkgName, pkgVersion, "src"),
                          Patten.WILDCARD_BAL);
    }

    public RemoteRepo(String baseURL) {
        this(new URIResolver());
    }

}
