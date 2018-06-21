package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.Patten.Part;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Calculate path pattens within meta-inf dir of zips.
 * Used to load system org bal files.
 */
public class BinaryRepo implements Repo<Path> {

    private final ZipConverter converter;

    public BinaryRepo(Path pathToHiddenDir) {
        this(pathToHiddenDir, Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME));
    }

    public BinaryRepo(ZipConverter converter) {
        this.converter = converter;
    }

    public BinaryRepo(Path pathToHiddenDir, Path subDir) {
        this(new ZipConverter(pathToHiddenDir.resolve(subDir)));
    }

    @Override
    public Patten calculate(PackageID pkg) {
        String orgName = pkg.getOrgName().getValue();
        String pkgName = pkg.getName().getValue();
        Part version;
        String versionStr = pkg.getPackageVersion().getValue();
        if (versionStr.isEmpty()) {
            version = LATEST_VERSION_DIR;
        } else {
            version = path(versionStr);
        }
        String artifactName = pkgName + ".zip";
        String binaryFileName = pkgName + ".balo";

        return new Patten(path(orgName, pkgName), version, path(artifactName, "obj", binaryFileName));
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
