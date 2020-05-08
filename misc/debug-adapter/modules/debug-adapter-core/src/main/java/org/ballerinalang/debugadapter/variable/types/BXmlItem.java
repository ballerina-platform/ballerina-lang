package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

/**
 * Ballerina xml variable type.
 */
public class BXmlItem extends BCompoundVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BXmlItem(Value value, Variable dapVariable) {
        this.jvmValueRef = (ObjectReferenceImpl) value;
        dapVariable.setType(BVariableType.XML.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
        this.computeChildVariables();
    }

    @Override
    public String getValue() {
        return jvmValueRef.toString();
    }

    @Override
    public void computeChildVariables() {
        // Todo
    }
}
