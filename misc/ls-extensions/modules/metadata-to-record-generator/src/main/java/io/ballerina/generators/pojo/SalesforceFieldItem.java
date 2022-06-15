package io.ballerina.generators.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO class representing the extracted data from SalesforceMetadataConverter field metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesforceFieldItem {
    boolean updateable;
    boolean createable;
    boolean nillable;
    boolean defaultedOnCreate;
    String name;
    String type;
    String label;

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public boolean isCreateable() {
        return createable;
    }

    public void setCreateable(boolean createable) {
        this.createable = createable;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDefaultedOnCreate() {
        return defaultedOnCreate;
    }

    public void setDefaultedOnCreate(boolean defaultedOnCreate) {
        this.defaultedOnCreate = defaultedOnCreate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
