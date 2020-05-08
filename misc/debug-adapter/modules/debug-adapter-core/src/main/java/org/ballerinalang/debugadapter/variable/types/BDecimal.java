package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

/**
 * Ballerina decimal variable type.
 */
public class BDecimal extends BPrimitiveVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BDecimal(Value value, Variable dapVariable) {
        this.jvmValueRef = (ObjectReferenceImpl) value;
        dapVariable.setType(BVariableType.DECIMAL.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
    }

    @Override
    public String getValue() {
        return jvmValueRef.toString();
    }
}
