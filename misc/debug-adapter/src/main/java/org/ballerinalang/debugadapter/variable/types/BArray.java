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

public class BArray extends VariableImpl {

    private final ObjectReferenceImpl value;
    private Map<String, Value> childVariables;

    public BArray(Value value, Variable dapVariable) {
        this.value = (ObjectReferenceImpl) value;
        this.setDapVariable(dapVariable);
        dapVariable.setType(this.toString());
        dapVariable.setValue(this.toString());

        Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
        this.setChildVariables(values);
    }

    @Override
    public Map<String, Value> getChildVariables() {
        return childVariables;
    }

    @Override
    public void setChildVariables(Map<String, Value> childVariables) {
        this.childVariables = childVariables;
    }

    @Override
    public String toString() {
        List<Field> fields = value.referenceType().allFields();
        Field arrayValueField = value.getValues(fields).entrySet().stream()
                .filter(fieldValueEntry ->
                        fieldValueEntry.getValue() != null
                                && fieldValueEntry.getKey().toString().endsWith("Values"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()).get(0);
        String arrayType = arrayValueField.toString();

        arrayType = arrayType.replaceFirst("org.ballerinalang.jvm.values.ArrayValue.", "");
        arrayType = arrayType.replaceFirst("Values", "");
        arrayType = arrayType.replaceFirst("ref", "Array");

        Field arraySizeField = value
                .getValues(fields).entrySet().stream()
                .filter(fieldValueEntry ->
                        fieldValueEntry.getValue() != null
                                && fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()).get(0);
        int arraySize = ((IntegerValue) value.getValue(arraySizeField)).value();

        return arrayType + "[" + arraySize + "]";
    }

}
