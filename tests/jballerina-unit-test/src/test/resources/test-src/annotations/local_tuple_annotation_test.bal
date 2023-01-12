// Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
