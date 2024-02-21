package io.ballerina.compiler.internal.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticPropertyKind;

import java.util.Arrays;

public class StringDiagnosticProperty implements DiagnosticProperty<String> {
    private final DiagnosticPropertyKind kind;
    private final String value;

    public StringDiagnosticProperty(String value) {
        this.kind = DiagnosticPropertyKind.STRING;
        this.value = value;
    }
    @Override
    public DiagnosticPropertyKind kind() {
        return kind;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{kind.hashCode(), value.hashCode()});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringDiagnosticProperty prop) {
            return this.value.equals(prop.value) && this.kind.equals(prop.kind);
        }
        return false;
    }
}
