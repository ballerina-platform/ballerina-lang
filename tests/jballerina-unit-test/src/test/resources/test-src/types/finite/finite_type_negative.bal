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

type Foo 1f|1d|2d;
type Foo2 1|1d|2d;
type Foo4 1|decimal|2f;

function testFiniteTypeWithNumericValues() {
    Foo f2 = 2;
    Foo2 f3 = 2;
    Foo4 f5 = 3;
    "chiran" x = 5;
}

function testFiniteTypeOfNumericValues() {
    1f _ = 2;
    1.0 _ = 3;
    1.121 _ = 5;
    1.2e12 _ = 2.1;
    0x1p-1 _ = 1.2f;
    0x.12p12 _ = 1.2d;
    1.2e12 _ = 2.1d;

    2d _ = 4;
    1.2d _ = 2.1;
    1.21d _ = 1.2f;
    0.1219e-1 _ = 1.21d;
    12.1d _ = 0x.21;

    boolean b = true;
    if b {
        0x.12p12 _ = 1.2d;
        1.2e12 _ = 2.1d;

        2d _ = 4;
        1.2d _ = 2.1;
    }
}

type Float1 1f;
Float1 _ = 2;

1f _ = 2;
1.0 _ = 3;
1.121 _ = 5;
1.2e12 _ = 2.1;
0x1p-1 _ = 1.2f;
0x.12p12 _ = 1.2d;
1.2e12 _ = 2.1d;

2d _ = 4;
1.2d _ = 2.1;
1.21d _ = 1.2f;
0.1219e-1 _ = 1.21d;
12.1d _ = 0x.21;

function finiteTypeFunctionParameterTest() {
    fn1(1, 2);
    fn2(1, 2);
}

function fn1(2d x, 1f y) {

}

function fn2(0.1219e-1 x, 0x.12p12 y) {

}

function finiteTypeFunctionReturnTypeTest() {
    2d _ = fn3();
    3f _ = fn4();
}

function fn3() returns 1f {
    return 2;
}

function fn4() returns 2d {
    return "s";
}

function finiteTypeTypeCastExpr() {
    2d _ = <2f> 1;
    3d _ = <4f> 4;
    2f _ = <2d> 2;
    3f _ = <4d> 4;
    3f _ = <4f> 4;
    3d _ = <4d> 4;
}

type IntOrNull int|null;
type IntOrNullStr int|"null";

function testNullFiniteType() {
    IntOrNull a = 1; // OK
    IntOrNullStr b = 1; // OK

    int|null c = 1;
    int|"null" d = 1;
    null e = null;
    "null" f = "null";

    IntOrNull _ = null; // OK
    IntOrNull _ = (); // OK
    IntOrNull _ = "null"; // error
    IntOrNull _ = a; // OK
    IntOrNull _ = b; // error
    IntOrNull _ = c; // OK
    IntOrNull _ = d; // error
    IntOrNull _ = e; // OK
    IntOrNull _ = f; // error

    IntOrNullStr _ = null; // error
    IntOrNullStr _ = (); // error
    IntOrNullStr _ = "null"; // OK
    IntOrNullStr _ = a; // error
    IntOrNullStr _ = b; // OK
    IntOrNullStr _ = c; // error
    IntOrNullStr _ = d; // OK
    IntOrNullStr _ = e; // error
    IntOrNullStr _ = f; // OK

    null _ = "null"; // error
    "null" _ = null; // error
}

type UnaryType1 -2;
type UnaryType2 string|-3;
type UnaryType3 UnaryType1|-3;

function testFiniteTypeWithUnaryMinus() {
    UnaryType2 x1 = -2;
    UnaryType3 x2 = -5;
}

function testFiniteTypeWithOutOfRangeValues() {
    1|5.4 _ = 92233720368547758078;
    1 _ = 92233720368547758078;
}

type IntOne 1;
type FloatOne 1.0;
type DecimalOne 1.0d;
type StringA "A";

function testFiniteTypeAssignableNegative() {
    IntOne intOne = 1;
    FloatOne _ = intOne;
    1.0 _ = intOne;
    float _ = intOne;
    DecimalOne _ = intOne;

    FloatOne floatOne = 1.0;
    IntOne _ = floatOne;
    1 _ = floatOne;
    DecimalOne _ = floatOne;
    DecimalOne _ = 1.0f;
    FloatOne|StringA _ = intOne;
    FloatOne|StringA _ = floatOne;

    DecimalOne decimalOne = 1.0d;
    IntOne _ = decimalOne;
    FloatOne _ = decimalOne;
    FloatOne|IntOne _ = decimalOne;
}

function testOutOfRangeWithDecimal() {
    1.7976931348623157E+309d a = 9223372036854775808;
    1.7976931348623157E+309d _ = 17976931348623157000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000;
}

type testType 3|-4|-9223372036854775808;
function testFiniteTypeOutOfRangeWithFunction() {
    testType val = foo();
}

function foo() returns 3|-4|-9223372036854775808 {
    testType val = 3;
    return val;
}

type InvalidTest1 -9223372036854775808|-1|-1d|"string";
function testInvalidLiteralInFiniteType() {
    InvalidTest1 x = -1;
}

function testFiniteTypeFloatWithIntegers() {
    1f a = 9223372036854775808;
    1f b = 340;
}
