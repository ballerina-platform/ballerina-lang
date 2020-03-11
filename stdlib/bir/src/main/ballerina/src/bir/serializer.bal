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

public function serialize(BType bType) returns string {
    if (bType is BTypeNil){
        return "()";
    } else if (bType is BTypeInt){
        return "int";
    } else if (bType is BTypeDecimal){
        return "decimal";
    } else if (bType is BTypeByte){
        return "byte";
    } else if (bType is BTypeBoolean){
        return "boolean";
    } else if (bType is BTypeFloat){
        return "float";
    } else if (bType is BTypeString){
        return "string";
    } else if (bType is BUnionType) {
        return serializeTypes(bType.members, "|");
    } else if (bType is BInvokableType) {
        return "function (" + serializeTypes(bType.paramTypes, ", ") + ") returns " + serialize(<BType> bType?.retType);
    } else if (bType is BArrayType) {
        return serialize(bType.eType) + "[]";
    } else if (bType is BRecordType) {
        return "record {" + serializeRecordFields(bType.fields) + "}";
    } else if (bType is BObjectType) {
        return "object {" + serializeFields(bType.fields) + serializeAttachedFunc(bType.attachedFunctions) + "}";
    } else if (bType is Self) {
        return "...";
    } else if (bType is BMapType) {
        return "map<"+ serialize(bType.constraint) +">";
    } else if (bType is BTableType) {
        return "table<"+ serialize(bType.tConstraint) +">";
    } else if (bType is BStreamType) {
        return "stream<"+ serialize(bType.sConstraint) +">";
    } else if (bType is BTypeAnyData) {
        return "anydata";
    } else if (bType is BErrorType) {
        return "error (" + serialize(bType.reasonType) + ", " + serialize(bType.detailType) + ")";
    }

    error err = error(io:sprintf("Unsupported serialization for type '%s'", bType));
    panic err;
}

function serializeTypes(BType?[] bTypes, string delimiter) returns string {
    var result = "";
    boolean first = true;
    foreach var t in bTypes {
        BType bType = getType(t);
        if (!first){
            result = result + delimiter;
        }
        result = result + serialize(bType);
        first = false;
    }
    return <@untainted> result;
}

function serializeFields(BObjectField?[] fields) returns string {
    var result = "";
    var delimiter = "; ";
    foreach var 'field in fields {
        if ('field is BObjectField) {
            result = result + serialize('field.typeValue) + " " + 'field.name.value + delimiter;
        }
    }
    return <@untainted> result;
}

function serializeRecordFields(BRecordField?[] fields) returns string {
    var result = "";
    var delimiter = "; ";
    foreach var 'field in fields {
        if ('field is BRecordField) {
            result = result + serialize('field.typeValue) + " " + 'field.name.value + delimiter;
        }
    }
    return result;
}

function serializeAttachedFunc(BAttachedFunction?[] functions) returns string {
    var result = "";
    foreach var func in functions {
        if (func is BAttachedFunction) {
            result = result + serialize(func.funcType) + " " + func.name.value + "; ";
        }
    }
    return <@untainted> result;
}
