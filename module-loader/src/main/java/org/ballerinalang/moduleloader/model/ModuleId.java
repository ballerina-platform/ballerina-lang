package org.ballerinalang.moduleloader.model;

import org.wso2.ballerinalang.compiler.util.Names;

public class ModuleId {
    public String orgName;
    public String moduleName;
    public String version;

    @Override
    public String toString() {
        if (Names.DOT.equals(this.moduleName)) {
            return this.moduleName;
        }

        String organizationName = "";
        if (this.orgName != null && !this.orgName.equals(Names.ANON_ORG)) {
            organizationName = this.orgName + Names.ORG_NAME_SEPARATOR.value;
        }

        if (version.equals(Names.EMPTY)) {
            return organizationName + this.moduleName;
        }

        return organizationName + this.moduleName + Names.VERSION_SEPARATOR.value + this.version;
    }
}
