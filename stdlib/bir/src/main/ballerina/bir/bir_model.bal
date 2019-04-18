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
    ImportModule[] importModules = [];
    TypeDef?[] typeDefs = [];
    GlobalVariableDcl?[] globalVars;
    Function?[] functions = [];
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
    Visibility visibility = "PACKAGE_PRIVATE";
    BType typeValue = "()";
    Function?[]? attachedFuncs = ();
};

public type Function record {|
    int argsCount = 0;
    BasicBlock?[] basicBlocks = [];
    ErrorEntry?[] errorEntries = [];
    boolean isDeclaration = false;
    VariableDcl?[] localVars = [];
    Name name = {};
    BInvokableType typeValue = {};
    Visibility visibility = "PACKAGE_PRIVATE";
|};

public type BasicBlock record {|
    Name id = {};
    Instruction?[] instructions = [];
    Terminator terminator = {kind:"RETURN"};
|};

public type ErrorEntry record {|
    BasicBlock trapBB;
    VarRef errorOp;
|};

public type Name record {|
    string value = "";
|};

public const BINARY_ADD = "ADD";
public const BINARY_SUB = "SUB";
public const BINARY_MUL = "MUL";
public const BINARY_DIV = "DIV";
public const BINARY_EQUAL = "EQUAL";
public const BINARY_NOT_EQUAL = "NOT_EQUAL";
public const BINARY_GREATER_THAN = "GREATER_THAN";
public const BINARY_GREATER_EQUAL = "GREATER_EQUAL";
public const BINARY_LESS_THAN = "LESS_THAN";
public const BINARY_LESS_EQUAL = "LESS_EQUAL";
public const BINARY_AND = "AND";
public const BINARY_OR = "OR";

public type BinaryOpInstructionKind BINARY_ADD|BINARY_SUB|BINARY_MUL|BINARY_DIV|BINARY_EQUAL|BINARY_NOT_EQUAL
                                       |BINARY_GREATER_THAN|BINARY_GREATER_EQUAL|BINARY_LESS_THAN|BINARY_LESS_EQUAL
                                       |BINARY_AND|BINARY_OR;

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
public const INS_KIND_FP_LOAD = "FP_LOAD";

public type InstructionKind INS_KIND_MOVE | INS_KIND_CONST_LOAD | INS_KIND_NEW_MAP | INS_KIND_NEW_INST | 
                                INS_KIND_MAP_STORE | INS_KIND_NEW_ARRAY | INS_KIND_NEW_ERROR | INS_KIND_ARRAY_STORE |
                                INS_KIND_MAP_LOAD | INS_KIND_ARRAY_LOAD | INS_KIND_TYPE_CAST | INS_KIND_IS_LIKE |
                                INS_KIND_TYPE_TEST | BinaryOpInstructionKind | INS_KIND_OBJECT_STORE |
                                INS_KIND_OBJECT_LOAD | INS_KIND_FP_LOAD;


public const TERMINATOR_GOTO = "GOTO";
public const TERMINATOR_CALL = "CALL";
public const TERMINATOR_ASYNC_CALL = "ASYNC_CALL";
public const TERMINATOR_BRANCH = "BRANCH";
public const TERMINATOR_RETURN = "RETURN";
public const TERMINATOR_PANIC = "PANIC";

public type TerminatorKind TERMINATOR_GOTO|TERMINATOR_CALL|TERMINATOR_BRANCH|TERMINATOR_RETURN|TERMINATOR_ASYNC_CALL
                                |TERMINATOR_PANIC;

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

public type VarKind LocalVarKind | TempVarKind | ReturnVarKind | ArgVarKind | GlobalVarKind | SelfVarKind;


public const VAR_SCOPE_GLOBAL = "GLOBAL_SCOPE";
public const VAR_SCOPE_FUNCTION = "FUNCTION_SCOPE";

public type VarScope VAR_SCOPE_GLOBAL | VAR_SCOPE_FUNCTION;


public const ARRAY_STATE_CLOSED_SEALED = "CLOSED_SEALED";
public const ARRAY_STATE_OPEN_SEALED = "OPEN_SEALED";
public const ARRAY_STATE_UNSEALED = "UNSEALED";

public type ArrayState ARRAY_STATE_CLOSED_SEALED | ARRAY_STATE_OPEN_SEALED | ARRAY_STATE_UNSEALED;

