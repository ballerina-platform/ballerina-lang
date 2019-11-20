// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Package record {|
    //TODO: change to ModuleID[]
    ImportModule[] importModules = [];
    TypeDef?[] typeDefs = [];
    GlobalVariableDcl?[] globalVars;
    Function?[] functions = [];
    //TODO: change to ModuleID
    Name name = {};
    Name org = {};
    BType?[] types = [];
    Name versionValue = {};
|};

public type ImportModule record {
    Name modOrg;
    Name modName;
    Name modVersion;
};

public type TypeDef record {
    Name name = {};
    DiagnosticPos pos;
    int flags = PRIVATE;
    BType typeValue = "()";
    Function?[]? attachedFuncs = ();
    BType?[] typeRefs;
};

public type TypeRef record {|
    Name name = {};
    ModuleID externalPkg;
|};

public type Function record {|
    DiagnosticPos pos;
    int argsCount = 0;
    BasicBlock?[] basicBlocks = [];
    FunctionParam?[] params = [];
    BasicBlock?[][] paramDefaultBBs = [];
    ErrorEntry?[] errorEntries = [];
    VariableDcl?[] localVars = [];
    Name name = {};
    Name workerName = {};
    BInvokableType typeValue = {};
    int flags = PRIVATE;
    ChannelDetail[] workerChannels;
    VariableDcl? receiver;
    boolean restParamExist;
    AnnotationAttachment?[] annotAttachments = [];
|};

public type BasicBlock record {|
    Name id = {};
    Instruction?[] instructions = [];
    Terminator terminator = {pos:{}, kind:"RETURN"};
|};

public type ErrorEntry record {|
    BasicBlock trapBB;
    VarRef errorOp;
    BasicBlock targetBB;
|};

public type ChannelDetail record {|
    Name name;
    boolean onSameStrand;
    boolean isSend;
|};

public type Name record {|
    string value = "";
|};

public type AnnotationAttachment record {|
    ModuleID moduleId;
    DiagnosticPos pos;
    Name annotTagRef;
    AnnotationValue?[] annotValues = [];
|};

public type AnnotationValue AnnotationLiteralValue | AnnotationRecordValue | AnnotationArrayValue;

public type AnnotationLiteralValue record {|
    BType literalType;
    anydata literalValue;
|};

public type AnnotationRecordValue record {|
    map<AnnotationValue> annotValueMap = {};
|};

public type AnnotationArrayValue record {|
    AnnotationValue?[]  annotValueArray = [];
|};

public const BINARY_ADD = "ADD";
public const BINARY_SUB = "SUB";
public const BINARY_MUL = "MUL";
public const BINARY_DIV = "DIV";
public const BINARY_MOD = "MOD";
public const BINARY_EQUAL = "EQUAL";
public const BINARY_NOT_EQUAL = "NOT_EQUAL";
public const BINARY_GREATER_THAN = "GREATER_THAN";
public const BINARY_GREATER_EQUAL = "GREATER_EQUAL";
public const BINARY_LESS_THAN = "LESS_THAN";
public const BINARY_LESS_EQUAL = "LESS_EQUAL";
public const BINARY_REF_EQUAL = "REF_EQUAL";
public const BINARY_REF_NOT_EQUAL = "REF_NOT_EQUAL";
public const BINARY_CLOSED_RANGE = "CLOSED_RANGE";
public const BINARY_HALF_OPEN_RANGE = "HALF_OPEN_RANGE";
public const BINARY_ANNOT_ACCESS = "ANNOT_ACCESS";
public const BINARY_BITWISE_AND = "BITWISE_AND";
public const BINARY_BITWISE_OR = "BITWISE_OR";
public const BINARY_BITWISE_XOR = "BITWISE_XOR";
public const BINARY_BITWISE_LEFT_SHIFT = "BITWISE_LEFT_SHIFT";
public const BINARY_BITWISE_RIGHT_SHIFT = "BITWISE_RIGHT_SHIFT";
public const BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT = "BITWISE_UNSIGNED_RIGHT_SHIFT";

