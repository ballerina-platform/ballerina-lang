package org.ballerinalang.langserver.completions.resolvers;

/**
 * Completion Item Priority enum
 */
public enum  Priority {
    PRIORITY1(1),
    PRIORITY2(2),
    PRIORITY3(3),
    PRIORITY4(4),
    PRIORITY5(5),
    PRIORITY6(6),
    PRIORITY7(7);

    private int priority;

    private Priority(int priority) {
        this.priority = priority;
    }

    private int getPriority() {
        return this.priority;
    }
}
