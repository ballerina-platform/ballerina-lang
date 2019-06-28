package org.ballerinalang.openapi.typemodel;

import java.util.List;

public class OpenApiComponentType {

    private List<OpenApiSchemaType> schemaTypes;

    public List<OpenApiSchemaType> getSchemaTypes() {
        return schemaTypes;
    }

    public void setSchemaTypes(List<OpenApiSchemaType> schemaTypes) {
        this.schemaTypes = schemaTypes;
    }

}
