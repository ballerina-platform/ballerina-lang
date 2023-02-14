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

function testAnnotationOnTupleFields() {
    string k = "chiranS";
    [@annotOne {value: "10"} int, @annotOne {value: k} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getLocalTupleAnnotations(typeof x1, "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({value: "10"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "chiranS"}, <map<anydata>>m2["annotOne"]);
}

function testAnnotationOnTupleFields2() {
    string name = "chiranS";
    int age = 26;
    [@details {name: name, age: age} int, @details {name: "name", age: 0} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getLocalTupleAnnotations(typeof x1, "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({name: "chiranS", age: 26},  <map<anydata>>m1["details"]);
    assertEquality({name: "name", age: 0},  <map<anydata>>m2["details"]);
}

string gVar = "foo";
string gVar1 = "bar";

function testAnnotationOnTupleWithGlobalVariable() {
    [@annotOne {value: gVar} int, int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m = getLocalTupleAnnotations(typeof x1, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m["annotOne"]);
}

function testMultipleAnnotationsOnLocalTuple() {
    string k = "chiranS";
    [@annotOne {value: "foo"} @annotTwo {value: "bar"} int, @details {name: k, age: 0} int, string...] x1 =  [1, 2, "hello", "world"];
    map<any> m1 = getLocalTupleAnnotations(typeof x1, "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof x1, "$field$.1");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "bar"}, <map<anydata>>m1["annotTwo"]);
    assertEquality({name: "chiranS", age: 0},  <map<anydata>>m2["details"]);
}

function() returns [int] x = function() returns [@annotOne {value: "foo"} int] {return [1];};
function() returns [int] x2 = function() returns [@annotOne {value: gVar1} @annotTwo {value: gVar2} int] {return [1];};
function() returns [int, int] x3 = function() returns [@annotOne {value: gVar1} int, @details {name: "name", age: gVar3} int] {return [1, 1];};

function testGlobalAnnotationsOnFunctionPointerReturnType() {
    map<any> m1 = getLocalTupleAnnotations(typeof x(), "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof x2(), "$field$.0");
    map<any> m3 = getLocalTupleAnnotations(typeof x3(), "$field$.0");
    map<any> m4 = getLocalTupleAnnotations(typeof x3(), "$field$.1");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "bar"}, <map<anydata>>m2["annotOne"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({value: "bar"}, <map<anydata>>m3["annotOne"]);
    assertEquality({name: "name", age: 10}, <map<anydata>>m4["details"]);
}

function testTupleMemberAnnotations1() returns [@annotOne {value: "foo"} int, @details {name: "name", age: gVar3} int] {
    return [1, 1];
}

function testTupleMemberAnnotations2() returns [@annotOne {value: gVar1} @annotTwo {value: gVar2} int] {
    return [1];
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
    map<any> m1 = getLocalTupleAnnotations(typeof func(), "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof func1(), "$field$.0");
    map<any> m3 = getLocalTupleAnnotations(typeof func2(), "$field$.0");
    map<any> m4 = getLocalTupleAnnotations(typeof func2(), "$field$.1");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "foo"}, <map<anydata>>m2["annotOne"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({name: "name", age: 15}, <map<anydata>>m3["details"]);
    assertEquality({value: "baz"}, <map<anydata>>m4["annotTwo"]);
}

int gVar4 = 15;

function func3([@annotOne {value: "foo"} int] a) {
    map<any> m1 = getLocalTupleAnnotations(typeof a, "$field$.0");
    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
}

function func4([@annotOne {value: "foo"} int, @annotTwo {value: "foo"} @details {name: "name", age: gVar4} int] a) {
    map<any> m1 = getLocalTupleAnnotations(typeof a, "$field$.0");
    map<any> m2 = getLocalTupleAnnotations(typeof a, "$field$.1");

    assertEquality({value: "foo"}, <map<anydata>>m1["annotOne"]);
    assertEquality({value: "foo"}, <map<anydata>>m2["annotTwo"]);
    assertEquality({name: "name", age: 15}, <map<anydata>>m2["details"]);
}

function testGlobalAnnotationsOnFunctionParameterType() {
    func3([10]);
    func4([10, 10]);
}

function getLocalTupleAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalTupleAnnotationTest",
    name: "getLocalTupleAnnotations"
} external;

function assertEquality(anydata expected, anydata  actual) {

    if expected == actual {
        return;
    }

    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
