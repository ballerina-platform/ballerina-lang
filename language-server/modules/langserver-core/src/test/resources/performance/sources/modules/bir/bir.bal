import wso2/nballerina.types as t;
import wso2/nballerina.err;

public type SemType t:SemType;
public type Position err:Position;

public type Module object {
    public function getId() returns ModuleId;
    // A SemType of a potentially recursive type uses integers to refer to definitions
    // which are in arrays in this.
    public function getTypeCheckContext() returns t:TypeCheckContext;
    public function getFunctionDefns() returns readonly & FunctionDefn[];
    public function generateFunctionCode(int i) returns FunctionCode|err:Semantic|err:Unimplemented;
    public function getPrefixForModuleId(ModuleId id) returns string?;
};

public type ModuleId readonly & record {|
    string? organization = ();
    [string, string...] names;
    // this omits the version, because programs cannot have two versions of the same module
|};

public type ExternalSymbol readonly & record {|
    ModuleId module;
    string identifier;
|};

public type ModuleDefn record {|
    InternalSymbol symbol;
    readonly...;
|};

# A label is an index of a basic block in the basicBlock.
public type Label int;

# The definition of a function.
public type FunctionDefn readonly & record {|
    *ModuleDefn;
    # Name within the module
    InternalSymbol symbol;
    # The signature of the function
    FunctionSignature signature;
    # The position of the definition
    Position position;
|};

public type InternalSymbol readonly & record {|
    boolean isPublic;
    string identifier;
|};

public type Symbol InternalSymbol|ExternalSymbol;

public function symbolToString(Module mod, Symbol sym) returns string {
    string prefix;
    if sym is InternalSymbol {
        prefix = "";
    }
    else {
        ModuleId modId = sym.module;
        string? importPrefix = mod.getPrefixForModuleId(modId);
        if importPrefix == () {
            string? org = modId.organization;
            string orgString = org == () ? "" : org + "/";
            prefix = "{" + orgString  + ".".'join(...sym.module.names) + "}";
        }
        else {
            prefix = importPrefix + ":";
        }
    }
    return prefix + sym.identifier;
}

public type FunctionRef readonly & record {|
    Symbol symbol;
    FunctionSignature signature;
|};

# A function's code is represented as a factored control flow graph.
# (as described in Choi et al 1999 https://dl.acm.org/doi/abs/10.1145/381788.316171)
# This is like a control flow graph, except that basic blocks
# can contain instructions that can potentially panic.
# Execution starts in basic block 0 with with value of param i
# in register i (0-based). (Not thinking about varargs yet.)
# Control flow between basic blocks is explicit: it does not
# flow implicitly between the members of `blocks`.
public type FunctionCode record {|
    # Basic blocks indexed by label
    BasicBlock[] blocks = [];
    # Registers indexed by number
    Register[] registers = [];
|};

# This represents the signature of a function definition.
# We don't need to convert this to a `SemType` unless
# the definition is converted to a function value,
# by referencing the name of the function as a variable
# reference.
public type FunctionSignature readonly & record {|
    SemType returnType;
    SemType[] paramTypes;
    SemType? restParamType = ();
|};

# A basic block.
# Normal control flow proceeds implicitly through the members of the insns array.
# Basic blocks can contain potentially panicking instructions (PPIs).
# Whether an instruction is a PPI is determined based on the name of the instruction.
# If a basic block does contain a PPI, then onPanic is the label of the
# basic block to which control should flow if any of those instructions panic:
# this basic block must start with a CatchInsn.
public type BasicBlock record {|
    # Label for the BB, unique within the function
    readonly Label label;
    # List of the instructions in this basic block
    Insn[] insns = [];
    # non-nil if this contains any PPI
    Label? onPanic = ();
    # Backend can use this to generate human-readable label
    string? name = ();
|};

public type InsnRef readonly & record {|
    // Label of the block containing the insn
    Label block;
    // Index in the block of the insn
    int index;
|};

