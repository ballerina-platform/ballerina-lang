package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Balo Cache representation for Home Balo Cache and Distribution Balo Cache.
 */
public class BaloCache extends FileSystemCache {

    public BaloCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }

    @Override
    PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        return new PackageBalo(moduleId, srcPath);
    }
}
