package org.wso2.ballerina.docgen.docs.model;

import org.wso2.ballerina.core.model.BallerinaConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina connector document object.
 */
public class BallerinaConnectorDoc {

    /**
     * <connectorName>(paramType0 paramValue0, paramType1 paramValue1 ...) eg: myConnector(string param0, string param1)
     */
    private String signature;

    /**
     * comments specified via param annotations.
     */
    private List<String> parameters;

    /**
     * actions resides in connector
     */
    private List<BallerinaActionDoc> actions;

    /**
     * comment specified via description annotation.
     */
    private String description;

    public BallerinaConnectorDoc(BallerinaConnector conn) {
        parameters = new ArrayList<String>();
        actions = new ArrayList<BallerinaActionDoc>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BallerinaActionDoc> getActions() {
        return actions;
    }

    public void setActions(List<BallerinaActionDoc> actions) {
        this.actions = actions;
    }

    public void addAction(BallerinaActionDoc doc) {
        this.actions.add(doc);
    }

    @Override
    public String toString() {
        return "BallerinaConnectorDoc [signature=" + signature + ", description="
                + description + ", parameters=" + parameters + ", actions=" + actions + "]";
    }

}
