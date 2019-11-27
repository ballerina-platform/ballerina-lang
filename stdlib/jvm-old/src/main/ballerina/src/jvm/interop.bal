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

public const INTEROP_ANNOT_ORG = "ballerinax";
public const INTEROP_ANNOT_MODULE = "java";

public const METHOD = "method";
public const CONSTRUCTOR = "constructor";

public const CONSTRUCTOR_ANNOT_TAG = "Constructor";
public const METHOD_ANNOT_TAG = "Method";
public const FIELD_GET_ANNOT_TAG = "FieldGet";
public const FIELD_PUT_ANNOT_TAG = "FieldSet";

public const ACCESS = "access";
public const MUTATE = "mutate";
public type FieldMethod ACCESS | MUTATE;

public type MethodAnnotTag CONSTRUCTOR_ANNOT_TAG | METHOD_ANNOT_TAG;
public type FieldAnnotTag FIELD_GET_ANNOT_TAG | FIELD_PUT_ANNOT_TAG;
public type InteropAnnotTag MethodAnnotTag | FieldAnnotTag;

public type MethodKind METHOD | CONSTRUCTOR;

public type MethodValidationRequest record {|
    string name;
    MethodKind kind;
    string class;
    anydata bFuncType;
    JType?[] paramTypeConstraints?;
    boolean restParamExist = false;
|};

public type FieldValidationRequest record {|
    string name;
    string class;
    FieldMethod method;
    anydata bFuncType;
|};

public type InteropValidationRequest MethodValidationRequest | FieldValidationRequest;

public type Method record {|
    string name;
    string class;
    boolean isInterface = false;
    MethodKind kind;
    boolean isStatic = false;
    string sig;
    MethodType mType;
    string[] throws;
|};

public type MethodType record {|
    JType[] paramTypes;
    JType retType;
|};

public type Field record {|
    string name;
    string class;
    boolean isStatic = false;
    FieldMethod method;
    string sig;
    JType fType;
|};


public type InteropValidator object {

    public function __init(string[] jarUrls, boolean useSystemClassLoader) {
        self.init(jarUrls, useSystemClassLoader);
    }

    function init(string[] jarUrls, boolean useSystemClassLoader) = external;

    public function validateAndGetJMethod(MethodValidationRequest methodValidationReq) returns Method | error = external;

    public function validateAndGetJField(FieldValidationRequest fieldValidationReq) returns Field | error = external;
};


public function getMethodKindFromAnnotTag(MethodAnnotTag annotTagRef) returns MethodKind {
    if annotTagRef is CONSTRUCTOR_ANNOT_TAG {
        return CONSTRUCTOR;
    } else {
        return METHOD;
    }
}

public function getFieldMethodFromAnnotTag(FieldAnnotTag annotTagRef) returns FieldMethod {
    if annotTagRef is FIELD_GET_ANNOT_TAG {
        return ACCESS;
    } else {
        return MUTATE;
    }
}
