package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;

import java.nio.file.Path;
import java.util.List;

public class PathBalo extends Cache {
    Path baloPath;

    public PathBalo(Path baloPath) {
        this.baloPath = baloPath;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        return null;
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        return false;
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
