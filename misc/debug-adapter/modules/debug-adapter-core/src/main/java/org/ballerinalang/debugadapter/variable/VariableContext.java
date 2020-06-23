package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Debug context information for ballerina debug variables.
 */
public class VariableContext {
    private final StackFrame frame;
    private final ThreadReference owningThread;

    public VariableContext(StackFrame frame) {
        this(frame, frame.thread());
    }

    public VariableContext(StackFrame frame, ThreadReference threadRef) {
        this.frame = frame;
        this.owningThread = threadRef;
    }

    public ThreadReference getOwningThread() {
        return owningThread;
    }

    public StackFrame getFrame() {
        return frame;
    }
}
