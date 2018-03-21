package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Calculate path pattens within meta-inf dir of jars (or exploded jars).
 * Used to load system org bal files.
 */
public class JarRepo implements Repo<Path> {
    private final ZipConverter converter;

    public JarRepo(URI jarLocation) {
        this.converter = new ZipConverter(Paths.get(jarLocation));
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path("META-INF"),
                          Patten.path(pkg.orgName.value, pkg.name.value),
                          Patten.WILDCARD_SOURCE);
    }

    @Override
    public Converter<Path> getConverterInstance() {
        return converter;
    }

    @Override
    public String toString() {
        return "{t:'JarRepo', c:'" + converter + "'}";
    }

}
