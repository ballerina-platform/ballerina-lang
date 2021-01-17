package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

public enum DiagramCompletionItemKind {
    IF(1),
    WHILE(2),
    TYPE_GUARD(3);

    private int value;
    
    DiagramCompletionItemKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