public type BinaryOpInstructionKind BINARY_ADD|BINARY_SUB|BINARY_MUL|BINARY_DIV|BINARY_MOD
                                        |BINARY_EQUAL|BINARY_NOT_EQUAL|BINARY_REF_EQUAL|BINARY_REF_NOT_EQUAL
                                        |BINARY_GREATER_THAN|BINARY_GREATER_EQUAL|BINARY_LESS_THAN|BINARY_LESS_EQUAL
                                        |BINARY_CLOSED_RANGE|BINARY_HALF_OPEN_RANGE|BINARY_ANNOT_ACCESS|BINARY_BITWISE_AND
                                        |BINARY_BITWISE_OR|BINARY_BITWISE_XOR|BINARY_BITWISE_LEFT_SHIFT
                                        |BINARY_BITWISE_RIGHT_SHIFT|BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT;

public const INS_KIND_MOVE = "MOVE";
public const INS_KIND_CONST_LOAD = "CONST_LOAD";
public const INS_KIND_NEW_MAP = "NEW_MAP";
public const INS_KIND_NEW_INST = "NEW_INST";
public const INS_KIND_MAP_STORE = "MAP_STORE";
public const INS_KIND_NEW_ARRAY = "NEW_ARRAY";
public const INS_KIND_ARRAY_STORE = "ARRAY_STORE";
public const INS_KIND_MAP_LOAD = "MAP_LOAD";
public const INS_KIND_ARRAY_LOAD = "ARRAY_LOAD";
public const INS_KIND_NEW_ERROR = "NEW_ERROR";
public const INS_KIND_TYPE_CAST = "TYPE_CAST";
public const INS_KIND_IS_LIKE = "IS_LIKE";
public const INS_KIND_TYPE_TEST = "TYPE_TEST";
public const INS_KIND_OBJECT_STORE = "OBJECT_STORE";
public const INS_KIND_OBJECT_LOAD = "OBJECT_LOAD";
public const INS_KIND_NEW_XML_ELEMENT = "NEW_XML_ELEMENT";
public const INS_KIND_NEW_XML_TEXT = "NEW_XML_TEXT";
public const INS_KIND_NEW_XML_COMMENT = "NEW_XML_COMMENT";
public const INS_KIND_NEW_XML_PI = "NEW_XML_PI";
public const INS_KIND_NEW_XML_QNAME = "NEW_XML_QNAME";
public const INS_KIND_NEW_STRING_XML_QNAME = "NEW_STRING_XML_QNAME";
public const INS_KIND_XML_SEQ_STORE = "XML_SEQ_STORE";
public const INS_KIND_XML_SEQ_LOAD = "XML_SEQ_LOAD";
public const INS_KIND_XML_LOAD = "XML_LOAD";
public const INS_KIND_XML_LOAD_ALL = "XML_LOAD_ALL";
public const INS_KIND_XML_ATTRIBUTE_STORE = "XML_ATTRIBUTE_STORE";
public const INS_KIND_XML_ATTRIBUTE_LOAD = "XML_ATTRIBUTE_LOAD";
public const INS_KIND_FP_LOAD = "FP_LOAD";
public const INS_KIND_STRING_LOAD = "STRING_LOAD";
public const INS_KIND_NEW_TABLE = "NEW_TABLE";
public const INS_KIND_NEW_STREAM = "NEW_STREAM";
public const INS_KIND_TYPEOF = "TYPEOF";
public const INS_KIND_NOT = "NOT";
public const INS_KIND_NEW_TYPEDESC = "NEW_TYPEDESC";
public const INS_KIND_NEGATE = "NEGATE";

