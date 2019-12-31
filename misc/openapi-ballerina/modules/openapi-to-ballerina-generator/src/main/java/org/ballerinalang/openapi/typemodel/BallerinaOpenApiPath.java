package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * This class contains the OpenApi Path Type Object.
 */
public class BallerinaOpenApiPath {
    private String path;
    private List<BallerinaOpenApiOperation> operationsList;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<BallerinaOpenApiOperation> getOperationsList() {
        return operationsList;
    }

    public void setOperationsList(List<BallerinaOpenApiOperation> operationsList) {
        this.operationsList = operationsList;
    }
}
