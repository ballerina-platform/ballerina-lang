// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function invalidTupleAssignment() {
    [int, NoFillerObject] x1 = [1];
    [int, boolean, NoFillerObject] x2 = [1, false];
    [int, boolean, string] x3 = [1, false, "abc"];
}

type Person record {
    string name;
};

type Employee record {
    string name;
    string id;
};

function invalidTupleAssignmentToUnion() {
    [int, boolean, string]? x1 = [1, false, "abc"];
    [int, boolean, string] | [any, boolean, string] | () x2 = [1, false, "abc"]; // ambiguous
    Employee p = {name:"foo", id:"12"};
    [Person, int] k1 = [p, 3];
    [Person, int] | () k2 = [p, 3];
    [Person, int] | [Employee, int] | () k3 = [p, 3]; // ambiguous
}

function testTupleToArrayAssignmentNegative() returns int[] {
    [string...] x = ["a", "b", "c"];
    int[] y = x;
    return y;
}

function testTupleToArrayAssignmentNegative1() returns string[] {
    [string...] x = ["a", "b", "c"];
    string[2] y = x;
    return y;
}

function testTupleToArrayAssignmentNegative2() returns string[] {
    [string, string] x = ["a", "b"];
    string[3] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative() returns [int...] {
    string[] x = ["a", "b", "c"];
    [int...] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative2() returns [int, string...] {
    (int|string)[] x = [1, "b", "c", 2];
    [int, string...] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative3() returns [int, int] {
    int[3] x = [1, 2, 3];
    [int, int] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative4() returns [int, int, int] {
    int[2] x = [1, 2];
    [int, int, int] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative5() returns [int...] {
    int[2] x = [1, 2];
    [int...] y = x;
    return y;
}

function testArrayToTupleAssignmentNegative6() {
    int[] a = [1, 2, 3];
    [int, int, int...] b = a;

    [int, int, int] c = a;

    int[1] d = [1];
    [int, int, int...] e = d;

    [int, int] f = d;

    int[3] g = [1, 2, 3];
    [int, int] h = g;

    [int...] i = d;
}

function tupleAssignmentToAnyAndVar () {
    var x1 = (1); // brace hence valid
    any x2 = (1); // brace hence valid
    var x3 = [1, 2]; // valid after 2019R1+, type will get resolved to int[]
    any x4 = [1, 2]; // valid after 2019R1+, type will get resolved to int[]
}

function testInvalidNumberedLiterals () {
    [float, int, NoFillerObject] x = [1.2, 3];
}

function testInvalidIndexAccess () {
    [float, int, string] x = [1.2, 3, "abc"];
    any x1 = x[-1];
    any x2 = x[3];
    int index = 0;
    any x4 = x["0"];
}

function testInvalidAccessToTupleUsingExpr() {
    [string, boolean, int] tuple = ["str", true, 10];
    string index = "0";
    var result = tuple[index]; // incompatible types: expected 'int', found 'string'
}

function testInvalidInsertionToTuple() {
    [string, boolean, int] tuple = ["str", true, 10];
    int index = 0;
    tuple[index] = 1.1; // incompatible types: expected 'string|boolean|int', found 'float'
    string y = tuple[index]; // incompatible types: expected 'string', found 'string|boolean|int'
    string|boolean x = tuple[index]; // incompatible types: expected 'string|boolean', found 'string|boolean|int'
}

const SIX = 6;

type FiniteOne "S1"|"S2";
type FiniteTwo 3|4|5;
type FiniteThree 0|1|2|"S1";
type FiniteFour FiniteThree|"S2";
type FiniteFive FiniteTwo|SIX;

function testInvalidInsertionToTupleUsingFiniteType() {
    [string, boolean, int] tuple = ["str", true, 10];
    FiniteOne f1 = "S1";
    FiniteTwo f2 = 3;
    FiniteThree f3 = 2;
    FiniteFour f4 = 0;
    FiniteFive f5 = 3;
    var a = tuple[f1]; // incompatible types: expected 'int', found 'S1|S2'
    var b = tuple[f2]; // invalid tuple index expression: value space '3|4|5' out of range
    var c = tuple[f3]; // incompatible types: expected 'int', found '0|1|2|S1'
    var d = tuple[f4]; // incompatible types: expected 'int', found 'FiniteFour'
    var e = tuple[f5]; // invalid tuple index expression: value space 'FiniteFive' out of range
}

const INDEX_NEG_ONE = -1;

function testInvalidConstIndex() {
    [string, int] tuple = ["str", 2];
    var v = tuple[INDEX_NEG_ONE];
}

type NoFillerObject object {
};

function testInvalidTupleDeclaredWithVar() {
    var [a1, a2, a3, ...a4] = getData();
    var [b1, b2, b3] = getData2();
    var [c1, c2] = getData();
}

function getData() returns [int, string...] {
    return [1, "hello"];
}

function getData2() returns int[2] {
    return [1, 2];
}

[int, int|error] m = [1, error("")];

var [x, _] = m;

function wildCardBindingPattern() {
    var [x2, _] = m;

    (int|error)[2] o = [1, 2];
    var [_, y] = o;
}

function testTupleToJSONAssignmentNegative() {
    xml A = xml `xml string`;
    [string, int, xml...] B = ["text1", 1, A];
    json jsonTest = B;

    [string, int|xml, string...] C = ["text1", 1, A.toString()];
    jsonTest = <json[]>C;
    jsonTest = C;
}

public function testUnionsOfTupleSingletons() {
     [1, "hello"]|[1] f; // Must not crash
     f = [1]; // ambiguous, [1] can match for both [1, "hello"] and [1] finite tuple values;
}

public function testTupleParamWithExistingArg1() {
    testFunc1(1, 2, 3);
}

function testFunc1(int i, int... i) {
}

public function testTupleParamWithExistingArg2() {
    testFunc2(1, 2, 3, 4);
}

function testFunc2(int i, int j, int... i) {
}

public function testTupleParamWithExistingArg3() {
    testFunc3("1", 2, 3);
}

public function testTupleParamWithExistingArg4() {
    testFunc2(1, 2, 3 + "4");
}

function testFunc3(string i, int... i) {
}

public const annotation member on field;

function testTupleMemberAnnotations() {
     [@typeParam int, string...] T1 = [1, "d"];
     [@annot int, string] T2 = [1, "d"];
     [@member @annot int, string] T3 = [1, "d"];
     [@member int, @annot string] T4 = [1, "d"];
}

type T5 [@typeParam int, string];   // annotation 'ballerina/lang.annotations:0.0.0:typeParam' is not allowed on field
type T6 [@annot int, string...];        // undefined annotation 'annot'
type T7 [@member @annot int, string];   // undefined annotation 'annot'
type T8 [@member int, @annot string];   // undefined annotation 'annot'

@annot  // undefined annotation 'annot'
type T9 [int, string];

@annot  // undefined annotation 'annot'
type T10 [@member int, string];

type T11 [@member int, @member string...];  // annotations not allowed for tuple rest descriptor