public type InstructionKind INS_KIND_MOVE | INS_KIND_CONST_LOAD | INS_KIND_NEW_MAP | INS_KIND_NEW_INST |
                                INS_KIND_MAP_STORE | INS_KIND_NEW_ARRAY | INS_KIND_NEW_ERROR | INS_KIND_ARRAY_STORE |
                                INS_KIND_MAP_LOAD | INS_KIND_ARRAY_LOAD | INS_KIND_TYPE_CAST | INS_KIND_IS_LIKE |
                                INS_KIND_TYPE_TEST | BinaryOpInstructionKind | INS_KIND_OBJECT_STORE |
                                INS_KIND_OBJECT_LOAD | INS_KIND_NEW_XML_ELEMENT | INS_KIND_NEW_XML_QNAME |
                                INS_KIND_NEW_STRING_XML_QNAME | INS_KIND_XML_SEQ_STORE | INS_KIND_NEW_XML_TEXT |
                                INS_KIND_NEW_XML_COMMENT | INS_KIND_NEW_XML_PI | INS_KIND_XML_ATTRIBUTE_STORE |
                                INS_KIND_XML_ATTRIBUTE_LOAD | INS_KIND_XML_LOAD_ALL | INS_KIND_XML_LOAD |
                                INS_KIND_XML_SEQ_LOAD | INS_KIND_FP_LOAD | INS_KIND_STRING_LOAD | INS_KIND_NEW_TABLE |
                                INS_KIND_TYPEOF | INS_KIND_NOT | INS_KIND_NEW_TYPEDESC | INS_KIND_NEW_STREAM |
                                INS_KIND_NEGATE;

public const TERMINATOR_GOTO = "GOTO";
public const TERMINATOR_CALL = "CALL";
public const TERMINATOR_ASYNC_CALL = "ASYNC_CALL";
public const TERMINATOR_BRANCH = "BRANCH";
public const TERMINATOR_RETURN = "RETURN";
public const TERMINATOR_PANIC = "PANIC";
public const TERMINATOR_WAIT = "WAIT";
public const TERMINATOR_WAIT_ALL = "WAIT_ALL";
public const TERMINATOR_FP_CALL = "FP_CALL";
public const TERMINATOR_WK_RECEIVE = "WK_RECEIVE";
public const TERMINATOR_WK_SEND = "WK_SEND";
public const TERMINATOR_FLUSH = "FLUSH";
public const TERMINATOR_LOCK = "LOCK";
public const TERMINATOR_FIELD_LOCK = "FIELD_LOCK";
public const TERMINATOR_UNLOCK = "UNLOCK";
public const TERMINATOR_PLATFORM = "PLATFORM";

public type TerminatorKind TERMINATOR_GOTO|TERMINATOR_CALL|TERMINATOR_BRANCH|TERMINATOR_RETURN|TERMINATOR_ASYNC_CALL
                                |TERMINATOR_PANIC|TERMINATOR_WAIT|TERMINATOR_FP_CALL|TERMINATOR_WK_RECEIVE
                                |TERMINATOR_WK_SEND|TERMINATOR_FLUSH|TERMINATOR_LOCK|TERMINATOR_FIELD_LOCK
                                |TERMINATOR_UNLOCK|TERMINATOR_WAIT_ALL|TERMINATOR_PLATFORM;
                                
                                
// Flags
public const int PUBLIC = 1;
public const int NATIVE = 2;
public const int ATTACHED = 8;
public const int INTERFACE = 128;
public const int REQUIRED = 256;
public const int PRIVATE = 1024;
public const int OPTIONAL = 8192;
public const int REMOTE = 65536;
public const SERVICE = 524288;
public const WORKER = 16777216;


//TODO try to make below details meta
public const VAR_KIND_LOCAL = "LOCAL";
public type LocalVarKind VAR_KIND_LOCAL;

public const VAR_KIND_TEMP = "TEMP";
public type TempVarKind VAR_KIND_TEMP;

public const VAR_KIND_RETURN = "RETURN";
public type ReturnVarKind VAR_KIND_RETURN;

public const VAR_KIND_ARG = "ARG";
public type ArgVarKind VAR_KIND_ARG;

public const VAR_KIND_GLOBAL = "GLOBAL";
public type GlobalVarKind VAR_KIND_GLOBAL;

public const VAR_KIND_SELF = "SELF";
public type SelfVarKind VAR_KIND_SELF;

public const VAR_KIND_CONSTANT = "CONSTANT";
public type ConstantVarKind VAR_KIND_CONSTANT;

