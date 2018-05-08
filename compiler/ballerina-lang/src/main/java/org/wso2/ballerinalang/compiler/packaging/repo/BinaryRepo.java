package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Calculate path pattens within meta-inf dir of zips.
 * Used to load system org bal files.
 */
public class BinaryRepo implements Repo<Path> {

    private final ZipConverter converter;

    public BinaryRepo(Path pathToHiddenDir) {
        this.converter = new ZipConverter(pathToHiddenDir);
    }

    @Override
    public Patten calculate(PackageID pkg) {
        String orgName = pkg.getOrgName().getValue();
        String pkgName = pkg.getName().getValue();
        String version = pkg.getPackageVersion().getValue();
        String artifactName = pkgName + ".zip";
        String binaryFileName = pkgName + ".balo";
        return new Patten(path("repo", orgName, pkgName, version, artifactName, "obj", binaryFileName));
    }

    @Override
    public Converter<Path> getConverterInstance() {
        return converter;
    }

    @Override
    public String toString() {
        return "{t:'BinaryRepo', c:'" + converter + "'}";
    }

}
