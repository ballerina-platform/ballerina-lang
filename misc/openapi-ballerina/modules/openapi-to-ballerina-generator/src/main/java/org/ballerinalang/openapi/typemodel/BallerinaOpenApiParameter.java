package org.ballerinalang.openapi.typemodel;

/**
 * This class contains the OpenApi Parameter Type Object.
 */
public class BallerinaOpenApiParameter {
    private boolean isPathParam;
    private String paramName;
    private String refType;
    private BallerinaOpenApiSchema paramType;

    public BallerinaOpenApiSchema getParamType() {
        return paramType;
    }

    public void setParamType(BallerinaOpenApiSchema paramType) {
        this.paramType = paramType;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public boolean isPathParam() {
        return isPathParam;
    }

    public void setPathParam(boolean pathParam) {
        isPathParam = pathParam;
    }
}
