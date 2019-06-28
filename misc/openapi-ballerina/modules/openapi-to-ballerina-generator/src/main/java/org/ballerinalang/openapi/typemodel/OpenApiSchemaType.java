package org.ballerinalang.openapi.typemodel;

import java.util.List;

public class OpenApiSchemaType {

    private String schemaName;
    private String schemaType;
    private String $ref;
    private String itemType;
    private List<String> required;
    private List<OpenApiPropertyType> schemaProperties;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(String schemaType) {
        this.schemaType = schemaType;
    }

    public List<OpenApiPropertyType> getSchemaProperties() {
        return schemaProperties;
    }

    public void setSchemaProperties(List<OpenApiPropertyType> schemaProperties) {
        this.schemaProperties = schemaProperties;
    }

    public String get$ref() {
        return $ref;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }


    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }


    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }


}
