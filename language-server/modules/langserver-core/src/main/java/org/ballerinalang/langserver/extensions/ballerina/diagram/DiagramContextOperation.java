package org.ballerinalang.langserver.extensions.ballerina.diagram;

import org.ballerinalang.langserver.commons.LSOperation;

public enum DiagramContextOperation implements LSOperation {
    DIAGRAM_COMPLETION("diagram/completion");
    
    private final String name;
    
    DiagramContextOperation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
