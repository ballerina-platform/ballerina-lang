package io.ballerina;

import io.ballerina.tools.text.LineRange;

/**
 * Represents a service resource.
 *
 * @since 2.0.0
 */
public class Resource {

    private String name;
    private LineRange lineRange;

    public Resource(String name, LineRange lineRange) {

        this.name = name;
        this.lineRange = lineRange;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public LineRange getLineRange() {

        return lineRange;
    }

    public void setLineRange(LineRange lineRange) {

        this.lineRange = lineRange;
    }
}
