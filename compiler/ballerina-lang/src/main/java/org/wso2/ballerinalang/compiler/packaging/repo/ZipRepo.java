package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Calculate path pattens within meta-inf dir of zips.
 * Used to load system org bal files.
 */
public class ZipRepo implements Repo<Path> {
    private final PathConverter converter;

    public ZipRepo(URI zipLocation) {
        Path root = Paths.get(zipLocation);
        this.converter = new ZipConverter(root);
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path("repos", "$current"),
                Patten.path(pkg.getName().value + ".zip"), Patten.path("src"),
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
