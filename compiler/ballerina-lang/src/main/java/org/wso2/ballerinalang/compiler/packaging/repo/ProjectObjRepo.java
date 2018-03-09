package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Converter;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathConverter;

import java.nio.file.Path;

/**
 * Calculate balo path pattens in project.
 */
public class ProjectObjRepo extends NonSysRepo<Path> {
    public ProjectObjRepo(Converter<Path> converter) {
        super(converter);
    }

    public ProjectObjRepo(Path path) {
        this(new PathConverter(path));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {

        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.version.value;

        return new Patten(Patten.path(".ballerina_project",
                                      "repo",
                                      orgName,
                                      pkgName,
                                      pkgVersion,
                                      "obj",
                                      pkgName + ".balo"));
    }

}
