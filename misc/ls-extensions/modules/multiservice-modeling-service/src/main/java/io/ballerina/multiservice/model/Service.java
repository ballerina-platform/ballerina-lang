package io.ballerina.multiservice.model;


import java.util.List;

/**
 * Provides service related information.
 */
public class Service {
    private final String path;
    private final String serviceId;
    private final List<Resource> resources;


    public Service(String path, String serviceId, List<Resource> resources) {
        this.path = path;
        this.serviceId = serviceId;
        this.resources = resources;
    }

    public String getPath() {
        return path;
    }

    public String getServiceId() {
        return serviceId;
    }

    public List<Resource> getResources() {
        return resources;
    }
}
