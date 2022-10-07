package io.ballerina.componentmodel.servicemodel.components;

import java.util.List;

/**
 * Represent a parameter of a Ballerina Object Method.
 */
public class FunctionParameter {

    private final List<String> type;
    private final String name;
    private final boolean isRequired;

    public FunctionParameter(List<String> type, String name, boolean isRequired) {

        this.type = type;
        this.name = name;
        this.isRequired = isRequired;
    }

    public List<String> getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public boolean isRequired() {

        return isRequired;
    }
}
