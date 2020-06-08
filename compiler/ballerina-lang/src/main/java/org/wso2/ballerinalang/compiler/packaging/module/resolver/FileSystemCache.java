package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isValidVersion;

public abstract class FileSystemCache extends Cache {

     Path fileSystemCachePath;

    FileSystemCache(Path fileSystemCachePath) {
        this.fileSystemCachePath = fileSystemCachePath;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        List<String> versions = new ArrayList<>();
        Path modulePath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue());
        File[] fileEntries = new File(String.valueOf(modulePath)).listFiles();
        if (modulePath.toFile().exists() && fileEntries != null) {
            for (final File fileEntry : fileEntries) {
                if (fileEntry.isDirectory() && isValidVersion(fileEntry.getName(), filter)) {
                    versions.add(fileEntry.getName());
                }
            }
        }
        return versions;
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        Path modulePath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        return modulePath.toFile().exists();
    }
}
