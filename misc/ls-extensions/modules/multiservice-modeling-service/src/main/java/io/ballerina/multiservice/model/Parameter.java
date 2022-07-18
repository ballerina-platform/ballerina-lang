package io.ballerina.multiservice.model;

public class Parameter {
    private final String type;
    private final String name;
    private final String in;

    public Parameter(String type, String name, String in) {
        this.type = type;
        this.name = name;
        this.in = in;
    }
}
