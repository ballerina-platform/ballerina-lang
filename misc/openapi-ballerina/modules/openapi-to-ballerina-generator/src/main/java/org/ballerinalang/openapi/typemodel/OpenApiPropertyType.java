package org.ballerinalang.openapi.typemodel;

/**
 * Java representation for OpenApu Property.
 */
public class OpenApiPropertyType {

    private String propertyName;
    private String propertyType;
    private boolean isArray;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public boolean getIsArray() {
        return isArray;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }


}
