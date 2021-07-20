package org.wso2.ballerinalang.compiler.nballerina;

public class NBTypeNames {
    static String IntArithmeticBinaryInsn =
            "record {| (INSN_INT_ARITHMETIC_BINARY|INSN_INT_NO_PANIC_ARITHMETIC_BINARY) name; +|-|*|/|% op; Register" +
                    " result; wso2/nballerina.bir:0.1.0:IntOperand[] operands; anydata...; |}";
    static String RetInsn = "record {| INSN_RET name; wso2/nballerina.bir:0.1.0:Operand operand; anydata...; |}";
    static String Register = "record {| int number; wso2/nballerina.types:0.1.0:SemType semType; string? varName; |}";
}
