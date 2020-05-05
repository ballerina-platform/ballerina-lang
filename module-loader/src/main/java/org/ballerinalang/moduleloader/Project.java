package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.moduleloader.Util.isValidVersion;

public class Project implements Cache {

    private Path projectPath;
    private String orgName;
    private String version;

    public Project(Path projectPath, String orgName, String version) {
        this.projectPath = projectPath;
        this.orgName = orgName;
        this.version = version;
    }

    @Override
    public List<String> resolveVersions(ModuleId moduleId, String filter) {
        List<String> versions = new ArrayList<>();
        if (moduleId.orgName.equals(this.orgName)) {
            Path srcPath = Paths.get(String.valueOf(this.projectPath), "src");
            File[] fileEntries = new File(String.valueOf(srcPath)).listFiles();
            if (fileEntries != null) {
                for (File fileEntry : fileEntries) {
                    if (fileEntry.isDirectory() && fileEntry.getName().equals(moduleId.moduleName) && isValidVersion(
                            fileEntry.getName(), filter)) {
                        versions.add(this.version);
                    }
                }
            }
        }
        return versions;
    }

    @Override
    public boolean isModuleExists(ModuleId moduleId) {
        if (moduleId.orgName.equals(this.orgName) && moduleId.version.equals(this.version)) {
            Path modulePath = Paths.get(String.valueOf(this.projectPath), "src", moduleId.moduleName);
            return modulePath.toFile().exists();
        }
        return false;
    }

    @Override
    public Module getModule(ModuleId moduleId) {
        return null;
    }
}
