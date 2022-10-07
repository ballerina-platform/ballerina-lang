package io.ballerina.componentmodel.servicemodel.components;

/**
 * Represents display annotation.
 */
public class ServiceAnnotation {
    private String id = "";
    private String label = "";

    public ServiceAnnotation(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
