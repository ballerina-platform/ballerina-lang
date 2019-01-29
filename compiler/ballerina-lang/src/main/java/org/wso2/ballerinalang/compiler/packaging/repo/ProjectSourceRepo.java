package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;

/**
 * Calculate bal files' path pattens in a project, excluding test files.
 */
public class ProjectSourceRepo extends NonSysRepo<Path> {

    private final boolean testEnabled;

    public ProjectSourceRepo(Converter<Path> converter, boolean testEnabled) {
        super(converter);
        this.testEnabled = testEnabled;
    }

    public ProjectSourceRepo(Path projectRoot, boolean testEnabled) {
        this(new PathConverter(projectRoot), testEnabled);
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        if (testEnabled) {
            return new Patten(Patten.path(pkg.getName().value),
                              Patten.WILDCARD_SOURCE_WITH_TEST);
        }
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_SOURCE);
    }
}
