package org.ballerinalang.openapi.typemodel;

public class OpenApiParameterType {

    private boolean isPathParam;
    private boolean isQueryParam;
    private String paramName;
    private OpenApiSchemaType paramType;

    public boolean isPathParam() {
        return isPathParam;
    }

    public void setPathParam(boolean pathParam) {
        isPathParam = pathParam;
    }

    public boolean isQueryParam() {
        return isQueryParam;
    }

    public void setQueryParam(boolean queryParam) {
        isQueryParam = queryParam;
    }

    public OpenApiSchemaType getParamType() {
        return paramType;
    }

    public void setParamType(OpenApiSchemaType paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }


}
