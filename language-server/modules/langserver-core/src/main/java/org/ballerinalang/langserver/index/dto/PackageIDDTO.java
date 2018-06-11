package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/10/18.
 */
public class PackageIDDTO {
    
    private String name;
    
    private String orgName;
    
    private String version;

    public PackageIDDTO(String name, String orgName, String version) {
        this.name = name;
        this.orgName = orgName;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getVersion() {
        return version;
    }
}
