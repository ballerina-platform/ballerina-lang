public type PackageId record {
    string name = "";
    string versionValue = "";
    string org = "";
};

public type Package record {
    Function?[] functions = [];
    Name name = {};
    Name org = {};
    BType?[] types = [];
    Name versionValue = {};
};

public type Function record {
    int argsCount = 0;
    BasicBlock?[] basicBlocks = [];
    boolean isDeclaration = false;
    VariableDcl?[] localVars = [];
    Name name = {};
    BInvokableType typeValue = {};
    Visibility visibility = "PACKAGE_PRIVATE";
};

public type BasicBlock record {
    Name id = {};
    Instruction?[] instructions = [];
    Terminator terminator = new Return("RETURN");
};

public type Name record {
    string value = "";
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
    VarKind kind = "LOCAL";
    Name name = {};
    BType typeValue = "()";
};

public type BTypeNil "()";

public type BTypeInt "int";

public type BTypeBoolean "boolean";

public type BType BTypeInt | BTypeBoolean | BTypeNil;


public type BTypeSymbol record {
    boolean closure = false;
    DocAttachment documentationValue = {};
    int flags = 0;
    boolean isLabel = false;
    SymbolKind kind = "OTHER";
    Name name = {};
//BSymbol owner;
    PackageID pkgID = {};
    Scope scopeValue = {};
    int tag = 0;
    boolean tainted = false;
    BType typeValue = "()";
};
//
public type DocAttachment record {
    DocAttribute?[] attributes = [];
    string description = "";
};

public type DocAttribute record {
    string description = "";
    DocTag docTag;
    string name = "";
};

public type DocTag "RETURN"|"PARAM"|"RECEIVER"|"FIELD"|"VARIABLE"|"ENDPOINT";

public type SymbolKind "PACKAGE"|"STRUCT"|"OBJECT"|"RECORD"|"ENUM"|"CONNECTOR"|"ACTION"|"SERVICE"|"RESOURCE"|"FUNCTION"|
"WORKER"|"ANNOTATION"|"ANNOTATION_ATTRIBUTE"|"CONSTANT"|"PACKAGE_VARIABLE"|"TRANSFORMER"|"TYPE_DEF"|"PARAMETER"|
"LOCAL_VARIABLE"|"SERVICE_VARIABLE"|"CONNECTOR_VARIABLE"|"CAST_OPERATOR"|"CONVERSION_OPERATOR"|"XMLNS"|"SCOPE"|"OTHER";

public type BSymbol record {
    boolean closure = false;
    DocAttachment documentationValue = {};
    int flags = 0;
    SymbolKind kind = "OTHER";
    Name name = {};
//BSymbol owner;
    PackageID pkgID = {};
//Scope scopeValue;
    int tag = 0;
    boolean tainted = false;
    BType typeValue = "()";
};

public type PackageID record {
    Name orgName = {};
    Name sourceFileName = {};
    Name versionValue = {};
};

public type Scope record {
//ScopeEntry NOT_FOUND_ENTRY;
//Map entries;
    BSymbol owner = {};
};
//
//type ScopeEntry record {
//     ScopeEntry next;
//BSymbol symbol;
//};

public type BInvokableType record {
    BType?[] paramTypes = [];
    BType retType = "()";
};

public type Visibility "PACKAGE_PRIVATE"|"PRIVATE"|"PUBLIC";


public type ConstantLoad object {
    public InstructionKind kind;
    public VarRef lhsOp;
    public BType typeValue;
    public int value;
    public function __init(InstructionKind kind, VarRef lhsOp, BType typeValue, int value) {
        self.kind = kind;
        self.lhsOp = lhsOp;
        self.typeValue = typeValue;
        self.value = value;
    }
};

public type VarRef object {
    public Kind kind;
    public BType typeValue;
    public VariableDcl variableDcl;
    public function __init(Kind kind, BType typeValue, VariableDcl variableDcl) {
        self.kind = kind;
        self.typeValue = typeValue;
        self.variableDcl = variableDcl;
    }
};

public type Kind "VAR_REF"|"CONST";

public type Move object {
    public InstructionKind kind;
    public VarRef lhsOp;
    public Operand rhsOp;
    public function __init(InstructionKind kind, VarRef lhsOp, Operand rhsOp) {
        self.kind = kind;
        self.lhsOp = lhsOp;
        self.rhsOp = rhsOp;
    }
};

public type Operand VarRef;

public type BinaryOp object {
    public BinaryOpInstructionKind kind;
    public VarRef lhsOp;
    public Operand rhsOp1;
    public Operand rhsOp2;
    public BType typeValue;
    public function __init(BinaryOpInstructionKind kind, VarRef lhsOp, Operand rhsOp1, Operand rhsOp2, BType typeValue) {
        self.kind = kind;
        self.lhsOp = lhsOp;
        self.rhsOp1 = rhsOp1;
        self.rhsOp2 = rhsOp2;
        self.typeValue= typeValue;
    }
};

public type Call object {
    public Operand?[] args;
    public InstructionKind kind;
    public VarRef? lhsOp;
    public Name name;
    public BasicBlock thenBB;
    public function __init(Operand?[] args, InstructionKind kind, VarRef? lhsOp, Name name, BasicBlock thenBB) {
        self.args = args;
        self.kind = kind;
        self.lhsOp = lhsOp;
        self.name = name;
        self.thenBB = thenBB;
    }
};

public type Branch object {
    public BasicBlock falseBB;
    public InstructionKind kind;
    public Operand op;
    public BasicBlock trueBB;
    public function __init(BasicBlock falseBB, InstructionKind kind, Operand op, BasicBlock trueBB) {
        self.falseBB = falseBB;
        self.kind = kind;
        self.op = op;
        self.trueBB = trueBB;
    }
};
//
public type GOTO object {
    public InstructionKind kind;
    public BasicBlock targetBB;
    public function __init(InstructionKind kind, BasicBlock targetBB) {
        self.kind = kind;
        self.targetBB = targetBB;
    }
};
//
public type Return object {
    public InstructionKind kind;
    public function __init(InstructionKind kind) {
        self.kind = kind;
    }
};
