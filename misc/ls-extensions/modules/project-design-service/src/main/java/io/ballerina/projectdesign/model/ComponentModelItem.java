package io.ballerina.projectdesign.model;

import io.ballerina.tools.text.LineRange;

/**
 * Represents the abstract model for a component model item.
 */
public abstract class ComponentModelItem {
    private final LineRange lineRange;

    public ComponentModelItem(LineRange lineRange) {
        this.lineRange = lineRange;
    }

    public LineRange getLineRange() {
        return lineRange;
    }
}
