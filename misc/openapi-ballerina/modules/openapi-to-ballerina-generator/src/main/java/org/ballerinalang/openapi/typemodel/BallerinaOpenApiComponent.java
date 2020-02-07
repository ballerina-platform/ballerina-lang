package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * This class contains the OpenApi Component Type Object.
 */
public class BallerinaOpenApiComponent {
    private List<BallerinaOpenApiSchema> schemaList;
    private List<BallerinaOpenApiSchema> propertiesList;

    public List<BallerinaOpenApiSchema> getSchemaList() {
        return schemaList;
    }

    public void setSchemaList(List<BallerinaOpenApiSchema> schemaList) {
        this.schemaList = schemaList;
    }

    public List<BallerinaOpenApiSchema> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<BallerinaOpenApiSchema> propertiesList) {
        this.propertiesList = propertiesList;
    }
}
