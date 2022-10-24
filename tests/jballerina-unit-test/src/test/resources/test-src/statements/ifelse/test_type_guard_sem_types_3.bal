// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type TypeDesc BuiltinTypeDesc|BinaryTypeDesc|ConstructorTypeDesc|TypeDescRef|SingletonTypeDesc|UnaryTypeDesc;

type PositionFields record {|
    int startPos;
    int endPos;
|};

public type BuiltinTypeDesc readonly & record {|
    *PositionFields;
    string builtinTypeName;
|};

public type BinaryTypeDesc record {|
    *PositionFields;
    int op;
    string opPos;
    TypeDesc left;
    TypeDesc right;
|};

public type ConstructorTypeDesc TupleTypeDesc|ArrayTypeDesc|MappingTypeDesc|FunctionTypeDesc|ErrorTypeDesc|XmlSequenceTypeDesc|TableTypeDesc;

public type TupleTypeDesc record {|
    *PositionFields;
    TypeDesc[] members;
    TypeDesc? rest;
    int? defn = ();
|};

public type ArrayTypeDesc record {|
    *PositionFields;
    TypeDesc member;
    string?[] dimensions = [];
    int? defn = ();
|};

public type FieldDesc record {|
    *PositionFields;
    string name;
    TypeDesc typeDesc;
|};

public const INCLUSIVE_RECORD_TYPE_DESC = true;

public type MappingTypeDesc record {|
    int startPos;
    int endPos;
    FieldDesc[] fields;
    TypeDesc|INCLUSIVE_RECORD_TYPE_DESC? rest;
    string? defn = ();
|};

public type FunctionTypeDesc record {|
    int startPos;
    int endPos;
    FunctionTypeParam[] params;
    TypeDesc ret;
    string? defn = ();
|};

public type FunctionTypeParam record {|
    *PositionFields;
    string? name;
    int? namePos;
    TypeDesc td;
|};

public type ErrorTypeDesc record {|
    int startPos;
    int endPos;
    TypeDesc detail;
|};

public type XmlSequenceTypeDesc record {|
    int startPos;
    int endPos;
    int pos;
    TypeDesc constituent;
|};

public type TableTypeDesc record {|
    *PositionFields;
    TypeDesc row;
|};

public type TypeDescRef record {|
    *PositionFields;
    string? prefix = ();
    string typeName;
    int qNamePos;
|};

public type SingletonTypeDesc record {|
    *PositionFields;
    (string|float|int|boolean|decimal) value;
|};

public type UnaryTypeDesc record {|
    *PositionFields;
    int op;
    TypeDesc td;
|};

function f1(TypeDesc td) {
    if td is BuiltinTypeDesc {
        return;
    } else if td is TypeDescRef {
        return;
    } else if td is MappingTypeDesc {

    } else if td is ErrorTypeDesc {
        return;
    } else if td is TupleTypeDesc {

    } else if td is ArrayTypeDesc {

    } else if td is BinaryTypeDesc {
        TypeDesc _ = td.right;
    } else if td is SingletonTypeDesc {

    } else if td is TableTypeDesc {

    } else if td is UnaryTypeDesc {

    } else if td is XmlSequenceTypeDesc {

    } else {
        foreach var _ in td.params {
        }
    }
}

function f2(TypeDesc td) {
    if td is BuiltinTypeDesc {
        return;
    } else {
        BinaryTypeDesc|ConstructorTypeDesc|TypeDescRef|SingletonTypeDesc|UnaryTypeDesc _ = td;
    }
}
