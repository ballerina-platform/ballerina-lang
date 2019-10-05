package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.VariableUtils;
import org.ballerinalang.debugadapter.variable.VariableImpl;
import org.eclipse.lsp4j.debug.Variable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BString extends VariableImpl {

    private final ObjectReferenceImpl value;

    public BString(Value value, Variable dapVariable) {
        this.value = (ObjectReferenceImpl) value;
        this.setDapVariable(dapVariable);
        dapVariable.setType("string");
        dapVariable.setValue(this.toString());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
