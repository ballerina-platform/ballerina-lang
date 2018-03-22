package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;

import java.nio.file.Path;

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
        return new Patten(Patten.path("repo", pkg.getOrgName().getValue(), pkg.getName().getValue(),
                pkg.getPackageVersion().getValue()), Patten.path(pkg.getName().value + ".zip"), Patten.path("src"),
                Patten.WILDCARD_SOURCE);
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
