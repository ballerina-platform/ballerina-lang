package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.URIConverter;

/**
 * Calculate url pattens of package.
 */
public class RemoteRepo extends NonSysRepo<StringBuilder> {
    public RemoteRepo(Converter<StringBuilder> converter) {
        super(converter);
    }

    public RemoteRepo(String baseURL) {
        this(new URIConverter());
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path("repo", orgName, pkgName, pkgVersion, "src"),
                          Patten.WILDCARD_SOURCE);
    }

}
