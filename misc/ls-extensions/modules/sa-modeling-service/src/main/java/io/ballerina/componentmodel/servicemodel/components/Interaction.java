package io.ballerina.componentmodel.servicemodel.components;

/**
 * Represent interaction with another service.
 */
public class Interaction {
    private final ResourceId resourceId;
    private final String connectorType;

    public Interaction(ResourceId resourceId, String connectorType) {
        this.resourceId = resourceId;
        this.connectorType = connectorType;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public String getConnectorType() {
        return connectorType;
    }
}
