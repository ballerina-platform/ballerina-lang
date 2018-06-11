package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangObjectDTO {
    
    private int packageId;
    
    private String name;
    
    private String fields;
    
    private ObjectType type;

    public BLangObjectDTO(int packageId, String name, String fields, ObjectType type) {
        this.packageId = packageId;
        this.name = name;
        this.fields = fields;
        this.type = type;
    }

    public BLangObjectDTO(int packageId, String name, String fields) {
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

    public ObjectType getType() {
        return type;
    }
}
