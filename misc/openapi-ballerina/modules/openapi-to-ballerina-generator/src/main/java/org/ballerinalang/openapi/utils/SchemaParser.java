package org.ballerinalang.openapi.utils;

import org.ballerinalang.openapi.typemodel.OpenApiPropertyType;

public class SchemaParser {

    public void parseIntegerSchema(OpenApiPropertyType openApiPropertyType) {
        openApiPropertyType.setPropertyType("int");
    }

    public void parseStringSchema(OpenApiPropertyType openApiPropertyType) {
        openApiPropertyType.setPropertyType("string");
    }
}
