package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBir;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * BIR Cache representation for Home BIR Cache and Distribution BIR Cache.
 */
public class BirCache extends FileSystemCache {

    public BirCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }

    @Override
    PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        return new PackageBir(moduleId, srcPath);
    }
}