public function createBasicBlock(FunctionCode code, string? name = ()) returns BasicBlock {
    int label = code.blocks.length();
    BasicBlock bb = { label, name };
    code.blocks.push(bb);
    return bb;
}

public function lastInsnRef(BasicBlock bb) returns InsnRef {
    return { block: bb.label, index: bb.insns.length() - 1 };
}

public type Register readonly & record {|
    # Unique identifier within a function
    # Always >= 0
    int number;
    SemType semType;
    string? varName;
|};

public function createRegister(FunctionCode code, SemType semType, string? varName = ()) returns Register {
    int number = code.registers.length();
    Register r = { number, semType, varName };
    code.registers.push(r);
    return r;
}

public type ArithmeticBinaryOp "+" | "-" | "*" | "/" | "%";
public type BitwiseBinaryOp "|" | "^" | "&" | BitwiseShiftOp;
public type BitwiseShiftOp "<<" | ">>" | ">>>";
public type OrderOp "<=" | ">=" | "<" | ">";
public type EqualityOp "==" | "!=" | "===" | "!==";

public enum InsnName {
    INSN_INT_ARITHMETIC_BINARY,
    INSN_INT_NO_PANIC_ARITHMETIC_BINARY,
    INSN_INT_BITWISE_BINARY,
    INSN_FLOAT_ARITHMETIC_BINARY,
    INSN_FLOAT_NEGATE,
    INSN_CONVERT_TO_INT,
    INSN_CONVERT_TO_FLOAT,
    INSN_COMPARE,
    INSN_EQUALITY,
    INSN_BOOLEAN_NOT,
    INSN_LIST_CONSTRUCT_RW,
    INSN_LIST_GET,
    INSN_LIST_SET,
    INSN_MAPPING_CONSTRUCT_RW,
    INSN_MAPPING_GET,
    INSN_MAPPING_SET,
    INSN_STR_CONCAT,
    INSN_ERROR_CONSTRUCT,
    INSN_RET,
    INSN_ABNORMAL_RET,
    INSN_CALL,
    INSN_INVOKE,
    INSN_ASSIGN,
    INSN_COND_NARROW,
    INSN_TYPE_CAST,
    INSN_TYPE_TEST,
    INSN_BRANCH,
    INSN_COND_BRANCH,
    INSN_CATCH,
    INSN_PANIC
}

# All instructions are a subtype of this.
public type InsnBase record {
    # The name of the instruction.
    # The name says what kind of instruction it is.
    # Whether an instruction is a terminator or a PPI is determined
    # just by its name.
    InsnName name;
};

public type Insn 
    IntArithmeticBinaryInsn|IntNoPanicArithmeticBinaryInsn|IntBitwiseBinaryInsn
    |FloatArithmeticBinaryInsn|FloatNegateInsn
    |ConvertToIntInsn|ConvertToFloatInsn
    |BooleanNotInsn|CompareInsn|EqualityInsn
    |ListConstructInsn|ListGetInsn|ListSetInsn
    |MappingConstructInsn|MappingGetInsn|MappingSetInsn
    |StringConcatInsn|RetInsn|AbnormalRetInsn|CallInsn
    |AssignInsn|CondNarrowInsn|TypeCastInsn|TypeTestInsn
    |BranchInsn|CondBranchInsn|CatchInsn|PanicInsn;

public type Operand ConstOperand|Register;
public type SimpleConstOperand ()|boolean|int|float;
public type ConstOperand SimpleConstOperand|string;
public type StringOperand string|Register;
public type IntOperand int|Register;
public type FloatOperand float|Register;
public type BooleanOperand boolean|Register;
public type FunctionOperand FunctionRef|Register;

# Perform a arithmetic operand on ints with two operands.
# This is a PPI.
public type IntArithmeticBinaryInsn readonly & record {|
    *InsnBase;
    INSN_INT_ARITHMETIC_BINARY name = INSN_INT_ARITHMETIC_BINARY;
    ArithmeticBinaryOp op;
    Register result;
    IntOperand[2] operands;
    Position position;
|};

