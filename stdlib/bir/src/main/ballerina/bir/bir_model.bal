public type PackageId record {
    string name;
    string versionValue;
    string org;
};

public type Package record {
    Function[] functions;
    Name name;
    Name org;
    BType[] types;
    Name versionValue;
};

public type Function record {
    int argsCount;
    BasicBlock[] basicBlocks;
    boolean isDeclaration;
    VariableDcl[] localVars;
    Name name;
    BInvokableType typeValue;
    Visibility visibility;
};

public type BasicBlock record {
    Name id;
    Instruction[] instructions;
    Terminator terminator;
};

public type Name record {
    string value;
};

public type Instruction Move|BinaryOp|ConstantLoad;

public type Terminator Call|Branch|GOTO|Return;

public type ADD "ADD";
public type SUB "SUB";
public type MUL "MUL";
public type DIV "DIV";
public type EQUAL "EQUAL";
public type NOT_EQUAL "NOT_EQUAL";
public type GREATER_THAN "GREATER_THAN";
public type GREATER_EQUAL "GREATER_EQUAL";
public type LESS_THAN "LESS_THAN";
public type LESS_EQUAL "LESS_EQUAL";


public type InstructionKind "GOTO"|"CALL"|"BRANCH"|"RETURN"|"MOVE"|"CONST_LOAD"|BinaryOpInstructionKind;

public type BinaryOpInstructionKind ADD|SUB|MUL|DIV|EQUAL|NOT_EQUAL|GREATER_THAN|GREATER_EQUAL|LESS_THAN|LESS_EQUAL;


public type ArgVarKind "ARG";

public type VarKind "LOCAL" | "TEMP" | "RETURN" | ArgVarKind;

public type VariableDcl record {
    VarKind kind;
    Name name;
    BType typeValue;
};

public type BTypeNil "()";

public type BTypeInt "int";

public type BTypeBoolean "boolean";

public type BType BTypeInt | BTypeBoolean | BTypeNil;


public type BTypeSymbol record {
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
public type DocAttachment record {
    DocAttribute[] attributes;
    string description;
};

public type DocAttribute record {
    string description;
    DocTag docTag;
    string name;
};

public type DocTag "RETURN"|"PARAM"|"RECEIVER"|"FIELD"|"VARIABLE"|"ENDPOINT";

public type SymbolKind "PACKAGE"|"STRUCT"|"OBJECT"|"RECORD"|"ENUM"|"CONNECTOR"|"ACTION"|"SERVICE"|"RESOURCE"|"FUNCTION"|
"WORKER"|"ANNOTATION"|"ANNOTATION_ATTRIBUTE"|"CONSTANT"|"PACKAGE_VARIABLE"|"TRANSFORMER"|"TYPE_DEF"|"PARAMETER"|
"LOCAL_VARIABLE"|"SERVICE_VARIABLE"|"CONNECTOR_VARIABLE"|"CAST_OPERATOR"|"CONVERSION_OPERATOR"|"XMLNS"|"SCOPE"|"OTHER";

public type BSymbol record {
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

public type PackageID record {
    Name orgName;
    Name sourceFileName;
    Name versionValue;
};

public type Scope record {
//ScopeEntry NOT_FOUND_ENTRY;
//Map entries;
    BSymbol owner;
};
//
//type ScopeEntry record {
//     ScopeEntry next;
//BSymbol symbol;
//};

public type BInvokableType record {
    BType[] paramTypes;
    BType retType;
};

public type Visibility "PACKAGE_PRIVATE"|"PRIVATE"|"PUBLIC";


public type ConstantLoad object {
    public InstructionKind kind;
    public VarRef lhsOp;
    public BType typeValue;
    public int value;
    public new(kind, lhsOp, typeValue, value) {}
};

public type VarRef object {
    public Kind kind;
    public BType typeValue;
    public VariableDcl variableDcl;
    public new(kind, typeValue, variableDcl) {}
};

public type Kind "VAR_REF"|"CONST";

public type Move object {
    public InstructionKind kind;
    public VarRef lhsOp;
    public Operand rhsOp;
    public new(kind, lhsOp, rhsOp) {}
};

public type Operand VarRef;

public type BinaryOp object {
    public BinaryOpInstructionKind kind;
    public VarRef lhsOp;
    public Operand rhsOp1;
    public Operand rhsOp2;
    public BType typeValue;
    public new(kind, lhsOp, rhsOp1, rhsOp2, typeValue) {}
};

public type Call object {
    public Operand[] args;
    public InstructionKind kind;
    public VarRef? lhsOp;
    public Name name;
    public BasicBlock thenBB;
    public new (args, kind, lhsOp, name, thenBB){}
};

public type Branch object {
    public BasicBlock falseBB;
    public InstructionKind kind;
    public Operand op;
    public BasicBlock trueBB;
    public new(falseBB, kind, op, trueBB) {}
};
//
public type GOTO object {
    public InstructionKind kind;
    public BasicBlock targetBB;
    public new(kind, targetBB) {}
};
//
public type Return object {
    public InstructionKind kind;
    public new(kind) {}
};
