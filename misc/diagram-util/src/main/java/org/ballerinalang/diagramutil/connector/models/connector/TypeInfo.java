package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;

/**
 * TypeInfo model.
 */
public class TypeInfo {
    @Expose
    public String name;
    @Expose
    public String orgName;
    @Expose
    public String moduleName;
    @Expose
    public String packageName;
    @Expose
    public String version;

    public TypeInfo(String name, String orgName, String moduleName, String packageName, String version) {
        this.name = name;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.version = version;
    }
}
