// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.value;

type Unnamed int;

public type IntType "i64"|"i32"|"i16"|"i8"|"i1";
public type FloatType "double";

public type StructType readonly & record {
    Type[] elementTypes;
    string? name = ();
};

public type ArrayType readonly & record {|
    Type elementType;
    int elementCount;
|};

public type RetType Type|"void";

# Corresponds to llvm::FunctionType class
public type FunctionType readonly & record {|
    RetType returnType;
    Type[] paramTypes;
|};

public type PointerType readonly & record {|
    Type pointsTo;
    int addressSpace;
|};

public type Type IntType|FloatType|PointerType|StructType|ArrayType|FunctionType;

public readonly distinct class DataValue {
    string|Unnamed operand;
    Type ty;
    function init(Type ty, string|Unnamed operand) {
        self.ty = ty;
        self.operand = operand;
    }
}
public readonly class PointerValue {
    *DataValue;
    string|Unnamed operand;
    PointerType ty;
    function init(PointerType ty, string|Unnamed operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

public readonly class ConstValue {
    *DataValue;
    string operand;
    Type ty;
    function init(Type ty, string operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

public readonly class ConstPointerValue {
    *PointerValue;
    *ConstValue;
    string operand;
    PointerType ty;
    function init(PointerType ty, string operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

function fn() returns ConstPointerValue => new  ({pointsTo: "i64", addressSpace: 1}, "const_val");

function testFn() {
    ConstValue[] x = [];
    x.push(fn());

    ConstPointerValue c = checkpanic value:ensureType(x.pop());
    assertEquality("i64", c.ty.pointsTo);
    assertEquality(1, c.ty.addressSpace);
    assertEquality("const_val", c.operand);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString()  + "'");
}
