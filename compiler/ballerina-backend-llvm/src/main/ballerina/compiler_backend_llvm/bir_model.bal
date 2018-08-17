type BIRPackage record {
    BIRFunction[] functions;
    Name name;
    Name org;
    BType[] types;
    Name versionValue;
};

type BIRFunction record {
    int argsCount;
    BIRBasicBlock[] basicBlocks;
    boolean isDeclaration;
    BIRVariableDcl[] localVars;
    Name name;
    BInvokableType typeValue;
    Visibility visibility;
};

type BIRBasicBlock record {
    Name id;
    BIRInstruction[] instructions;
    BIRTerminator terminator;
};

type Name record {
    string value;
};

type BIRInstruction Move|BinaryOp|ConstantLoad;

type BIRTerminator Branch|GOTO|Return;

type InstructionKind "GOTO"|"CALL"|"BRANCH"|"RETURN"|"MOVE"|"CONST_LOAD"|"ADD"|"SUB"|"MUL"|"DIV"|"MOD"|"EQUAL"|
"NOT_EQUAL"|"GREATER_THAN"|"GREATER_EQUAL"|"LESS_THAN"|"LESS_EQUAL";

type BIRVariableDcl record {
    VarKind kind;
    Name name;
    BType typeValue;
};

type VarKind "LOCAL"|"ARG"|"TEMP"|"RETURN";

type BTypeInt "int";

type BTypeBoolean "boolean";

type BType BTypeInt | BTypeBoolean;

type BTypeSymbol record {
    boolean closure;
    DocAttachment documentationValue;
    int flags;
    boolean isLabel;
    SymbolKind kind;
    Name name;
//BSymbol owner;
    PackageID pkgID;
    Scope scopeValue;
    int tag;
    boolean tainted;
    BType typeValue;
};
//
type DocAttachment record {
    DocAttribute[] attributes;
    string description;
};

type DocAttribute record {
    string description;
    DocTag docTag;
    string name;
};

type DocTag "RETURN"|"PARAM"|"RECEIVER"|"FIELD"|"VARIABLE"|"ENDPOINT";

type SymbolKind "PACKAGE"|"STRUCT"|"OBJECT"|"RECORD"|"ENUM"|"CONNECTOR"|"ACTION"|"SERVICE"|"RESOURCE"|"FUNCTION"|
"WORKER"|"ANNOTATION"|"ANNOTATION_ATTRIBUTE"|"CONSTANT"|"PACKAGE_VARIABLE"|"TRANSFORMER"|"TYPE_DEF"|"PARAMETER"|
"LOCAL_VARIABLE"|"SERVICE_VARIABLE"|"CONNECTOR_VARIABLE"|"CAST_OPERATOR"|"CONVERSION_OPERATOR"|"XMLNS"|"SCOPE"|"OTHER";

type BSymbol record {
    boolean closure;
    DocAttachment documentationValue;
    int flags;
    SymbolKind kind;
    Name name;
//BSymbol owner;
    PackageID pkgID;
//Scope scopeValue;
    int tag;
    boolean tainted;
    BType typeValue;
};

type PackageID record {
    Name orgName;
    Name sourceFileName;
    Name versionValue;
};

type Scope record {
//ScopeEntry NOT_FOUND_ENTRY;
//Map entries;
    BSymbol owner;
};
//
//type ScopeEntry record {
//     ScopeEntry next;
//BSymbol symbol;
//};

type BInvokableType record {
    BType[] paramTypes;
    BType retType;
    int tag;
    BTypeSymbol tsymbol;
};

type Visibility "PACKAGE_PRIVATE"|"PRIVATE"|"PUBLIC";


type ConstantLoad object {
    public InstructionKind kind;
    public BIRVarRef lhsOp;
    public BType typeValue;
    public int value;
    new(kind, lhsOp, typeValue, value) {}
};

type BIRVarRef object {
    Kind kind;
    BType typeValue;
    BIRVariableDcl variableDcl;
    new(kind, typeValue, variableDcl) {}
};

type Kind "VAR_REF"|"CONST";

type Move object {
    InstructionKind kind;
    BIRVarRef lhsOp;
    BIROperand rhsOp;
    new(kind, lhsOp, rhsOp) {}
};

type BIROperand BIRVarRef;

type BinaryOp object {
    InstructionKind kind;
    BIRVarRef lhsOp;
    BIROperand rhsOp1;
    BIROperand rhsOp2;
    BType typeValue;
    new(kind, lhsOp, rhsOp1, rhsOp2, typeValue) {}
};

type Branch object {
    BIRBasicBlock falseBB;
    InstructionKind kind;
    BIROperand op;
    BIRBasicBlock trueBB;
    new(falseBB, kind, op, trueBB) {}
};
//
type GOTO object {
    InstructionKind kind;
    BIRBasicBlock targetBB;
    new(kind, targetBB) {}
};
//
type Return object {
    InstructionKind kind;
    new(kind) {}
};

InstructionKind ADD = "ADD";
InstructionKind LESS_THAN = "LESS_THAN";
