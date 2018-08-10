type BIRPackage record {
    Name name;
    Name versionValue;
    BIRFunction[] functions;
    BType[] types;
};

type Name record {
    string value;
};

type BIRFunction record {
    Name name;
    boolean isDeclaration;
    Visibility visibility;
    BInvokableType typeValue;
    int argsCount;
    BIRVariableDcl[] localVars;
    BIRBasicBlock[] basicBlocks;
};

type Visibility "PACKAGE_PRIVATE"|"PRIVATE"|"PUBLIC";

type BInvokableType record {
    BType[] paramTypes;
    BType retType;
};

type BType "int"|"boolean";

type BTypeSymbol record {
    boolean isLabel;
};

type BIRVariableDcl record {
    BType typeValue;
    Name name;
    VarKind kind;
};

type VarKind "LOCAL"|"ARG"|"TEMP"|"RETURN";

type BIRBasicBlock record {
    Name id;
    BIRInstruction[] instructions;
    BIRTerminator terminator;
};


type BIRInstruction Move|BinaryOp;

type BIRTerminator GOTO|Branch|Return;

type InstructionKind "MOVE"|"GOTO"|();

type Kind "VAR_REF"|"CONST";


type OperatorKind "ADD"|"SUB"|"MUL"|"DIV"|"MOD"|"AND"|"OR"|"EQUAL"|"NOT_EQUAL"|
"GREATER_THAN"|"GREATER_EQUAL"|"LESS_THAN"|"LESS_EQUAL"|"IS_ASSIGNABLE"|
"NOT"|"LENGTHOF"|"UNTAINT"|"INCREMENT"|"DECREMENT"|"CHECK"|"ELVIS"|"BITWISE_AND"|
"BITWISE_OR"|"BITWISE_XOR"|"BITWISE_COMPLEMENT"|"BITWISE_LEFT_SHIFT"|"BITWISE_RIGHT_SHIFT"|
"BITWISE_UNSIGNED_RIGHT_SHIFT"|"CLOSED_RANGE"|"HALF_OPEN_RANGE"|"opValue";

OperatorKind LESS_THAN = "LESS_THAN";
OperatorKind ADD = "ADD";


type Move object {
    public BIRVarRef lhsOp;
    public BIROperand rhsOp;
    new(lhsOp, rhsOp) {}
};


type BIRVarRef object {
    public Kind kind;
    public BType typeValue;
    public BIRVariableDcl variableDcl;
    new(kind, typeValue, variableDcl) {}
};

type BIROperand BIRVarRef|BIRConstant;

type BIRConstant object {
    public int value;
    public Kind kind;
    public BType typeValue;
    new(kind, typeValue, value) {}
};

type GOTO object {
    public BIRBasicBlock targetBB;
    new(targetBB) {}
};

type BinaryOp object {
    public OperatorKind binaryOpKind;
    public InstructionKind instructionKind;
    public BIRVarRef lhsOp;
    public BIROperand rhsOp1;
    public BIROperand rhsOp2;
    public BType typeValue;
    new(binaryOpKind, instructionKind, lhsOp, rhsOp1, rhsOp2, typeValue) {}
};

type Branch object {
    public BIRBasicBlock falseBB;
    public BIROperand op;
    public BIRBasicBlock trueBB;
    new(falseBB, op, trueBB) {}
};

type Return object {
    new() {}
};