public type VarKind LocalVarKind | TempVarKind | ReturnVarKind | ArgVarKind | GlobalVarKind | SelfVarKind | ConstantVarKind;


public const VAR_SCOPE_GLOBAL = "GLOBAL_SCOPE";
public const VAR_SCOPE_FUNCTION = "FUNCTION_SCOPE";

public type VarScope VAR_SCOPE_GLOBAL | VAR_SCOPE_FUNCTION;


public const ARRAY_STATE_CLOSED_SEALED = "CLOSED_SEALED";
public const ARRAY_STATE_OPEN_SEALED = "OPEN_SEALED";
public const ARRAY_STATE_UNSEALED = "UNSEALED";

public type ArrayState ARRAY_STATE_CLOSED_SEALED | ARRAY_STATE_OPEN_SEALED | ARRAY_STATE_UNSEALED;

public type VariableDclMeta record {
    string name = "";
    string endBBID = "";
    string startBBID = "";
    int insOffset = 0;
};

public type VariableDcl record {|
    VarKind kind = "LOCAL";
    VarScope varScope = VAR_SCOPE_FUNCTION;
    Name name = {};
    VariableDclMeta meta = {};
    BType typeValue = "()";
    ModuleID moduleId?;

    anydata...; // This is to type match with Object type fields in subtypes
|};

public type FunctionParam record {|
    *VariableDcl;
    boolean hasDefaultExpr;
|};

public type GlobalVariableDcl record {|
    VarKind kind = VAR_KIND_GLOBAL;
    VarScope varScope = VAR_SCOPE_GLOBAL;
    Name name = {};
    VariableDclMeta meta = {};
    BType typeValue = "()";
    ModuleID moduleId?;
    int flags = PRIVATE;
|};

public const TYPE_ANY = "any";
public type BTypeAny TYPE_ANY;

public const TYPE_ANYDATA = "anydata";
public type BTypeAnyData TYPE_ANYDATA;

public const TYPE_NONE = "none";
public type BTypeNone TYPE_NONE;

public const TYPE_NIL = "()";
public type BTypeNil TYPE_NIL;

public const TYPE_INT = "int";
public type BTypeInt TYPE_INT;

public const TYPE_DECIMAL = "decimal";
public type BTypeDecimal TYPE_DECIMAL;

public const TYPE_FLOAT = "float";
public type BTypeFloat TYPE_FLOAT;

public const TYPE_BOOLEAN = "boolean";
public type BTypeBoolean TYPE_BOOLEAN;

public const TYPE_STRING = "string";
public type BTypeString TYPE_STRING;

public const TYPE_BYTE = "byte";
public type BTypeByte TYPE_BYTE;

public const TYPE_JSON = "json";
public type BJSONType TYPE_JSON;

public const TYPE_XML = "xml";
public type BXMLType TYPE_XML;

const HANDLE_TYPE_NAME = "handle";
const SERVICE_TYPE_NAME = "service";
const RECORD_TYPE_NAME = "record";
const MAP_TYPE_NAME = "map";
const STREAM_TYPE_NAME = "stream";
const ARRAY_TYPE_NAME = "array";
const TABLE_TYPE_NAME = "table";
const ERROR_TYPE_NAME = "error";
const OBJECT_TYPE_NAME = "object";
const UNION_TYPE_NAME = "union";
const TUPLE_TYPE_NAME = "tuple";
const FUTURE_TYPE_NAME = "future";
const FINITE_TYPE_NAME = "finite";
const TYPEDESC_TYPE_NAME = "typedesc";

public type BServiceType record {|
    SERVICE_TYPE_NAME typeName = SERVICE_TYPE_NAME;
    BObjectType oType;
|};

public type BArrayType record {|
    ARRAY_TYPE_NAME typeName = ARRAY_TYPE_NAME;
    ArrayState state;
    int size;
    BType eType;
|};

public type BTypeDesc record {|
    TYPEDESC_TYPE_NAME typeName = TYPEDESC_TYPE_NAME;
    BType typeConstraint;
|};

