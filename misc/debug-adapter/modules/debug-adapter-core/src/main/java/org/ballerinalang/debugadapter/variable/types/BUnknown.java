package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

/**
 * Ballerina variable implementation for unknown types.
 */
public class BUnknown extends BPrimitiveVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BUnknown(Value value, Variable dapVariable) {
        this.jvmValueRef = (ObjectReferenceImpl) value;
        dapVariable.setType(BVariableType.UNKNOWN.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
    }

    @Override
    public String getValue() {
        return jvmValueRef.toString();
    }
}
