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

import ballerina/bir;
import ballerina/jvm;
import ballerina/io;

const NAME_FIELD_NAME = "name";
const CLASS_FIELD_NAME = "class";
const FIELD_METHOD_FIELD_NAME = "method";
const PARAM_TYPES_FIELD_NAME = "paramTypes";
const string DIMENSIONS_FIELD_NAME = "dimensions";

function getInteropAnnotValue(bir:Function birFunc) returns jvm:InteropValidationRequest? {
    var optionalAnnotAttach = getInteropAnnotAttachment(birFunc);
    if (optionalAnnotAttach is ()) {
        return ();
    }

    var annotAttach = <bir:AnnotationAttachment> optionalAnnotAttach;
    var annotTagRef = <jvm:InteropAnnotTag>annotAttach.annotTagRef.value;
    return createJInteropValidationRequest(annotTagRef, annotAttach, birFunc);
}

function getInteropAnnotAttachment(bir:Function birFunc) returns bir:AnnotationAttachment? {
    foreach var optionalAnnotAttach in birFunc.annotAttachments {
        if(isInteropAnnotAttachment(<bir:AnnotationAttachment> optionalAnnotAttach)) {
            return optionalAnnotAttach;
        }
    }
    return ();
}

function isInteropAnnotAttachment(bir:AnnotationAttachment annotAttach) returns boolean {
    return annotAttach.moduleId.org === jvm:INTEROP_ANNOT_ORG &&
            annotAttach.moduleId.name === jvm:INTEROP_ANNOT_MODULE &&
                annotAttach.annotTagRef.value is jvm:InteropAnnotTag;
}

function createJInteropValidationRequest(jvm:InteropAnnotTag annotTagRef,
                                         bir:AnnotationAttachment annotAttach,
                                         bir:Function birFunc) returns jvm:InteropValidationRequest {

    var annotRecValue = <bir:AnnotationRecordValue> annotAttach.annotValues[0];
    map<bir:AnnotationValue> annotValueMap = annotRecValue.annotValueMap;
    if annotTagRef is jvm:MethodAnnotTag {
        return createJMethodValidationRequest(annotTagRef, annotValueMap, birFunc);
    } else {
        return createJFieldValidationRequest(annotTagRef, annotValueMap, birFunc);
    }
}

function createJMethodValidationRequest(jvm:MethodAnnotTag annotTagRef,
                                        map<bir:AnnotationValue> annotValueMap,
                                        bir:Function birFunc) returns jvm:MethodValidationRequest {

    jvm:MethodValidationRequest valRequest = {
        name:  getJMethodNameFromAnnot(annotTagRef, annotValueMap[NAME_FIELD_NAME], birFunc),
        'class: <string>getLiteralValueFromAnnotValue(annotValueMap[CLASS_FIELD_NAME]),
        kind: jvm:getMethodKindFromAnnotTag(annotTagRef),
        bFuncType: birFunc.typeValue
    };

    var paramTypeConstraints = buildParamTypeConstraints(annotValueMap[PARAM_TYPES_FIELD_NAME], birFunc, annotTagRef);
    if !(paramTypeConstraints is ()) {
        valRequest["paramTypeConstraints"] = paramTypeConstraints;
    }
    return valRequest;
}

function createJFieldValidationRequest(jvm:FieldAnnotTag annotTagRef,
                                       map<bir:AnnotationValue> annotValueMap,
                                       bir:Function birFunc) returns jvm:FieldValidationRequest {

    return {
        name: getJFieldNameFromAnnot(annotValueMap[NAME_FIELD_NAME], birFunc),
        'class: <string>getLiteralValueFromAnnotValue(annotValueMap[CLASS_FIELD_NAME]),
        method: jvm:getFieldMethodFromAnnotTag(annotTagRef),
        bFuncType: birFunc.typeValue
    };
}

function buildParamTypeConstraints(bir:AnnotationValue? annotValue,
                                   bir:Function birFunc,
                                   jvm:MethodAnnotTag annotTagRef) returns jvm:JType?[]? {
    // creating the constraints using the existing function parameter types first.
    var birFuncParams = birFunc.params;
    var birFuncParamCount = birFunc.params.length();

    if annotValue is bir:AnnotationArrayValue {
        bir:AnnotationValue?[] annotArrayElements =  annotValue.annotValueArray;
        jvm:JType?[] constraints = [];
        foreach var annotArrayElement in annotArrayElements {
            jvm:JType jType;
            if annotArrayElement is bir:AnnotationLiteralValue {
                jType = jvm:getJTypeFromTypeName(<string> annotArrayElement.literalValue);
            } else if annotArrayElement is bir:AnnotationRecordValue {
                map<bir:AnnotationValue> annotValueMap = annotArrayElement.annotValueMap;
                string elementClass = <string> getLiteralValueFromAnnotValue(annotValueMap.get(CLASS_FIELD_NAME));
                byte dimensions = <byte> getLiteralValueFromAnnotValue(annotValueMap.get(DIMENSIONS_FIELD_NAME));
                jType = jvm:getJArrayTypeFromTypeName(elementClass, dimensions);
            } else {
                panic error(io:sprintf("unexpected annotation value: %s", annotArrayElement));
            }

            constraints[constraints.length()] = jType;
        }

        return constraints;
    }
    return ();
}

function getLiteralValueFromAnnotValue(bir:AnnotationValue? annotValue) returns anydata {
    if annotValue is () {
        return ();
    } else if annotValue is bir:AnnotationLiteralValue {
        return annotValue.literalValue;
    } else {
        panic error(io:sprintf("unexpected annotation value, expected a literal value, found %s", annotValue));
    }
}

function getJMethodNameFromAnnot(jvm:MethodAnnotTag annotTagRef,
                                 bir:AnnotationValue? jNameValueEntry,
                                 bir:Function birFunc) returns string {
    if annotTagRef is jvm:CONSTRUCTOR_ANNOT_TAG {
        return "<init>";
    } else {
        string? methodName = <string?>getLiteralValueFromAnnotValue(jNameValueEntry);
        return methodName?:birFunc.name.value;
    }
}

function getJFieldNameFromAnnot(bir:AnnotationValue? jNameValueEntry,
                                 bir:Function birFunc) returns string {
    string? fieldName = <string?>getLiteralValueFromAnnotValue(jNameValueEntry);
    return fieldName?:birFunc.name.value;
}
