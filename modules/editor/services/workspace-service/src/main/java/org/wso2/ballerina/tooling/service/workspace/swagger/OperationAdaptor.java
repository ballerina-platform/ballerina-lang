package org.wso2.ballerina.tooling.service.workspace.swagger;

import io.swagger.models.Operation;

/**
 * This class will hold operation details specific to HTTP operation.
 */
public class OperationAdaptor {


    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    Operation operation;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    String path;

    public String getHttpOperation() {
        return httpOperation;
    }

    public void setHttpOperation(String httpOperation) {
        this.httpOperation = httpOperation;
    }

    String httpOperation;

    OperationAdaptor() {
        this.operation = new Operation();
    }

}
