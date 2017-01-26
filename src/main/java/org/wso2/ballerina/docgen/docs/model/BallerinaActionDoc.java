package org.wso2.ballerina.docgen.docs.model;

import org.wso2.ballerina.core.model.BallerinaAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina action document object.
 */
public class BallerinaActionDoc {

    /**
     * <actionName>(ConnectorName connector, paramType0 paramValue0 ...)
     * eg: myAction(MyConnector myConnector, string param0)
     */
    private String signature;

    /**
     * comments specified via param annotations.
     */
    private List<String> parameters;

    /**
     * comments specified via return annotations.
     */
    private List<String> returnParams;

    /**
     * comment specified via description annotation.
     */
    private String description;

    /**
     * return types of the action.
     */
    private String returnTypes;

    /**
     * comments specified via exceptions annotations.
     */
    private List<String> thrownExceptions;

    public BallerinaActionDoc(BallerinaAction action) {
        parameters = new ArrayList<String>();
        returnParams = new ArrayList<String>();
        thrownExceptions = new ArrayList<String>();
    }

    public String getReturnTypes() {
        return returnTypes;
    }

    public void setReturnTypes(String returnTypes) {
        this.returnTypes = returnTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getReturnParams() {
        return returnParams;
    }

    public void setReturnParams(List<String> returnParams) {
        this.returnParams = returnParams;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getThrownExceptions() {
        return thrownExceptions;
    }

    public void setThrownExceptions(List<String> thrownExceptions) {
        this.thrownExceptions = thrownExceptions;
    }

    @Override
    public String toString() {
        return "BallerinaActionDoc [signature=" + signature + ", returnType=" + returnTypes + ", description="
                + description + ", parameters=" + parameters + ", returnParams=" + returnParams + "]";
    }
}
