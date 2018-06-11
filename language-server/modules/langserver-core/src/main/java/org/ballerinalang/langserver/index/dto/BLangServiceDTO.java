package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangServiceDTO {
    
    private int packageId;
    
    private String name;

    public BLangServiceDTO(int packageId, String name) {
        this.packageId = packageId;
        this.name = name;
    }

    public int getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }
}
