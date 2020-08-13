package org.ballerinalang.openapi.validator.error;

import org.ballerinalang.openapi.validator.Constants;

/**
 * This model for identify the validation errors.
 */
public class ValidationError {
    String fieldName;
    Constants.Type type;
    
    public ValidationError() {
        fieldName = null;
        type = null;
    }
    public ValidationError(String fieldName, Constants.Type type) {
        this.fieldName = fieldName;
        this.type = type;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public void setType(Constants.Type type) {
        this.type = type;
    }
    public String getFieldName() {
        return fieldName;
    }
    public  Constants.Type getType() {
        return type;
    }
}
