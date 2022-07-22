package io.ballerina.multiservice.model;

import java.util.List;

/**
 * Represents resource details.
 */
public class Resource {

    private final ResourceId resourceId;
    private final List<Parameter> parameters;
    private final List<String> returns;
    private final List<ResourceId> interactions;

    public Resource(ResourceId resourceId, List<Parameter> parameters, List<String> returns,
                    List<ResourceId> interactions) {
        this.resourceId = resourceId;
        this.parameters = parameters;
        this.returns = returns;
        this.interactions = interactions;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<String> getReturns() {
        return returns;
    }

    public List<ResourceId> getInteractions() {
        return interactions;
    }

    /**
     * Provide resource information.
     */
    public static class ResourceId {
        private final String serviceId;
        private final String path;
        private final String method;

        public ResourceId(String serviceId, String method, String path) {
            this.serviceId = serviceId;
            this.method = method;
            this.path = path;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }
    }
}
