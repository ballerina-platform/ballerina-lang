package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.Value;
import org.eclipse.lsp4j.debug.Variable;

import java.util.Map;

/**
 * Base implementation for ballerina compound variable types.
 */
public abstract class BCompoundVariable implements BVariable {

    private Variable dapVariable;
    private Map<String, Value> childVariables;

    public BCompoundVariable() {
        this(null);
    }

    public BCompoundVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
        this.childVariables = null;
    }

    /**
     * Returns the value of the variable variable instance in string form. Each extended variable type must have their
     * own implementation to compute/fetch the value.
     */
    public abstract void computeChildVariables();

    public Map<String, Value> getChildVariables() {
        return childVariables;
    }

    public void setChildVariables(Map<String, Value> childVariables) {
        this.childVariables = childVariables;
    }

    @Override
    public Variable getDapVariable() {
        return dapVariable;
    }

    @Override
    public void setDapVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
    }
}
