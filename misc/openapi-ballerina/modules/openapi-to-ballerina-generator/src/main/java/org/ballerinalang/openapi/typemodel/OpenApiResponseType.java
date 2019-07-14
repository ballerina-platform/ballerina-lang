package org.ballerinalang.openapi.typemodel;

/**
 * Java representation for OpenApi Response.
 */
public class OpenApiResponseType {

    private String responseCode;
    private String responseType;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

}