# Concatenate strings, returns a new string
public type StringConcatInsn readonly & record {|
    *InsnBase;
    INSN_STR_CONCAT name = INSN_STR_CONCAT;
    Register result;
    StringOperand[2] operands;
|};

# This is a non-PPI variant of IntArithmeticBinaryInsn.
# It is an optimization to be used only when the compiler can prove that a panic is impossible;
# the NO_PANIC version of % must not be used if first operand is int:MIN_VALUE and second operand is -1.
public type IntNoPanicArithmeticBinaryInsn readonly & record {|
    *InsnBase;
    INSN_INT_NO_PANIC_ARITHMETIC_BINARY name = INSN_INT_NO_PANIC_ARITHMETIC_BINARY;
    ArithmeticBinaryOp op;
    Register result;
    IntOperand[2] operands;
|};

public type IntBitwiseBinaryInsn readonly & record {|
    *InsnBase;
    INSN_INT_BITWISE_BINARY name = INSN_INT_BITWISE_BINARY;
    BitwiseBinaryOp op;
    Register result;
    IntOperand[2] operands;
|};

# Perform logical not operation on a boolean.
public type BooleanNotInsn readonly & record {|
    *InsnBase;
    INSN_BOOLEAN_NOT name = INSN_BOOLEAN_NOT;
    Register result;
    Register operand;
|};

// This is not a PPI
public type FloatArithmeticBinaryInsn readonly & record {|
    *InsnBase;
    INSN_FLOAT_ARITHMETIC_BINARY name = INSN_FLOAT_ARITHMETIC_BINARY;
    ArithmeticBinaryOp op;
    Register result;
    FloatOperand[2] operands;
    Position position;
|};

public type FloatNegateInsn readonly & record {|
    *InsnBase;
    INSN_FLOAT_NEGATE name = INSN_FLOAT_NEGATE;
    Register result;
    Register operand;
|};


# If the operand is a float or decimal, then convert it to an int.
# Otherwise leave the operand unchanged.
# The intersection of the operand type with float|decimal must be non-empty.
# The result type must be `(T - (float|decimal))|int`,
# where T is the operand type.
# This panics if the conversion cannot be performed, so is a PPI.
public type ConvertToIntInsn readonly & record {|
    *InsnBase;
    INSN_CONVERT_TO_INT name = INSN_CONVERT_TO_INT;
    Register result;
    Register operand;
    Position position;
|};

# If the operand is an int or decimal, then convert it to a float.
# Otherwise leave the operand unchanged.
# The intersection of the operand type with int|decimal must be non-empty.
# The result type must be `(T - (int|decimal))|float`,
# where T is the operand type.
# This is not a PPI.
public type ConvertToFloatInsn readonly & record {|
    *InsnBase;
    INSN_CONVERT_TO_FLOAT name = INSN_CONVERT_TO_FLOAT;
    Register result;
    Register operand;
|};

public type OrderType UniformOrderType|OptOrderType;
public type UniformOrderType t:UT_FLOAT|t:UT_INT|t:UT_BOOLEAN|t:UT_STRING;
public type OptOrderType readonly & record {|
    UniformOrderType opt;
|};

# This does ordered comparision
# Equality and inequality are done by equal
public type CompareInsn readonly & record {|
    *InsnBase;
    INSN_COMPARE name = INSN_COMPARE;
    OrderOp op;
    OrderType orderType;
    Register result;
    Operand[2] operands;
|};

# Constructs a new mutable list value.
public type ListConstructInsn readonly & record {|
    INSN_LIST_CONSTRUCT_RW name = INSN_LIST_CONSTRUCT_RW;
    // The type of the result gives the inherent type of the constructed list
    Register result;
    Operand[] operands;
|};

# Gets a member of a list at a specified index.
# This is a PPI (since the index may be out of bounds).
public type ListGetInsn readonly & record {|
    INSN_LIST_GET name = INSN_LIST_GET;
    Register result;
    Register list;
    IntOperand operand;
    Position position;
|};

