package org.wso2.ballerinalang.compiler.nballerina;

/**
 * Type names for Nballerina.
 */
public class NBTypeNames {
    static final String INT_ARITHMETIC_BINARY_INSN =
            "record {| (INSN_INT_ARITHMETIC_BINARY|INSN_INT_NO_PANIC_ARITHMETIC_BINARY) name; +|-|*|/|% op; Register" +
                    " result; wso2/nballerina.bir:0.1.0:IntOperand[] operands; anydata...; |}";
    static final String RET_INSN = "record {| INSN_RET name; wso2/nballerina.bir:0.1.0:Operand operand; anydata...; |}";
    static final String REGISTER = "record {| int number; wso2/nballerina.types:0.1.0:SemType semType;" +
            " string? varName; |}";
    static final String INTERNAL_SYMBOL = "record {| boolean isPublic; string identifier; |}";
    static final String FUNCTION_SIGNATURE = "record {| wso2/nballerina.types:0.1.0:SemType returnType;" +
            " wso2/nballerina.types:0.1.0:SemType[] paramTypes; wso2/nballerina.types:0.1.0:SemType? restParamType; |}";
    static final String FUNCTION_DEFN = "record {| wso2/nballerina.bir:0.1.0:Symbol symbol;" +
            " FunctionSignature signature; |}";
    static final String MODULE_ID = "record {| string? organization; [string,string...] names; |}";
    static final String READONLY = " & readonly";
}