public type BMapType record {|
    MAP_TYPE_NAME typeName = MAP_TYPE_NAME;
    BType constraint;
|};

public type BTableType record {|
    TABLE_TYPE_NAME typeName = TABLE_TYPE_NAME;
    BType tConstraint;
|};

public type BStreamType record {|
    STREAM_TYPE_NAME typeName = STREAM_TYPE_NAME;
    BType sConstraint;
|};


public type BErrorType record {|
    ERROR_TYPE_NAME typeName = ERROR_TYPE_NAME;
    ModuleID moduleId = {};
    Name name = {};
    BType reasonType;
    BType detailType;
|};

public type BRecordType record {|
    RECORD_TYPE_NAME typeName = RECORD_TYPE_NAME;
    ModuleID moduleId = {};
    Name name = {};
    boolean sealed;
    BType restFieldType;
    BRecordField?[] fields = [];
    BAttachedFunction initFunction;
    int typeFlags;
|};

public type BObjectType record {|
    OBJECT_TYPE_NAME typeName = OBJECT_TYPE_NAME;
    ModuleID moduleId = {};
    Name name = {};
    boolean isAbstract = false;
    BObjectField?[] fields = [];
    BAttachedFunction?[] attachedFunctions = [];
    BAttachedFunction? constructor;
|};

public type BTypeHandle record {
    HANDLE_TYPE_NAME typeName = HANDLE_TYPE_NAME;
    string? constraint = ();
};

public type Self record {|
    BType bType;
|};

public type BAttachedFunction record {|
    Name name = {};
    BInvokableType funcType;
    int flags;
|};

public type BRecordField record {
    Name name;
    int flags;
    BType typeValue;
    //TODO add position
};

public type BObjectField record {
    Name name;
    int flags;
    BType typeValue;
    //TODO add position
};

public type BUnionType record {|
    UNION_TYPE_NAME typeName = UNION_TYPE_NAME;
    BType?[]  members;
    int typeFlags;
|};

public type BTupleType record {|
    TUPLE_TYPE_NAME typeName = TUPLE_TYPE_NAME;
    BType?[]  tupleTypes;
    BType?  restType = ();
    int typeFlags;
|};

public type BFutureType record {|
    FUTURE_TYPE_NAME typeName = FUTURE_TYPE_NAME;
    BType returnType;
|};

public type BFiniteType record {|
    FINITE_TYPE_NAME typeName = FINITE_TYPE_NAME;
    Name name = {};
    int flags;
    [(int | string | boolean | float | byte| () | Decimal), BType] [] values;
    int typeFlags;
|};

public type BType BTypeInt | BTypeBoolean | BTypeAny | BTypeNil | BTypeByte | BTypeFloat | BTypeString | BUnionType |
                  BTupleType | BInvokableType | BArrayType | BRecordType | BObjectType | BMapType | BErrorType |
                  BTypeAnyData | BTypeNone | BFutureType | BJSONType | Self | BTypeDesc | BXMLType | BServiceType |
                  BFiniteType | BTableType | BStreamType | BTypeDecimal | BTypeHandle;

public type ModuleID record {|
    string org = "";
    string name = "";
    string modVersion = "";
    boolean isUnnamed = false;
    string sourceFilename = "";
|};

public type BInvokableType record {
    BType?[] paramTypes = [];
    BType retType?;
};

// Instructions

public type Instruction record  {
    DiagnosticPos pos;
    InstructionKind kind;
};

public type Terminator record {
    DiagnosticPos pos;
    TerminatorKind kind;
};

public type DiagnosticPos record {|
    int sLine = -1;
    int eLine = -1;
    int sCol = -1;
    int eCol = -1;
    string sourceFileName = "";
|};

public type ConstantLoad record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    int | string | boolean | float | byte | () | Decimal value;
|};

public type NewMap record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    BType bType;
    TypeRef? typeRef;
|};

public type NewTable record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef columnsOp;
    VarRef dataOp;
    VarRef keyColOp;
    BType typeValue;
|};

public type NewStream record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    BType streamType;
|};

