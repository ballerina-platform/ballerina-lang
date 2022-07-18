package io.ballerina.multiservice.model;


import java.util.List;

public class Service {
    private final String path;
    private final String serviceId;
    private final List<Resource> resources;


    public Service(String path, String serviceId, List<Resource> resources) {
        this.path = path;
        this.serviceId = serviceId;
        this.resources = resources;
    }
}
