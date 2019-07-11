package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * Java Representation for OpenApi Path.
 */
public class OpenApiPathType {

    private String pathName;
    private List<OpenApiOperationType> operations;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public List<OpenApiOperationType> getOperations() {
        return operations;
    }

    public void setOperations(List<OpenApiOperationType> operations) {
        this.operations = operations;
    }

}
