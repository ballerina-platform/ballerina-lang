// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/jballerina.java;

type AnnotTupleOne record {|
   string value;
|};

type Details record {|
    string name;
    int age;
|};
 
annotation AnnotTupleOne annotOne on type, field;
annotation AnnotTupleOne annotTwo on type, field;
annotation Details details on field;

[@annotOne {value: "10"} int, @annotOne {value: "k"} int, string...] g1 =  [1, 2, "hello", "world"];

function testAnnotationOnTupleFields() {
    string k = "chiranS";
    [@annotOne {value: "10"} int, @annotOne {value: k} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getAnonymousTupleAnnotations(typeof x1, "$field$.0");
    assertEquality({value: "10"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({value: "chiranS"}, <map<anydata>>m2["annotOne"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof g1, "$field$.0");
    assertEquality({value: "10"}, <map<anydata>>m3["annotOne"]);
    map<any> m4 = getAnonymousTupleAnnotations(typeof g1, "$field$.1");
    assertEquality({value: "k"}, <map<anydata>>m4["annotOne"]);
}

string gVar0 = "foo";

// This test evaluates the reordering of global variables
[@details {name: gVar0, age: gVar4} int, @details {name: "name", age: 0} int, string...] g4 =  [1, 2, "hello", "world"];

function testAnnotationOnTupleFields2() {
    string name = "chiranS";
    int age = 26;
    [@details {name: name, age: age} int, @details {name: "name", age: 0} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getAnonymousTupleAnnotations(typeof x1, "$field$.0");
    assertEquality({name: "chiranS", age: 26},  <map<anydata>>m1["details"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({name: "name", age: 0},  <map<anydata>>m2["details"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof g4, "$field$.0");    
    assertEquality({name: "foo", age: 15},  <map<anydata>>m3["details"]);
    map<any> m4 = getAnonymousTupleAnnotations(typeof g4, "$field$.1");
    assertEquality({name: "name", age: 0},  <map<anydata>>m4["details"]);
}

string gVar = "foo";
string gVar1 = "bar";
[@annotOne {value: gVar} int, int, string...] g2 =  [1, 2, "hello", "world"];

function testAnnotationOnTupleWithGlobalVariable() {
    [@annotOne {value: gVar} int, int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getAnonymousTupleAnnotations(typeof x1, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof g2, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m2["annotOne"]);
}

[@annotOne {value: "foo"} @annotTwo {value: "bar"} int, @details {name: gVar2, age: 0} int, string...] g3 =  [1, 2, "hello", "world"];

function testMultipleAnnotationsOnLocalTuple() {
    string k = "chiranS";
    [@annotOne {value: "foo"} @annotTwo {value: "bar"} int, @details {name: k, age: 0} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getAnonymousTupleAnnotations(typeof x1, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "bar"}, <map<anydata>>m1["annotTwo"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({name: "chiranS", age: 0},  <map<anydata>>m2["details"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof g3, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m3["annotOne"]);
    assertEquality({value: "bar"}, <map<anydata>>m3["annotTwo"]);
    map<any> m4 = getAnonymousTupleAnnotations(typeof g3, "$field$.1");
    assertEquality({name: "baz", age: 0},  <map<anydata>>m4["details"]);
}

function() returns [int] x = function() returns [@annotOne {value: "foo"} int] {return [1];};
function() returns [int] x2 = function() returns [@annotOne {value: gVar1} @annotTwo {value: gVar2} int] {return [1];};
function() returns [int, int] x3 = function() returns [@annotOne {value: gVar1} int, @details {name: "name", age: gVar3} int] {return [1, 1];};

function testTupleAnnotationsOnFunctionPointerReturnType() {
    map<any> m1 = getAnonymousTupleAnnotations(typeof x(), "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof x2(), "$field$.0");
    assertEquality({value: "bar"}, <map<anydata>>m2["annotOne"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof x3(), "$field$.0");
    assertEquality({value: "bar"}, <map<anydata>>m3["annotOne"]);
    map<any> m4 = getAnonymousTupleAnnotations(typeof x3(), "$field$.1");
    assertEquality({name: "name", age: 10}, <map<anydata>>m4["details"]);
}

function testTupleMemberAnnotations1() returns [@annotOne {value: "foo"} int, @details {name: "name", age: gVar3} int] {
    return [1, 1];
}

function testTupleMemberAnnotations2() returns [@annotOne {value: gVar1} @annotTwo {value: gVar2} int] {
    return [1];
}

function testTupleMemberAnnotations() {
    map<any> m1 = getAnonymousTupleAnnotations(typeof testTupleMemberAnnotations1(), "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof testTupleMemberAnnotations1(), "$field$.1");
    assertEquality({name: "name", age: 10}, <map<anydata>>m2["details"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof testTupleMemberAnnotations2(), "$field$.0");
    assertEquality({value: "bar"}, <map<anydata>>m3["annotOne"]);
    assertEquality({value: "baz"}, <map<anydata>>m3["annotTwo"]);
}

string gVar2 = "baz";
int gVar3 = 10;

function func() returns [@annotOne {value: "foo"} int] {
    return [1];
}

function func1() returns [@annotOne {value: "foo"} @annotTwo {value: gVar2} int] {
    return [1];
}

function func2() returns [@details {name: "name", age: gVar4} int, @annotTwo {value: gVar2} int] {
    return [1, 1];
}

function testGlobalAnnotationsOnFunctionReturnType() {
    map<any> m1 = getAnonymousTupleAnnotations(typeof func(), "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof func1(), "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m2["annotOne"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    map<any> m3 = getAnonymousTupleAnnotations(typeof func2(), "$field$.0");
    assertEquality({name: "name", age: 15}, <map<anydata>>m3["details"]);
    map<any> m4 = getAnonymousTupleAnnotations(typeof func2(), "$field$.1");
    assertEquality({value: "baz"}, <map<anydata>>m4["annotTwo"]);
}

int gVar4 = 15;

function func3([@annotOne {value: "foo"} int] a) {
    map<any> m1 = getAnonymousTupleAnnotations(typeof a, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
}

function func4([@annotOne {value: "foo"} int, @annotTwo {value: "foo"} @details {name: "name", age: gVar4} int] a) {
    map<any> m1 = getAnonymousTupleAnnotations(typeof a, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    map<any> m2 = getAnonymousTupleAnnotations(typeof a, "$field$.1");
    assertEquality({value: "foo"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({name: "name", age: 15}, <map<anydata>>m2["details"]);
}

function testGlobalAnnotationsOnFunctionParameterType() {
    func3([10]);
    func4([10, 10]);
}

function getAnonymousTupleAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/AnonymousTupleAnnotationTest",
    name: "getAnonymousTupleAnnotations"
} external;

function assertEquality(anydata expected, anydata  actual) {

    if expected == actual {
        return;
    }

    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
