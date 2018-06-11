package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangRecordDTO {
    
    private int packageId;
    
    private String name;
    
    private String fields;

    public BLangRecordDTO(int packageId, String name, String fields) {
        this.packageId = packageId;
        this.name = name;
        this.fields = fields;
    }

    public int getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }

    public String getFields() {
        return fields;
    }
}
