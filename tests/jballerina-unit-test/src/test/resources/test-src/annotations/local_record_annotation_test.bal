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

import ballerina/jballerina.java;

type AnnotRecord record {|
    string value;
|};

type Details record {|
    string name;
    int age;
|};

annotation AnnotRecord annot on type, field;
annotation AnnotRecord annot1 on type, field;
annotation Details details on field;

function testAnnotationOnLocalRecord() {
    string k = "chiranS";
    record {@annot {value: k} string x;} r = {x : ""}; 

    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality("typedesc map", (typeof m).toString());
    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 
}

function testAnnotationWithMultipleFieldsOnLocalRecord() {
    string k = "chiranS";
    record {@details {name: k, age: 26} string x;} r = {x : ""}; 

    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality("typedesc map", (typeof m).toString());
    assertEquality("{\"details\":{\"name\":\"chiranS\",\"age\":26}}", m.toString()); 
}

function testAnnotationOnLocalRecordWithMultipleFields() {
    string k = "chiranS";
    record {@annot {value: k} string x; @annot {value: k} string y; } r = {x : "", y: ""}; 

    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality("typedesc map", (typeof m).toString());
    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 

    any y = getLocalRecordAnnotations(typeof r, "$field$.y");
    assertEquality("typedesc map", (typeof y).toString());
    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", y.toString()); 
}


string gVar = "foo";
function testAnnotationOnLocalRecordWithGlobalVariable() {
    record {@annot {value: gVar} string x;} r = {x : ""}; 

    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality("typedesc map", (typeof m).toString());
    assertEquality("{\"annot\":{\"value\":\"foo\"}}", m.toString()); 
}

function testAnnotationOnLocalRecordWithinLambdaFunction() {
    string k = "chiranS";
    function() func = function () {
                                    record {@annot {value: k} string x;} r = {x : ""}; 
                                    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality("typedesc map", (typeof m).toString());
                                    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 
                                };
    func();
}

function testAnnotationOnLocalRecordWithinLambdaFunction1() {
    string k = "chiranS";
    function(string) func = function (string value) {
                                    record {@annot {value: value} string x;} r = {x : ""}; 
                                    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality("typedesc map", (typeof m).toString());
                                    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 
                                };
    func(k);
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions() {
    string k = "chiranS";
    function () returns function() var1 = function () returns function() {
                                function() func = function () {
                                    record {@annot {value: k} string x;} r = {x : ""}; 
                                    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality("typedesc map", (typeof m).toString());
                                    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 
                                };
                                return func;
                                };
    function() func = var1();
    func();
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions1() {
    string k = "chiranS";
    function (string) returns function() var1 = function (string val) returns function() {
                                function() func = function () {
                                    record {@annot {value: val} string x;} r = {x : ""}; 
                                    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality("typedesc map", (typeof m).toString());
                                    assertEquality("{\"annot\":{\"value\":\"chiranS\"}}", m.toString()); 
                                };
                                return func;
                                };
    function() func = var1(k);
    func();
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions2() {
    string k = "chiranS";
    function (string) returns function() var1 = function (string val) returns function() {
                                string name = "Name: ";
                                function() func = function () {
                                    record {@annot {value: name + val} string x;} r = {x : ""}; 
                                    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality("typedesc map", (typeof m).toString());
                                    assertEquality("{\"annot\":{\"value\":\"Name: chiranS\"}}", m.toString()); 
                                };
                                return func;
                                };
    function() func = var1(k);
    func();
}

function testMultipleAnnotationsOnLocaRecord() {
    string k = "chiran";
    record {@annot {value: k} @annot1 {value: k} string x;} r = {x : ""}; 

    any m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality("typedesc map", (typeof m).toString());
}

function testLocalRecordAnnotations() {
        testMultipleAnnotationsOnLocaRecord();
        testAnnotationOnLocalRecord();
        testAnnotationOnLocalRecordWithGlobalVariable();
        testAnnotationOnLocalRecordWithinLambdaFunction();
        testAnnotationOnLocalRecordWithinLambdaFunction1();
        testAnnotationOnLocalRecordWithinNestedLambdaFunctions();
        testAnnotationOnLocalRecordWithinNestedLambdaFunctions1();
        testAnnotationOnLocalRecordWithinNestedLambdaFunctions2();
        testAnnotationWithMultipleFieldsOnLocalRecord();
        testAnnotationOnLocalRecordWithMultipleFields();
}

function getLocalRecordAnnotations(typedesc<any> obj, string annotName) returns any =
@java:Method {
    'class: "org/ballerinalang/test/annotations/GetLocalRecordAnnotations",
    name: "getLocalRecordAnnotations"
} external;

function assertEquality(any|error expected, any|error  actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
                                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
