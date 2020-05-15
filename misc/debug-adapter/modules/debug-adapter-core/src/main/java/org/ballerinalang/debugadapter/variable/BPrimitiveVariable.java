package org.ballerinalang.debugadapter.variable;

import org.eclipse.lsp4j.debug.Variable;

/**
 * Base implementation for ballerina primitive variable types.
 */
public abstract class BPrimitiveVariable implements BVariable {

    private Variable dapVariable;

    @Override
    public Variable getDapVariable() {
        return dapVariable;
    }

    @Override
    public void setDapVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
    }
}
