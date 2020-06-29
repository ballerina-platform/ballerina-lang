package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;

import java.nio.file.Path;
import java.util.List;

/**
 * Repo to get module from path balos.
 */
public class PathBalo extends Cache {
    private Path baloPath;

    PathBalo(Path baloPath) {
        this.baloPath = baloPath;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        throw new  UnsupportedOperationException();
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        throw new  UnsupportedOperationException();
    }

    @Override
    PackageEntity getModule(PackageID moduleId) {
        if (this.baloPath.toFile().exists()) {
            return new PackageBalo(moduleId, baloPath);
        } else {
            return null;
        }
    }
}
