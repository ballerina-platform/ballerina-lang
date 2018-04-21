package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.WILDCARD_SOURCE;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Calculate path pattens within meta-inf dir of zips.
 * Used to load system org bal files.
 */
public class ZipRepo implements Repo<Path> {
    private final ZipConverter converter;

    public ZipRepo(Path pathToHiddenDir) {
        this.converter = new ZipConverter(pathToHiddenDir);
    }

    @Override
    public Patten calculate(PackageID pkg) {
        String orgName = pkg.getOrgName().getValue();
        String pkgName = pkg.getName().getValue();
        String version = pkg.getPackageVersion().getValue();
        if (version.isEmpty()) {
            return new Patten(path("repo", orgName, pkgName), LATEST_VERSION_DIR,
                              path(pkgName + ".zip"), path("src"), WILDCARD_SOURCE);
        } else {
            return new Patten(path("repo", orgName, pkgName, version),
                              path(pkgName + ".zip"), path("src"), WILDCARD_SOURCE);
        }
    }

    @Override
    public Converter<Path> getConverterInstance() {
        return converter;
    }

    @Override
    public String toString() {
        return "{t:'ZipRepo', c:'" + converter + "'}";
    }

}