# Sets a member of a list at a specified index.
# This is a PPI (since the index may be out of bounds).
public type ListSetInsn readonly & record {|
    INSN_LIST_SET name = INSN_LIST_SET;
    Register list;
    IntOperand index;
    // operand is the value to store in the list
    Operand operand;
    Position position;
|};

# Constructs a new mutable list value.
public type MappingConstructInsn readonly & record {|
    INSN_MAPPING_CONSTRUCT_RW name = INSN_MAPPING_CONSTRUCT_RW;
    // The type of the result gives the inherent type of the constructed list
    Register result;
    string[] fieldNames;
    Operand[] operands;
|};

# Gets a member of a mapping with a specified key.
# This returns nil if there is no such member.
# So this is not a PPI
public type MappingGetInsn readonly & record {|
    INSN_MAPPING_GET name = INSN_MAPPING_GET;
    Register result;
    [Register, StringOperand] operands;
|};

# Sets a member of a mapping with a specified key.
# This is a PPI.
public type MappingSetInsn readonly & record {|
    INSN_MAPPING_SET name = INSN_MAPPING_SET;
    [Register, StringOperand, Operand] operands;
    Position position;
|};

# Constructs an error value.
# Operand must be of type string.
public type ErrorConstructInsn readonly & record {|
    INSN_ERROR_CONSTRUCT name = INSN_ERROR_CONSTRUCT;
    Register result;
    Operand operand;
    Position position;
|};

# This does equality expressions.
# This includes `==`, `!=`, `===` and `!==`
// XXX Complex cases (comparing structures deeply) can use memory in
// a way that cannot be bounded at compile time so may result in memory exhaustion
// Should this mean this is a PPI? Should we distinguish these as different
// kind of instruction.
public type EqualityInsn readonly & record {|
    *InsnBase;
    INSN_EQUALITY name = INSN_EQUALITY;
    EqualityOp op;
    Register result;
    Operand[2] operands;
|};

# Call a function.
# This is a not a terminator.
# This is a PPI. A panic in the called function
# goes to the onPanic label in the basic block.
# Regardless of where the function itself panics,
# any function call could result in a stack overflow panic.
# XXX This does not handle functions that don't return
# (i.e. with return type of never)
public type CallInsn readonly & record {|
    *InsnBase;
    # Position in the source that resulted in the instruction
    Position? position;
    INSN_CALL name = INSN_CALL;
    Register result;
    FunctionOperand func;
    Operand[] args;
|};

# Assign a value to a register.
# Typing rule:
# typeof(operand) <: typeof(result)
public type AssignInsn readonly & record {|
    *InsnBase;
    INSN_ASSIGN name = INSN_ASSIGN;
    Register result;
    Operand operand;
|};

# A type cast that may fail.
# Don't need to allow for operand to be a const
# Since we can do that at compile-time.
# This is a PPI.
# Typing rules:
# typeof(result) <: semType
# typeof(result) <: typeof(operand)
# semType not empty
public type TypeCastInsn readonly & record {|
    *InsnBase;
    INSN_TYPE_CAST name = INSN_TYPE_CAST;
    Register result;
    Register operand;
    SemType semType;
    Position position;
|};


# Tests whether a value belongs to a type
# Used for `is` expressions
# Typing rule:
# typeof(result) <: boolean
# XXX some type tests are potentially complex
# and can require memory allocation and thus can potentially
# panic. Probably need to distinguish these.
public type TypeTestInsn readonly & record {|
    *InsnBase;
    INSN_TYPE_TEST name = INSN_TYPE_TEST;
    # Gets result of test.
    # Must be exactly type boolean
    Register result;
    # Holds value to be tested.
    Register operand;
    SemType semType;
    boolean negated;
|};


