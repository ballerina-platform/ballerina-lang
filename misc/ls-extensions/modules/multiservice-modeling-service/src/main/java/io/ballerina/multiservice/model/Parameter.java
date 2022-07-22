package io.ballerina.multiservice.model;

/**
 * Represents resource funstion parameter information.
 */
public class Parameter {
    private final String type;
    private final String name;
    private final String in;

    public Parameter(String type, String name, String in) {
        this.type = type;
        this.name = name;
        this.in = in;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }
}
