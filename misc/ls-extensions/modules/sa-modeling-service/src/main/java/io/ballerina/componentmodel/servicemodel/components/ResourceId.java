package io.ballerina.componentmodel.servicemodel.components;

/**
 * Provide resource information.
 */
public class ResourceId {

    private final String serviceId;
    private final String path;
    private final String action;

    public ResourceId(String serviceId, String action, String path) {

        this.serviceId = serviceId;
        this.action = action;
        this.path = path;
    }

    public String getServiceId() {

        return serviceId;
    }

    public String getPath() {

        return path;
    }

    public String getAction() {

        return action;
    }
}
