package org.ballerinalang.moduleloader.model;

import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;

public class ModuleId {
    private String orgName;
    private String moduleName;
    private String version;
    private Path resolution;

//    @Override
//    public String toString() {
//        if (Names.DOT.equals(this.moduleName)) {
//            return this.moduleName;
//        }
//
//        String organizationName = "";
//        if (this.orgName != null && !this.orgName.equals(Names.ANON_ORG)) {
//            organizationName = this.orgName + Names.ORG_NAME_SEPARATOR.value;
//        }
//
//        if (version.equals(Names.EMPTY)) {
//            return organizationName + this.moduleName;
//        }
//
//        return organizationName + this.moduleName + Names.VERSION_SEPARATOR.value + this.version;
//    }

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
