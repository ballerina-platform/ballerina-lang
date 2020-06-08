package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageFileSystem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectModules extends Cache {

    private Path projectPath;
    private String orgName;
    private String version;

    public ProjectModules(Path projectPath, String orgName, String version) {
        this.projectPath = projectPath;
        this.orgName = orgName;
        this.version = version;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        List<String> versions = new ArrayList<>();
        if (moduleId.getOrgName().getValue().equals(this.orgName)) {
            File[] fileEntries = new File(String.valueOf(this.projectPath)).listFiles();
            if (fileEntries != null) {
                for (File fileEntry : fileEntries) {
                    if (fileEntry.isDirectory() && fileEntry.getName().equals(moduleId.getName().getValue())) {
                        versions.add(this.version);
                    }
                }
            }
        }
        return versions;
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        if (moduleId.getOrgName().getValue().equals(this.orgName) && moduleId.version.getValue().equals(this.version)) {
            Path modulePath = Paths.get(String.valueOf(this.projectPath), moduleId.getName().getValue());
            return modulePath.toFile().exists();
        }
        return false;
    }

    @Override
    public PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.projectPath), moduleId.getName().getValue());
        return new PackageFileSystem(moduleId, srcPath);
    }
}
