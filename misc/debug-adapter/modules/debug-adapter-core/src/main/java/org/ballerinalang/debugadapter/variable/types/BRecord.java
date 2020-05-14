package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * Ballerina record variable type.
 */
public class BRecord extends BCompoundVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BRecord(Value value, Variable dapVariable) {
        this.jvmValueRef = (ObjectReferenceImpl) value;
        dapVariable.setType(BVariableType.RECORD.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
        this.computeChildVariables();
    }

    @Override
    public String getValue() {
        try {
            // Extracts object type name from the reflected type class.
            String[] split = this.jvmValueRef.referenceType().classObject().reflectedType().name().split("\\.");
            for (String element : split) {
                if (element.contains("$value$")) {
                    return element.replaceFirst("\\$value\\$", "");
                }
            }
            return "unknown";
        } catch (Exception ignored) {
            return "unknown";
        }
    }

    @Override
    public void computeChildVariables() {
        try {
            Map<Field, Value> fieldValueMap = jvmValueRef.getValues(jvmValueRef.referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            // Uses the ballerina record type name to extract ballerina record fields from the jvm reference.
            String balRecordFiledIdentifier = this.getValue() + ".";
            fieldValueMap.forEach((field, value) -> {
                // Filter out internal variables.
                if (field.toString().contains(balRecordFiledIdentifier)) {
                    values.put(field.name(), value);
                }
            });
            this.setChildVariables(values);
        } catch (Exception ignored) {
            this.setChildVariables(new HashMap<>());
        }
    }
}
