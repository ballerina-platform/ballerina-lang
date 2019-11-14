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

import ballerina/io;

map<BType> bTypes = {};

type BTypeBasicType BTypeInt | BTypeBoolean | BTypeAny | BTypeNil | BTypeByte | BTypeFloat 
                          | BTypeString | BTypeAnyData | BTypeNone | BJSONType | BXMLType | BTypeDecimal;

type BTypeComplexType BUnionType | BTupleType | BInvokableType | BArrayType | BRecordType | BObjectType 
                          | BMapType | BErrorType | BFutureType | Self | BTypeDesc | BServiceType | BPlatformType 
			  | BFiniteType | BTableType | BStreamType;


function emitType(BType bType, int tabs = 0) returns string {
    if bType is BTypeBasicType {
        return emitBasicType(bType);
    } else if bType is BTypeComplexType {
        return emitComplexType(bType, tabs);
    }
    error e = error(io:sprintf("cannot emmit type, invalid type: %s", bType));
    panic e;
}

function emitBasicType(BTypeBasicType bType) returns string {
    string bStr = "";
    if bType is BTypeInt {
        bStr += "int";
    } else if bType is BTypeBoolean {
        bStr += "boolean";
    } else if bType is BTypeAny {
        bStr += "any";
    } else if bType is BTypeNil {
        bStr += "()";
    } else if bType is BTypeByte {
        bStr += "byte";
    } else if bType is BTypeFloat {
        bStr += "float";
    } else if bType is BTypeString {
        bStr += "string";
    } else if bType is BTypeAnyData {
        bStr += "anydata";
    } else if bType is BTypeNone {
        bStr += "none";
    } else if bType is BJSONType {
        bStr += "json";
    } else if bType is BXMLType {
        bStr += "xml";
    } else { //if bType is BTypeDecimal {
        bStr += "decimal";
    }
    return bStr;
}

function emitComplexType(BTypeComplexType bType, int tabs) returns string {
    if bType is BUnionType {
        return emitBUnionType(bType, tabs);
    } else if bType is BTupleType {
        return emitBTupleType(bType, tabs);
    } else if bType is BInvokableType {
        return emitBInvokableType(bType, tabs);
    } else if bType is BArrayType {
        return emitBArrayType(bType, tabs);
    } else if bType is BRecordType {
        return emitBRecordType(bType, tabs);
    } else if bType is BObjectType {
        return emitBObjectType(bType, tabs);
    } else if bType is BMapType {
        return emitBMapType(bType, tabs);
    } else if bType is BErrorType {
        return emitBErrorType(bType, tabs);
    } else if bType is BFutureType {
        return emitBFutureType(bType, tabs);
    } else if bType is Self {
        return emitSelf(bType, tabs);
    } else if bType is BTypeDesc {
        return emitBTypeDesc(bType, tabs);
    } else if bType is BServiceType {
        return emitBServiceType(bType, tabs);
    } else if bType is BPlatformType {
        return emitBPlatformType(bType, tabs);
    } else if bType is BFiniteType {
        return emitBFiniteType(bType, tabs);
    } else if bType is BTableType {
        return emitBTableType(bType, tabs);
    } else { //if bType is BStreamType {
        return emitBStreamType(bType, tabs);
    }
}

function emitBUnionType(BUnionType bType, int tabs) returns string {
    string unionStr = "";
    int length = bType.members.length();
    int i = 0;
    foreach BType? mType in bType.members {
        if mType is BType {
            unionStr += emitTypeRef(mType, tabs);
            i += 1;
            if i < length {
                unionStr += emitSpaces(1);
                unionStr += "|";
                unionStr += emitSpaces(1);
            }
	}
    }
    return unionStr;
}	

function emitBTupleType(BTupleType bType, int tabs) returns string {
    string tupleStr = "(";
    int length = bType.tupleTypes.length();
    int i = 0;
    foreach BType? mType in bType.tupleTypes {
        if mType is BType {
            tupleStr += emitTypeRef(mType, tabs);
            i += 1;
            if i < length {
                tupleStr += ",";
                tupleStr += emitSpaces(1);
            }
	}
    }
    tupleStr += ")";
    return tupleStr;
}	

function emitBInvokableType(BInvokableType bType, int tabs) returns string {
    string invString = "function(";
    int pLength = bType.paramTypes.length();
    int i = 0;
    foreach BType? pType in bType.paramTypes {
    	if pType is BType {
            invString += emitTypeRef(pType, tabs);
	    i += 1;
	    if i < pLength {
                invString += ",";
                invString += emitSpaces(1);
	    }
	}
    }
    invString += ")";
    BType? retType = bType?.retType;
    if retType is BType {
        invString += emitSpaces(1);
        invString += "->";
        invString += emitSpaces(1);
        invString += emitTypeRef(retType, tabs);
    }
    return invString;
}