public type NewInstance record {|
    DiagnosticPos pos;
    InstructionKind kind;
    TypeDef|TypeRef typeDefRef;
    VarRef lhsOp;
|};

public type NewArray record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef sizeOp;
    BType typeValue;
|};

public type NewError record {|
    DiagnosticPos pos;
    InstructionKind kind;
    BType typeValue;
    VarRef lhsOp;
    VarRef reasonOp;
    VarRef detailsOp;
|};

public type FPLoad record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    ModuleID pkgID;
    Name name;
    VariableDcl?[] params;
    VarRef?[] closureMaps;
    BType retType;
|};

public type FieldAccess record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef keyOp;
    VarRef rhsOp;
    boolean optionalFieldAccess = false;
    boolean fillingRead = false;
|};

public type TypeCast record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
    BType castType;
    boolean checkType;
|};

public type IsLike record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
    BType typeVal;
|};

public type TypeTest record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
    BType typeValue;
|};

public type VarRef record {|
    BType typeValue;
    VariableDcl variableDcl;
|};

public type Move record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
|};

public type BinaryOp record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp1;
    VarRef rhsOp2;
|};

public type Wait record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef lhsOp;
    VarRef?[] exprList;
    BasicBlock thenBB;
|};

public type Flush record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef lhsOp;
    ChannelDetail[] workerChannels;
    BasicBlock thenBB;
|};

public type WorkerReceive record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef lhsOp;
    Name channelName;
    boolean isSameStrand;
    BasicBlock thenBB;
|};

public type WorkerSend record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef dataOp;
    Name channelName;
    boolean isSameStrand;
    VarRef? lhsOp;
    boolean isSync;
    BasicBlock thenBB;
|};

public type Call record {|
    DiagnosticPos pos;
    VarRef?[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    ModuleID pkgID;
    Name name;
    boolean isVirtual;
    BasicBlock thenBB;
|};

public type AsyncCall record {|
    DiagnosticPos pos;
    VarRef?[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    ModuleID pkgID;
    Name name;
    boolean isVirtual;
    boolean isAsync = true;
    BasicBlock thenBB;
    AnnotationAttachment?[] annotAttachments = [];
|};

public type Branch record {|
    DiagnosticPos pos;
    BasicBlock falseBB;
    TerminatorKind kind;
    VarRef op;
    BasicBlock trueBB;
|};

public type GOTO record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    BasicBlock targetBB;
|};

public type Lock record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VariableDcl globleVar;
    BasicBlock lockBB;
|};

public type FieldLock record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VariableDcl localVar;
    string field;
    BasicBlock lockBB;
|};

public type Unlock record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VariableDcl?[] globleVars;
    LocalLocks?[] localLocks;
    BasicBlock unlockBB;
|};

public type LocalLocks record {|
    VariableDcl localVar;
    string[] fields;
|};

public type Return record {|
    DiagnosticPos pos;
    TerminatorKind kind;
|};


public type Panic record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef errorOp;
|};

public type FPCall record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef fp;
    VarRef? lhsOp;
    VarRef?[] args;
    boolean isAsync;
    BasicBlock thenBB;
|};

public type NewXMLElement record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef startTagOp;
    VarRef endTagOp;
    VarRef defaultNsURIOp;
|};

public type NewXMLQName record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef localnameOp;
    VarRef nsURIOp;
    VarRef prefixOp;
|};

public type NewStringXMLQName record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef stringQNameOp;
|};

public type XMLAccess record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
|};

public type NewXMLText record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef textOp;
|};

public type NewXMLComment record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef textOp;
|};

public type NewXMLPI record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef dataOp;
    VarRef targetOp;
|};

public type UnaryOp record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
|};

public type NewTypeDesc record {|
    DiagnosticPos pos;
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
|};

public type WaitAll record {|
    DiagnosticPos pos;
    TerminatorKind kind;
    VarRef lhsOp;
    VarRef?[] futures;
    string[] keys;
    BasicBlock thenBB;
|};

public type Decimal record {|
    string value;
|};
