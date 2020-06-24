package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;

import java.io.File;
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
    public PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        if (srcPath.toFile().exists()) {
            File[] files = new File(String.valueOf(srcPath)).listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getPath().endsWith(".balo")) {
                        return new PackageBalo(moduleId, Paths.get(file.getPath()));
                    }
                }
            }
        }
        return null;
    }
}
