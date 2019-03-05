public type PackageId record {
    string name = "";
    string versionValue = "";
    string org = "";
};

public type Package record {
    ImportModule[] importModules = [];
    TypeDef[] typeDefs = [];
    GlobalVariableDcl[] globalVars = [];
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
    any...; // This is to type match with Object type fields in subtypes
};

public type Terminator record {
    TerminatorKind kind;
    any...; // This is to type match with Object type fields in subtypes
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

public const INS_KIND_MOVE = "MOVE";
public const INS_KIND_CONST_LOAD = "CONST_LOAD";
public const INS_KIND_NEW_MAP = "NEW_MAP";
public const INS_KIND_MAP_STORE = "MAP_STORE";
public const INS_KIND_NEW_ARRAY = "NEW_ARRAY";
public const INS_KIND_ARRAY_STORE = "ARRAY_STORE";
public const INS_KIND_MAP_LOAD = "MAP_LOAD";
public const INS_KIND_ARRAY_LOAD = "ARRAY_LOAD";
public const INS_KIND_NEW_ERROR = "NEW_ERROR";

public type InstructionKind INS_KIND_MOVE|INS_KIND_CONST_LOAD|INS_KIND_NEW_MAP|INS_KIND_MAP_STORE|INS_KIND_NEW_ARRAY
                                |INS_KIND_NEW_ERROR|INS_KIND_ARRAY_STORE|INS_KIND_MAP_LOAD|INS_KIND_ARRAY_LOAD
                                |BinaryOpInstructionKind;

public type BinaryOpInstructionKind ADD|SUB|MUL|DIV|EQUAL|NOT_EQUAL|GREATER_THAN|GREATER_EQUAL|LESS_THAN|LESS_EQUAL|
                                        AND|OR;

public type LocalVarKind "LOCAL";

public type TempVarKind "TEMP";

public type ReturnVarKind "RETURN";

public type GlobalVarKind "GLOBAL";

public type ArgVarKind "ARG";

public type VarKind LocalVarKind | TempVarKind | ReturnVarKind | GlobalVarKind | ArgVarKind;


public type ClosedSealed "CLOSED_SEALED";

public type OpenSealed "OPEN_SEALED";

public type UnSealed "UNSEALED";

public type ArrayState ClosedSealed | OpenSealed | UnSealed;

public type VariableDcl record {
    VarKind kind = "LOCAL";
    Name name = {};
    BType typeValue = "()";
    any...; // This is to type match with Object type fields in subtypes
};

public type GlobalVariableDcl record {
    VarKind kind = "GLOBAL";
    Name name = {};
    BType typeValue = "()";
    Visibility visibility = "PACKAGE_PRIVATE";
    !...;
};

public type BTypeAny "any";

public type BTypeNil "()";

public type BTypeInt "int";

public type BTypeFloat "float";

public type BTypeBoolean "boolean";

public type BTypeString "string";

public type BTypeByte "byte";

public type BArrayType record {
    ArrayState state;
    BType eType;
};

public type BMapType record {
    BType constraint;
};

public type BErrorType record {
    BType reasonType;
    BType detailType;
};

public type BRecordType record {
    Name name = {};
    boolean sealed;
    BType restFieldType;
    BRecordField[] fields;
};

public type BObjectType record {
    Name name = {};
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


public type BType BTypeInt | BTypeBoolean | BTypeAny | BTypeNil | BTypeByte | BTypeFloat | BTypeString | BUnionType |
                  BInvokableType | BArrayType | BRecordType | BObjectType | BMapType | BErrorType;


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

public type ConstantLoad record {
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    int | string | boolean | float | () value;
    !...;
};

public type NewMap record {
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    !...;
};

public type NewArray record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef sizeOp;
    BType typeValue;
    !...;
};

public type FieldAccess record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef keyOp;
    VarRef rhsOp;
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
    VarRef rhsOp;
    !...;
};

public type BinaryOp record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp1;
    VarRef rhsOp2;
    !...;
};

public type Call record {
    VarRef[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    PackageId pkgID;
    Name name;
    BasicBlock thenBB;
    !...;
};

public type Branch record {
    BasicBlock falseBB;
    TerminatorKind kind;
    VarRef op;
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

public type NewError record {
    InstructionKind kind;
    VarRef lhsOp;
    VarRef reasonOp;
    VarRef detailsOp;
    !...;
};
