package io.ballerina.multiservice.model;

import java.util.List;

public class Resource {

    private final ResourceId resourceId;
    private final List<Parameter> parameters;
    private final List<String> returns;
    private final List<ResourceId> interactions;

    public Resource(ResourceId resourceId, List<Parameter> parameters, List<String> returns, List<ResourceId> interactions) {
        this.resourceId = resourceId;
        this.parameters = parameters;
        this.returns = returns;
        this.interactions = interactions;
    }

    public static class ResourceId {
        private final String serviceId;
        private final String path;
        private final String method;

        public ResourceId(String serviceId, String method, String path) {
            this.serviceId = serviceId;
            this.method = method;
            this.path = path;
        }
    }
}
