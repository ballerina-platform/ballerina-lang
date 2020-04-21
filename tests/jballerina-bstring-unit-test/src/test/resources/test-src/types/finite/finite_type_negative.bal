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

const byte BCONST = 5;
const int ICONST = 5;
const float FCONST = 5;
const decimal DCONST = 5;
const int ICONST2 = 100;
const float FCONST2 = 100.5;
const string SCONST = "S";

type Number ICONST|DCONST|FCONST;

type Finite ICONST|ICONST2;

type FloatingPoint FCONST2|SCONST;

// Test invalid string assignment
function testDifferentLiteralKindsWithSameValue() {
    Finite f = "5";
}

type ByteType BCONST;

// Test assigning int constant to byte finite type
function testAssigningIntConstantToByteFiniteType() {
    ByteType b = ICONST;
}

type IntType ICONST;

// Test assigning byte constant to int finite type
function testAssigningByteConstantToIntFiniteType() {
    IntType i = BCONST;
}

// Test assigning float literal to int finite type
function testAssigningFloatLiteralToIntFiniteType() {
    IntType i = 23.00;
}

type FloatType FCONST;

// Test assigning int constant to float finite type
function testAssigningIntConstantToFloatFiniteType() {
    FloatType f = ICONST;
}

// Test assigning decimal constant to float finite type
function testAssigningDecimalConstantToFloatFiniteType() {
    FloatType f = DCONST;
}

type DecimalType DCONST;

// Test assigning int constant to decimal finite type
function testAssigningIntConstantToDecimalFiniteType() {
    DecimalType d = ICONST;
}

// Test assigning float constant to decimal finite type
function testAssigningFloatConstantToDecimalFiniteType() {
    DecimalType d = FCONST;
}

// Test assigning an expression to a finite type
function testAssigningExpressionToFiniteType() {
    IntType i = 2 + 3;
}

type StringOrIntVal "foo"|1;
type StringOrInt string|int;

function testInvalidAssignmentToDifferentType() {
    StringOrIntVal s1 = "foo";
    string s2 = s1;

    StringOrInt s3 = 5;
    int s4 = s3;
}

type t 1.0f|1.0d;
type t2 2.22f|3.33d;
type t3 1.0d|2.0f;
function testFiniteTypesWithDiscriminatedMembers() returns [any, any, any, any, any] {
    t a = 1.0f;
    t b = 1.0d;
    t3 ta = 2.0;
    t3 tb = 1.0f;
    t3 tc = 1.0;
    t|t2 c = 2.22;
    t|t2 d = 2.22f;
    t|t2 e = 3.33;
    t|t2 f = 3.334d;
    return [a, b, c, d, e];
}

