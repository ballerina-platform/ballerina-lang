package io.ballerina.multiservice.model;

/**
 * Represents resource funstion parameter information.
 */
public class Parameter {
    private final String type;
    private final String name;
    private final String in;
    private final boolean isRequired;

    public Parameter(String type, String name, String in, boolean isRequired) {
        this.type = type;
        this.name = name;
        this.in = in;
        this.isRequired = isRequired;
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

    public boolean isRequired() {
        return isRequired;
    }
}