public type VariableDcl record {
    VarKind kind = "LOCAL";
    VarScope varScope = VAR_SCOPE_FUNCTION;
    Name name = {};
    BType typeValue = "()";
    anydata...; // This is to type match with Object type fields in subtypes
};

public type GlobalVariableDcl record {|
    VarKind kind = "GLOBAL";
    VarScope varScope = VAR_SCOPE_GLOBAL;
    Name name = {};
    BType typeValue = "()";
    Visibility visibility = "PACKAGE_PRIVATE";
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

public type BArrayType record {|
    ArrayState state;
    BType eType;
|};

public type BMapType record {|
    BType constraint;
|};

public type BErrorType record {|
    BType reasonType;
    BType detailType;
|};

public type BRecordType record {|
    Name name = {};
    boolean sealed;
    BType restFieldType;
    BRecordField?[] fields = [];
|};

public type BObjectType record {|
    Name name = {};
    BObjectField?[] fields = [];
    BAttachedFunction?[] attachedFunctions = [];
|};

public type Self record {|
    BType bType;
|};

public type BAttachedFunction record {|
    Name name = {};
    BInvokableType funcType;
    Visibility visibility;
|};

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

public type BUnionType record {|
   BType[]  members;
|};

public type BTupleType record {|
   BType[]  tupleTypes;
|};

public type BFutureType record {|
    BType returnType;
|};

public type BType BTypeInt | BTypeBoolean | BTypeAny | BTypeNil | BTypeByte | BTypeFloat | BTypeString | BUnionType |
                  BTupleType | BInvokableType | BArrayType | BRecordType | BObjectType | BMapType | BErrorType |
                  BTypeAnyData | BTypeNone | BFutureType | BJSONType | Self;

public type ModuleID record {|
    string org = "";
    string name = "";
    string modVersion = "";
    boolean isUnnamed = false;
    string sourceFilename = "";
|};

public type BInvokableType record {
    BType[] paramTypes = [];
    BType retType?;
};

public const VISIBILITY_PACKAGE_PRIVATE = "PACKAGE_PRIVATE";
public const VISIBILITY_PRIVATE = "PRIVATE";
public const VISIBILITY_PUBLIC = "PUBLIC";

public type Visibility VISIBILITY_PACKAGE_PRIVATE|VISIBILITY_PRIVATE|VISIBILITY_PUBLIC;


// Instructions

public type Instruction record  {
    InstructionKind kind;
};

public type Terminator record {
    TerminatorKind kind;
};

public type ConstantLoad record {|
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
    int | string | boolean | float | () value;
|};

public type NewMap record {|
    InstructionKind kind;
    VarRef lhsOp;
    BType typeValue;
|};

public type NewInstance record {|
    InstructionKind kind;
    TypeDef typeDef;
    VarRef lhsOp;
|};

public type NewArray record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef sizeOp;
    BType typeValue;
|};

public type NewError record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef reasonOp;
    VarRef detailsOp;
|};

public type FPLoad record {|
    InstructionKind kind;
    VarRef lhsOp;
    ModuleID pkgID;
    Name name;
|};

public type FieldAccess record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef keyOp;
    VarRef rhsOp;
|};

public type TypeCast record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
|};

public type IsLike record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
    BType typeValue;
|};

public type TypeTest record {|
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
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp;
|};

public type BinaryOp record {|
    InstructionKind kind;
    VarRef lhsOp;
    VarRef rhsOp1;
    VarRef rhsOp2;
|};

public type Call record {|
    VarRef?[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    ModuleID pkgID;
    Name name;
    boolean isVirtual;
    BasicBlock thenBB;
|};

public type AsyncCall record {|
    VarRef?[] args;
    TerminatorKind kind;
    VarRef? lhsOp;
    ModuleID pkgID;
    Name name;
    BasicBlock thenBB;
|};

public type Branch record {|
    BasicBlock falseBB;
    TerminatorKind kind;
    VarRef op;
    BasicBlock trueBB;
|};

public type GOTO record {|
    TerminatorKind kind;
    BasicBlock targetBB;
|};

public type Return record {|
    TerminatorKind kind;
|};

public type Panic record {|
    TerminatorKind kind;
    VarRef errorOp;
|};
