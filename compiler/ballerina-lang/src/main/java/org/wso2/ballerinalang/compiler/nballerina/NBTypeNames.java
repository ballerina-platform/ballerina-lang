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
    static final String CATCH_INSN = "record {| INSN_CATCH name; Register result; anydata...; |}";
    static final String ABN_RET_INSN = "record {| INSN_ABNORMAL_RET name; Register operand; anydata...; |}";
    static final String COND_BRANCH_INSN = "record {| INSN_COND_BRANCH name; Register operand; int ifTrue;" +
            " int ifFalse; anydata...; |}";
    static final String BRANCH_INSN = "record {| INSN_BRANCH name; int dest; anydata...; |}";
    static final String TYPECAST_INSN = "record {| INSN_TYPE_CAST name; Register result; Register operand; " +
            "wso2/nballerina.types:0.1.0:SemType semType; Position position; anydata...; |}";
    static final String EQUALITY_INSN = "record {| INSN_EQUALITY name; ==|!=|===|!== op; Register result; " +
            "wso2/nballerina.bir:0.1.0:Operand[2] operands; anydata...; |}";
    static final String COMPARE_INSN = "record {| INSN_COMPARE name; <=|>=|<|> op; int|boolean|string orderType; " +
            "Register result; wso2/nballerina.bir:0.1.0:Operand[2] operands; anydata...; |}";
    static final String INT_BITWISE_INSN = "record {| INSN_INT_BITWISE_BINARY name;" +
            " wso2/nballerina.bir:0.1.0:BitwiseBinaryOp op; Register result;" +
            " wso2/nballerina.bir:0.1.0:IntOperand[2] operands; anydata...; |}";
    static final String STRING_CONCAT_INSN = "record {| INSN_STR_CONCAT name; Register result;" +
            " wso2/nballerina.bir:0.1.0:StringOperand[2] operands; anydata...; |}";
    static final String MAP_CON_INSN = "record {| INSN_MAPPING_CONSTRUCT_RW name; Register result;" +
            " string[] fieldNames; wso2/nballerina.bir:0.1.0:Operand[] operands; |}";
    static final String LIST_GET_INSN = "record {| INSN_LIST_GET name; Register result; Register list; " +
            "wso2/nballerina.bir:0.1.0:IntOperand operand; Position position; |}";
    static final String LIST_SET_INSN = "record {| INSN_LIST_SET name; Register list; wso2/nballerina.bir:0.1.0:" +
            "IntOperand index; wso2/nballerina.bir:0.1.0:Operand operand; Position position; |}";
    static final String MAP_GET_INSN = "record {| INSN_MAPPING_GET name; Register result; " +
            "[Register,wso2/nballerina.bir:0.1.0:StringOperand] operands; |}";
    static final String MAP_SET_INSN = "record {| INSN_MAPPING_SET name; [Register,wso2/nballerina.bir:0.1.0:" +
            "StringOperand,wso2/nballerina.bir:0.1.0:Operand] operands; Position position; |}";
    static final String INT_NOPANIC_ARITHMETIC_BINARY_INSN = "record {| INSN_INT_NO_PANIC_ARITHMETIC_BINARY name; " +
            "+|-|*|/|% op; Register result; wso2/nballerina.bir:0.1.0:IntOperand[2] operands; anydata...; |}";
}
