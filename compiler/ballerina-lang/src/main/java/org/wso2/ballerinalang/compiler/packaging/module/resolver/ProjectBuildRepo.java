package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBuildRepoBir;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectBuildRepo extends FileSystemCache {

    public ProjectBuildRepo(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }

    @Override
    PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue(), moduleId.getName().getValue() + ".zip");
        return new PackageBuildRepoBir(moduleId, srcPath);
    }
}
