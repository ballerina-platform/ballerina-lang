package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Calculate bal files' path pattens in a project, excluding test files.
 */
public class ProgramingSourceRepo extends NonSysRepo<Path> {
    public ProgramingSourceRepo(Converter<Path> converter) {
        super(converter);
    }

    public ProgramingSourceRepo(Path programingDir) {
        this(new PathConverter(programingDir));

    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        if (pkg.isUnnamed) {
            return new Patten(path(pkg.sourceFileName.value));
        } else {
            return Patten.NULL;
        }
    }
}

