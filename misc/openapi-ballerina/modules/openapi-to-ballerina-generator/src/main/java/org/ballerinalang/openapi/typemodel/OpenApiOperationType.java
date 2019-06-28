package org.ballerinalang.openapi.typemodel;

import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

public class OpenApiOperationType {

    private String operationType;
    private List<OpenApiParameterType> parameters;
    private List<OpenApiResponseType> responses;
    private OpenApiRequestBodyType requestBody;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<OpenApiParameterType> getParameters() {
        return parameters;
    }

    public void setParameters(List<OpenApiParameterType> parameters) {
        this.parameters = parameters;
    }

    public List<OpenApiResponseType> getResponses() {
        return responses;
    }

    public void setResponses(List<OpenApiResponseType> responses) {
        this.responses = responses;
    }

    public OpenApiRequestBodyType getRequestBodies() {
        return requestBody;
    }

    public void setRequestBodies(OpenApiRequestBodyType requestBody) {
        this.requestBody = requestBody;
    }

}
