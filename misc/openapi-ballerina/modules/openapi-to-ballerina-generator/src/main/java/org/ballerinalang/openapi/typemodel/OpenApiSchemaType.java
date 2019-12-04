package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * Java representation for OpenApi schema.
 */
public class OpenApiSchemaType {

    private String schemaName;
    private String schemaType;
    private String reference;
    private String itemType;
    private String itemName;
    private String unescapedItemName;
    private List<String> required;
    private List<OpenApiPropertyType> schemaProperties;
    private boolean isArray;
    private boolean isInline;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isInline() {
        return isInline;
    }

    public void setInline(boolean inline) {
        isInline = inline;
    }

    public String getUnescapedItemName() {
        return unescapedItemName;
    }

    public void setUnescapedItemName(String unescapedItemName) {
        this.unescapedItemName = unescapedItemName;
    }

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

    public String getreference() {
        return reference;
    }

    public void setreference(String ref) {
        this.reference = ref;
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

    public boolean getIsArray() {
        return isArray;
    }

    public void setIsArray(boolean array) {
        isArray = array;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
