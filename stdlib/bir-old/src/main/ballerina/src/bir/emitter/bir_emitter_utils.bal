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

function emitName(Name name) returns string {
    return name.value;
}

function emitVarRef(VarRef ref) returns string {
    return emitName(ref.variableDcl.name);
}   

function emitBasicBlockRef(BasicBlock basicBlock) returns string {
    return emitName(basicBlock.id);
}

function emitModuleID(ModuleID modId) returns string {
    string str = "";
    str += modId.org + "/";
    str += modId.name;
    if modId.modVersion != "" {
        str += ":";
        str += modId.modVersion;
    }
    return str;
}

function emitBAttachedFunction(BAttachedFunction func, int tabs) returns string {
    string str = "";
    str += emitTabs(tabs);
    str += emitName(func.name);
    str += emitSpaces(1);
    str += emitType(func.funcType);
    return str;
}

function emitBinaryOpInstructionKind(BinaryOpInstructionKind kind) returns string {
    if kind is BINARY_ADD {
        return "+";
    } else if kind is BINARY_SUB {
        return "-";
    } else if kind is BINARY_MUL {
        return "*";
    } else if kind is BINARY_DIV {
        return "/";
    } else if kind is BINARY_MOD {
        return "%";
    } else if kind is BINARY_EQUAL {
        return "==";
    } else if kind is BINARY_NOT_EQUAL {
        return "!=";
    } else if kind is BINARY_GREATER_THAN {
        return ">";
    } else if kind is BINARY_GREATER_EQUAL {
        return ">=";
    } else if kind is BINARY_LESS_THAN {
        return "<";
    } else if kind is BINARY_LESS_EQUAL {
        return "<=";
    } else if kind is BINARY_REF_EQUAL {
        return "===";
    } else if kind is BINARY_REF_NOT_EQUAL {
        return "!==";
    } else if kind is BINARY_CLOSED_RANGE {
        return "...";
    } else if kind is BINARY_HALF_OPEN_RANGE {
        return "..<";
    } else if kind is BINARY_ANNOT_ACCESS {
        return ".@";
    } else if kind is BINARY_BITWISE_AND {
        return "&";
    } else if kind is BINARY_BITWISE_OR {
        return "|";
    } else if kind is BINARY_BITWISE_XOR {
        return "^";
    } else if kind is BINARY_BITWISE_LEFT_SHIFT {
        return "<<";
    } else if kind is BINARY_BITWISE_RIGHT_SHIFT {
        return ">>";
    } else { // if kind is BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT {
        return ">>>";
    }
}

function emitFlags(int flag) returns string {
    if (flag & PRIVATE) == PRIVATE {
        return "private";
    } else if (flag & PUBLIC) == PUBLIC {
        return "public";
    }
    return "";
}

function emitTabs(int tabs) returns string {
    string tab = "";
    int i = 0;
    while i < tabs {
        tab += emitSpaces(4);
	i += 1;
    }
    return tab;
}

function emitSpaces(int spaces) returns string {
    string spacesString = "";
    int i = 0;
    while i < spaces {
        spacesString += " ";
	i += 1;
    }
    return spacesString;
}

function emitLBreaks(int breaks) returns string {
    string lineBreaks = "";
    int i = 0;
    while i < breaks {
        lineBreaks += "\n";
	i += 1;
    }
    return lineBreaks;
}

function getTypeName(BType bType) returns string? {
    foreach var [k, v] in bTypes.entries() {
        if v == bType {
	    return k;
	}
    }
    return ();
}	

function getFromStack(BType bType, BType[] tStack) returns BType? {
    foreach BType tType in tStack {
        if tType == bType {
            return tType;
        }
    }
    return ();
}

function isEmpty(Name nameVal) returns boolean {
    return nameVal.value == "";
}

function emitValue(int | string | boolean | float | byte | () | Decimal value) returns string {
    if value is string {
        return value;
    } else if value is () {
        return "()";
    } else {
        return value.toString();
    }
}

