package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * This class contains the OpenApi Schema Type Object.
 */
public class BallerinaOpenApiSchema {
    private boolean isArray;
    private boolean isInline;
    private boolean isComposedSchema;
    private boolean isAllOf;
    private boolean isOneOf;
    private boolean isAnyOf;
    private boolean hasChildElements;
    private String schemaName;
    private BallerinaOpenApiSchema itemsSchema;
    private List<String> schemaType;
    private List<String> composedSchemaTypes;
    private List<BallerinaOpenApiSchema> schemaProperties;

    public boolean isHasChildElements() {
        return hasChildElements;
    }

    public void setHasChildElements(boolean hasChildElements) {
        this.hasChildElements = hasChildElements;
    }

    public BallerinaOpenApiSchema getItemsSchema() {
        return itemsSchema;
    }

    public void setItemsSchema(BallerinaOpenApiSchema itemsSchema) {
        this.itemsSchema = itemsSchema;
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

    public boolean isComposedSchema() {
        return isComposedSchema;
    }

    public void setComposedSchema(boolean composedSchema) {
        isComposedSchema = composedSchema;
    }

    public boolean isAllOf() {
        return isAllOf;
    }

    public void setAllOf(boolean allOf) {
        isAllOf = allOf;
    }

    public boolean isOneOf() {
        return isOneOf;
    }

    public void setOneOf(boolean oneOf) {
        isOneOf = oneOf;
    }

    public boolean isAnyOf() {
        return isAnyOf;
    }

    public void setAnyOf(boolean anyOf) {
        isAnyOf = anyOf;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<String> getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(List<String> schemaType) {
        this.schemaType = schemaType;
    }

    public List<String> getComposedSchemaTypes() {
        return composedSchemaTypes;
    }

    public void setComposedSchemaTypes(List<String> composedSchemaTypes) {
        this.composedSchemaTypes = composedSchemaTypes;
    }

    public List<BallerinaOpenApiSchema> getSchemaProperties() {
        return schemaProperties;
    }

    public void setSchemaProperties(List<BallerinaOpenApiSchema> schemaProperties) {
        this.schemaProperties = schemaProperties;
    }

}
