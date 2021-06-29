package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;

/**
 * Calculate bal files' path pattens in a project, including test files.
 */
public class ProjectSourceRepoWithTests extends NonSysRepo<Path> {
    public ProjectSourceRepoWithTests(Converter<Path> converter) {
        super(converter);
    }

    public ProjectSourceRepoWithTests(Path projectDir) {
        this(new PathConverter(projectDir));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_SOURCE_WITH_TEST);
    }
}