# A type narrowing that is based on the result of one or more previous instructions.
# The result in each case is a boolean and the instruction is a TypeTestInsn or an EqualInsn.
# Usually this would require a TypeCastInsn, but the compiler emits this
# to support Ballerina's type narrowing feature, when it knows the cast
# would succeed.
# The basis field describes the basis for the narrowing.
# This can can be verified in two parts.
# First, verify that the data flow through the basic blocks guarantees instructions
# must have had the results describe in the basis field.
# In particular, a block referenced in an InsnResult must end
# with a CondBranchInsn whose operand comes from the result of the condition
# (possibly via a BooleanNotInsn).
# Second, verify that the results described in the basis field justify the
# narrowing.
public type CondNarrowInsn readonly & record {|
    *InsnBase;
    INSN_COND_NARROW name = INSN_COND_NARROW;
    Register result;
    Register operand;
    Result basis;
|};

public type Result InsnResult|OrResult|AndResult;

public type OrResult readonly & record {|
    Result[] or;
|};

public type AndResult readonly & record {|
    Result[] and;
|};

// An instruction that is the basis of a narrowing.
// The execution of the instruction having the specified result
// is the basis for the narrowing.
public type InsnResult readonly & record {|
    // Where the insn is
    InsnRef insn;
    // Result of the insn
    boolean result;
|};

# Return normally from a function.
# This is a terminator.
# Typing rule:
# typeof(operand) <: typeof(functionReturnType)
public type RetInsn readonly & record {|
    *InsnBase;
    INSN_RET name = INSN_RET;
    Operand operand;
|};

# Return abnormally from the function
# The type of the operand need not belong to the functions return type.
# The associated error value is in the operand register.
# This is a terminator.
public type AbnormalRetInsn readonly & record {|
    *InsnBase;
    INSN_ABNORMAL_RET name = INSN_ABNORMAL_RET;
    # Operand is error value
    Register operand;
|};

# Performs a panic.
# This is a PPI.
# Control flow allows follows the onPanic label of its basic block.
# This is a terminator.
# The operand contains the associated error value.
public type PanicInsn readonly & record {|
    *InsnBase;
    INSN_PANIC name = INSN_PANIC; 
    # Must be of type error
    Register operand;
|};


# A CatchInsn is allowed as the first insn of a block that
# is the target of an onPanic label of a basic block.
# Executing the catch instruction causes the error value associated
# with the panic to be stored in the result register.
# This is a very simplified form of a LLVM landingpad.
public type CatchInsn readonly & record {|
    *InsnBase;
    INSN_CATCH name = INSN_CATCH;
    Register result;
|};

# Conditionally branch to one of two labels based on a boolean operand.
# If the operand is const, then use a Jump instead.
# This is a terminator.
public type CondBranchInsn readonly & record {|
    *InsnBase;
    INSN_COND_BRANCH name = INSN_COND_BRANCH;
    Register operand;
    Label ifTrue;
    Label ifFalse;
|};

# Unconditional branch to a label
# This is a terminator.
public type BranchInsn readonly & record {|
    *InsnBase;
    INSN_BRANCH name = INSN_BRANCH;
    Label dest;
|};


public function isBasicBlockPotentiallyPanicking(BasicBlock block) returns boolean {
    foreach Insn insn in block.insns {
        if isInsnPotentiallyPanicking(insn) {
            return true;
        }
    }
    return false;
}

final readonly & map<true> PPI_INSNS = {
    // When we implement trap, we will need to treat call as potentially
    // panicking but for now we don't
    // If we allow this, we need to be careful about not generating
    // code from the catch block if the only PPIs in the basic block are calls.
    // [INSN_CALL]: true,
    [INSN_PANIC]: true,
    [INSN_INT_ARITHMETIC_BINARY]: true,
    [INSN_CONVERT_TO_INT]: true,
    [INSN_TYPE_CAST]: true,
    [INSN_LIST_GET]: true,
    [INSN_LIST_SET]: true,
    [INSN_MAPPING_SET]: true
};

public function isInsnPotentiallyPanicking(Insn insn) returns boolean {
    return PPI_INSNS[insn.name] == true;
}
