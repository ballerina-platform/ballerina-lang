package org.wso2.ballerinalang.compiler.bir.codegen.interop;

public enum JInsKind {
    JCAST((byte) 1),
    CALL((byte) 2);

    byte value;

    JInsKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
