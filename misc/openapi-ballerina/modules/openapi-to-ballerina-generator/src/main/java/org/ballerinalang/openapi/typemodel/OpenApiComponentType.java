package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * Java representation for OpenApi Components.
 */
public class OpenApiComponentType {

    private List<OpenApiSchemaType> schemaTypes;

    public List<OpenApiSchemaType> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<OpenApiSchemaType> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    private List<OpenApiSchemaType> parameterTypes;

    public List<OpenApiSchemaType> getSchemaTypes() {
        return schemaTypes;
    }

    public void setSchemaTypes(List<OpenApiSchemaType> schemaTypes) {
        this.schemaTypes = schemaTypes;
    }

}
