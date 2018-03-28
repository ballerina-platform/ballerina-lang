package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.URIConverter;

import java.net.URI;

/**
 * Calculate url pattens of package.
 */
public class RemoteRepo extends NonSysRepo<URI> {

    public RemoteRepo(Converter<URI> converter) {
        super(converter);
    }

    public RemoteRepo(URI base) {
        this(new URIConverter(base));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.getPackageVersion().value;
        if (pkgVersion.isEmpty()) {
            pkgVersion = "*";
        }

        return new Patten(Patten.path(orgName, pkgName, pkgVersion));
    }

}
