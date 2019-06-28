package org.ballerinalang.openapi.utils;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.ballerinalang.openapi.typemodel.OpenApiPropertyType;

public class SchemaParser {

    public void parseIntegerSchema(Object integerSchema, OpenApiPropertyType openApiPropertyType) {
        IntegerSchema intSchema = (IntegerSchema) integerSchema;
        openApiPropertyType.setPropertyType(intSchema.getType());
    }

    public void parseStringSchema(Object stringSchema, OpenApiPropertyType openApiPropertyType) {
        StringSchema strSchema = (StringSchema) stringSchema;
        openApiPropertyType.setPropertyType(strSchema.getType());
    }
}
