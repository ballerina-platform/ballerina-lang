package org.ballerinalang.debugadapter.variable.types;

import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

/**
 * Ballerina nil variable type.
 */
public class BNil extends BPrimitiveVariable {

    public BNil(Variable dapVariable) {
        dapVariable.setType(BVariableType.NIL.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
    }

    @Override
    public String getValue() {
        return "()";
    }
}
