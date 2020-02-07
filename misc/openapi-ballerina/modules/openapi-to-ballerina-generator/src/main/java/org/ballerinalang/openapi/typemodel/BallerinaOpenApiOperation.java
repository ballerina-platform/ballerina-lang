package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * This class contains the OpenApi Operation Type Object.
 */
public class BallerinaOpenApiOperation {
    private String opMethod;
    private String opName;
    private List<BallerinaOpenApiParameter> parameterList;
    private BallerinaOpenApiRequestBody requestBody;

    public BallerinaOpenApiRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(BallerinaOpenApiRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public String getOpMethod() {
        return opMethod;
    }

    public void setOpMethod(String opMethod) {
        this.opMethod = opMethod;
    }

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public List<BallerinaOpenApiParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<BallerinaOpenApiParameter> parameterList) {
        this.parameterList = parameterList;
    }
}
