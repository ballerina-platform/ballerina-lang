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

// This files defines Ballerina types for Java types

public const Byte = "byte";
public const Char = "char";
public const Short = "short";
public const Int = "int";
public const Long = "long";
public const Float = "float";
public const Double = "double";
public const Boolean = "boolean";
public const Void = "void";

public const NoType = "NoType";
const REF_TYPE_TAG = "RefType";
const ARRAY_TYPE_TAG = "ArrayType";

public type PrimitiveType Byte | Char | Short | Int | Long | Float | Double | Boolean | Void;

public type ArrayType record {|
    ARRAY_TYPE_TAG tag = ARRAY_TYPE_TAG;
    JType elementType;
|};

public type RefType record {
    REF_TYPE_TAG tag = REF_TYPE_TAG;
    string typeName;
    boolean isInterface = false;
    boolean isArray = false;
};

public type JType PrimitiveType | RefType | NoType | ArrayType;

public function getJTypeFromTypeName(string typeName) returns PrimitiveType|RefType {
    if typeName is PrimitiveType {
        return typeName;
    } else {
        return <RefType> {
            typeName: typeName
        };
    }
}

public function getJArrayTypeFromTypeName(string typeName, byte dimensions) returns ArrayType {
    ArrayType arrayType = { elementType : getJTypeFromTypeName(typeName)};
    int i = 1;
    while(i < (<int>dimensions)) {
        ArrayType temp = { elementType : arrayType};
        arrayType = temp;
        i += 1;
    }

    return arrayType;
}
