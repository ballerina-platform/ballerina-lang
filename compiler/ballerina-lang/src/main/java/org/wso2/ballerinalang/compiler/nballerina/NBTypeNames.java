package org.wso2.ballerinalang.compiler.nballerina;

/**
 * Type names for Nballerina.
 */
public class NBTypeNames {
    static final String FUNCTION_REF = "record {| wso2/nballerina.bir:0.1.0:Symbol symbol; " +
            "FunctionSignature signature; |}";
    static final String INT_ARITHMETIC_BINARY_INSN = "record {| INSN_INT_ARITHMETIC_BINARY name; +|-|*|/|% op; " +
            "Register result; wso2/nballerina.bir:0.1.0:IntOperand[2] operands; Position position; anydata...; |}";
    static final String RET_INSN = "record {| INSN_RET name; wso2/nballerina.bir:0.1.0:Operand operand; anydata...; |}";
    static final String INTNEG_INSN = "record {| (INSN_INT_NEGATE|INSN_INT_NO_PANIC_NEGATE) name; Register result;" +
            " Register operand; anydata...; |}";
    static final String BOOLNOT_INSN = "record {| INSN_BOOLEAN_NOT name; Register result;" +
            " Register operand; anydata...; |}";
    static final String LIST_CON_INSN = "record {| INSN_LIST_CONSTRUCT_RW name; Register result; " +
            "wso2/nballerina.bir:0.1.0:Operand[] operands; |}";
    static final String REGISTER = "record {| int number; wso2/nballerina.types:0.1.0:SemType semType;" +
            " string? varName; |}";
    static final String INTERNAL_SYMBOL = "record {| boolean isPublic; string identifier; |}";
    static final String EXTERNAL_SYMBOL = "record {| ModuleId module; string identifier; |}";
    static final String CALL_INSN = "record {| Position? position; INSN_CALL name; Register result; " +
            "wso2/nballerina.bir:0.1.0:FunctionOperand func; wso2/nballerina.bir:0.1.0:Operand[] args; anydata...; |}";
    static final String ASSIGN_INSN = "record {| INSN_ASSIGN name; Register result; " +
            "wso2/nballerina.bir:0.1.0:Operand operand; anydata...; |}";
    static final String FUNCTION_SIGNATURE = "record {| wso2/nballerina.types:0.1.0:SemType returnType;" +
            " wso2/nballerina.types:0.1.0:SemType[] paramTypes; wso2/nballerina.types:0.1.0:SemType? restParamType; |}";
    static final String FUNCTION_DEFN = "record {| InternalSymbol symbol; FunctionSignature signature; " +
            "Position position; readonly...; |}";
    static final String MODULE_ID = "record {| string? organization; [string,string...] names; |}";
    static final String READONLY = " & readonly";
    static final String POSITION = "record {| int lineNumber; int indexInLine; |}";
    static final String OPERAND = "";
}
