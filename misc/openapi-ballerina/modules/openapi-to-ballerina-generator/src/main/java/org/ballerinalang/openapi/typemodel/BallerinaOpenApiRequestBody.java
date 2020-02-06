package org.ballerinalang.openapi.typemodel;

/**
 * This class contains the OpenApi RequestBody Type Object.
 */
public class BallerinaOpenApiRequestBody {
    private BallerinaOpenApiSchema contentType;
    private String refType;

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public BallerinaOpenApiSchema getContentType() {
        return contentType;
    }

    public void setContentType(BallerinaOpenApiSchema contentType) {
        this.contentType = contentType;
    }
}
