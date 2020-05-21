package org.ballerinalang.moduleloader.model;

import java.nio.file.Path;

public class ModuleId {
    private String orgName;
    private String moduleName;
    private String version;
    private Path resolution;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Path getResolution() {
        return resolution;
    }

    public void setResolution(Path resolution) {
        this.resolution = resolution;
    }
}
