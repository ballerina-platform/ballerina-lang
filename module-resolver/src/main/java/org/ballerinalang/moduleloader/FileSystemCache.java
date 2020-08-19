package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.moduleloader.Util.isValidVersion;

public abstract class FileSystemCache extends Cache {

    private Path fileSystemCachePath;

    public FileSystemCache(Path fileSystemCachePath) {
        this.fileSystemCachePath = fileSystemCachePath;
    }

    @Override
    public List<String> resolveVersions(ModuleId moduleId, String filter) {
        List<String> versions = new ArrayList<>();
        Path modulePath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName(), moduleId.getModuleName());
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
    public boolean isModuleExists(ModuleId moduleId) {
        Path modulePath = Paths
                .get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName(), moduleId.getModuleName(), moduleId.getVersion());
        return modulePath.toFile().exists();
    }

    @Override
    public Module getModule(ModuleId moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName(),
                moduleId.getModuleName(), moduleId.getVersion());
        return new Module(moduleId, srcPath);
    }
}
