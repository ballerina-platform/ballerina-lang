package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;

/**
 * Calculate bala path pattens in project.
 */
public class ObjRepo extends NonSysRepo<Path> {
    public ObjRepo(Converter<Path> converter) {
        super(converter);
    }

    public ObjRepo(Path path) {
        this(new PathConverter(path));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {

        String orgName = pkg.getOrgName().getValue();
        String pkgName = pkg.getName().getValue();
        String pkgVersion = pkg.version.getValue();

        return new Patten(Patten.path("repo",
                                      orgName,
                                      pkgName,
                                      pkgVersion,
                                      "obj",
                                      pkgName + ".bala"));
    }

}
