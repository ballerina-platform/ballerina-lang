public type PackageId record {
    string name = "";
    string versionValue = "";
    string org = "";
};

public type Package record {
    ImportModule[] importModules = [];
    TypeDef[] typeDefs = [];
    Function[] functions = [];
    Name name = {};
    Name org = {};
    BType[] types = [];
    Name versionValue = {};
};

public type ImportModule record {
    Name modOrg;
    Name modName;
    Name modVersion;
};

public type TypeDef record {
    Name name;
    Visibility visibility;
    BType typeValue;
};

public type Function record {
    int argsCount = 0;
    BasicBlock[] basicBlocks = [];
    boolean isDeclaration = false;
    VariableDcl[] localVars = [];
    Name name = {};
    BInvokableType typeValue = {};
    Visibility visibility = "PACKAGE_PRIVATE";
};

public type BasicBlock record {
    Name id = {};
    Instruction[] instructions = [];
    Terminator terminator = {kind:"RETURN"};
};

public type Name record {
    string value = "";
};

public type Instruction record  {
    InstructionKind kind;
    any...;
};

public type Terminator record {
    TerminatorKind kind;
    any...;
};

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
public type AND "AND";
public type OR "OR";

public type TerminatorKind "GOTO"|"CALL"|"BRANCH"|"RETURN";

public type InstructionKind "MOVE"|"CONST_LOAD"|"NEW_MAP"|"MAP_STORE"|"NEW_ARRAY"|"ARRAY_STORE"|BinaryOpInstructionKind;

public type BinaryOpInstructionKind ADD|SUB|MUL|DIV|EQUAL|NOT_EQUAL|GREATER_THAN|GREATER_EQUAL|LESS_THAN|LESS_EQUAL|
                                        AND|OR;


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

public type BTypeString "string";

public type BArrayType record {
   BType eType;
};

public type BMapType record {
   BType constraint;
};

public type BRecordType record {
    boolean sealed;
    BType restFieldType;
    BRecordField[] fields;
};

public type BObjectType record {
    BObjectField[] fields;    
};

public type BRecordField record {
    Name name;
    BType typeValue;
    //TODO add position
};

public type BObjectField record {
    Name name;    
    Visibility visibility;
    BType typeValue;
    //TODO add position
};

public type BUnionType record {
   BType[]  members;
};


public type BType BTypeInt | BTypeBoolean | BTypeNil | "byte" | "float" | BTypeString | BUnionType |
                  BInvokableType | BArrayType | BRecordType | BObjectType | BMapType;


public type BTypeSymbol record {
    boolean closure = false;
    DocAttachment documentationValue = {};
    int flags = 0;
    boolean isLabel = false;
    SymbolKind kind = "OTHER";
    Name name = {};
//BSymbol owner;
    ModuleID pkgID = {};
    Scope scopeValue = {};
    int tag = 0;
    boolean tainted = false;
    BType typeValue = "()";
};
//
public type DocAttachment record {
    DocAttribute[] attributes = [];
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
    ModuleID pkgID = {};
//Scope scopeValue;
    int tag = 0;
    boolean tainted = false;
    BType typeValue = "()";
};

public type ModuleID record {
    string org = "";
    string name = "";
    string modVersion = "";
    boolean isUnnamed = false;
    string sourceFilename = "";
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
    BType[] paramTypes = [];
    BType retType = "()";
};

public type Visibility "PACKAGE_PRIVATE"|"PRIVATE"|"PUBLIC";

// public type Instruction Move|BinaryOp|ConstantLoad|NewMap|MapStore;

public type ConstantLoad record {
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    int | string value;
    !...;
};

public type NewMap record {
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    !...;
};

public type MapStore record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef keyOp;
    VarRef rhsOp;
    BType typeValue; //TODO do we need this?
    !...;
};

public type NewArray record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef sizeOp;
    BType typeValue;
    !...;
};

public type ArrayStore record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef keyOp;
    VarRef rhsOp;
    BType typeValue; //TODO do we need this?
    !...;
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

public type Move record {
    InstructionKind kind;
    VarRef lhsOp;
    Operand rhsOp;
    !...;
};

public type Operand VarRef;

public type BinaryOp record {
    InstructionKind kind;
    VarRef lhsOp;
    Operand rhsOp1;
    Operand rhsOp2;
    !...;
};

public type Call record {
    Operand[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    Name name;
    BasicBlock thenBB;
    !...;
};

public type Branch record {
    BasicBlock falseBB;
    TerminatorKind kind;
    Operand op;
    BasicBlock trueBB;
    !...;
};

public type GOTO record {
    TerminatorKind kind;
    BasicBlock targetBB;
    !...;
};

public type Return record {
    TerminatorKind kind;
    !...;
};
