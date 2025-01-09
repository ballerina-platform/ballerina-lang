package io.ballerina.runtime.internal.query.pipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * FrameContext class to encapsulate a frame in the query pipeline.
 * Supports augmentation with variables derived from from/let clauses.
 *
 * @param <T> the type of data stored in the frame context
 */
public class FrameContext<T> {
    private final Map<String, Object> variables; // Variables bound to this frame

    public FrameContext() {
        this.variables = new HashMap<>();
    }

    public FrameContext(FrameContext<T> existingFrame) {
        this.variables = new HashMap<>(existingFrame.variables);
    }

    public void addVariable(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void augmentWithBinding(Map<String, Object> bindings) {
        variables.putAll(bindings);
    }

    public Map<String, Object> getAllVariables() {
        return Map.copyOf(variables);
    }

    @Override
    public String toString() {
        return "FrameContext{" +
                "variables=" + variables +
                '}';
    }
}