function emitBArrayType(BArrayType bType, int tabs) returns string {
    string arrStr = emitTypeRef(bType.eType);
    arrStr += "[";
    if bType.size > 0 {
        arrStr += bType.size.toString();
    }
    arrStr += "]";
    return arrStr;
}	

function emitBRecordType(BRecordType bType, int tabs) returns string {
    string recordStr = "record";
    recordStr += emitSpaces(1);
    recordStr += "{";
    recordStr += emitLBreaks(1);
    foreach BRecordField? bField in bType.fields {
        if bField is BRecordField {
            recordStr += emitTabs(tabs + 1);
            string flags = emitFlags(bField.flags);
            recordStr += flags;
            if flags != "" {
                recordStr += emitSpaces(1);
            }
            recordStr += emitTypeRef(bField.typeValue, tabs + 1);
            recordStr += emitSpaces(1);
            recordStr += emitName(bField.name);
            recordStr += ";";
            recordStr += emitLBreaks(1);
        }
    }
    recordStr += "}";
    return recordStr;
}	

function emitBObjectType(BObjectType bType, int tabs) returns string {
    string str = "object";
    str += emitSpaces(1);
    str += "{";
    str += emitLBreaks(1);
    foreach BObjectField? bField in bType.fields {
        if bField is BObjectField {
            str += emitTabs(tabs + 1);
            string flags = emitFlags(bField.flags);
            str += flags;
            if flags != "" {
                str += emitSpaces(1);
            }
            str += emitTypeRef(bField.typeValue, tabs + 1);
            str += emitSpaces(1);
            str += emitName(bField.name);
            str += ";";
            str += emitLBreaks(1);
        }
    }
    int nTab = tabs + 1;
    BAttachedFunction? constructor = bType.constructor;
    if constructor is BAttachedFunction {
        str += emitBAttachedFunction(constructor, nTab);
    }
    str += emitLBreaks(1);
    foreach BAttachedFunction? attachedFunc in bType.attachedFunctions {
        if attachedFunc is BAttachedFunction {
            str += emitBAttachedFunction(attachedFunc, nTab);
            str += emitLBreaks(1);
        }   
    }   
    str += "}";
    return str;
}	

function emitBMapType(BMapType bType, int tabs) returns string {
    string str = "map";
    str += "<";
    str += emitTypeRef(bType.constraint);
    str += ">";
    return str;
}	

function emitBErrorType(BErrorType bType, int tabs) returns string {
    string errStr = "error{";
    errStr += emitTypeRef(bType.reasonType, tabs);
    errStr += ",";
    errStr += emitSpaces(1);
    errStr += emitTypeRef(bType.detailType, tabs);
    errStr += "}";
    return errStr;
}

function emitBFutureType(BFutureType bType, int tabs) returns string {
    string str = "future";
    str += "<";
    str += emitTypeRef(bType.returnType);
    str += ">";
    return str;
}

function emitSelf(Self bType, int tabs) returns string {
    // TODO fill 
    return "";
}

function emitBTypeDesc(BTypeDesc bType, int tabs) returns string {
    string str = "typeDesc";
    str += "<";
    str += emitTypeRef(bType.typeConstraint);
    str += ">";
    return str;
}

function emitBServiceType(BServiceType bType, int tabs) returns string {
    string str = "service";
    str += "<";
    str += emitTypeRef(bType.oType);
    str += ">";
    return str;
}

function emitBFiniteType(BFiniteType bType, int tabs) returns string {
    string str = "";
    str += "[";
    // TODO fill finite type
    str += "]";
    return str;
}

function emitBTableType(BTableType bType, int tabs) returns string {
    string str = "table";
    str += "<";
    str += emitTypeRef(bType.tConstraint);
    str += ">";
    return str;
}

function emitBStreamType(BStreamType bType, int tabs) returns string {
    string str = "table";
    str += "<";
    str += emitTypeRef(bType.sConstraint);
    str += ">";
    return str;
}
/////////////////////// Emitting type reference ///////////////////////////
function emitTypeRef(BType bType, int tabs = 0) returns string {
    if bType is BTypeBasicType {
        return emitBasicType(bType);
    } else if bType is BTypeComplexType {
        return emitComplextTypeRef(bType, tabs);
    }
    error e = error(io:sprintf("cannot emit type ref, invalid type: %s", bType));
    panic e;
}

function emitComplextTypeRef(BTypeComplexType bType, int tabs) returns string {
    string? tName = getTypeName(bType);
    if tName is string {
        return tName;
    }
    if bType is BErrorType {
        bTypes[bType.name.value] = bType;
    }
    return emitType(bType, tabs);
}   


