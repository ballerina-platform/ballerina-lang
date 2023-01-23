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

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
}

function testAnnotationWithMultipleFieldsOnLocalRecord() {
    string k = "chiranS";
    record {@details {name: k, age: 26} string x;} r = {x : ""}; 

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({name: "chiranS", age: 26},  <map<anydata>>m["details"]);
}

function testAnnotationOnLocalRecordWithMultipleFields() {
    string k = "chiranS";
    record {@annot {value: k} string x; @annot {value: k} string y; } r = {x : "", y: ""}; 

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);

    map<any> y = getLocalRecordAnnotations(typeof r, "$field$.y");
    assertEquality({value: "chiranS"}, <map<anydata>>y["annot"]);
}

string gVar = "foo";

function testAnnotationOnLocalRecordWithGlobalVariable() {
    record {@annot {value: gVar} string x;} r = {x : ""}; 

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "foo"}, <map<anydata>>m["annot"]);
}

function testAnnotationOnLocalRecordWithinLambdaFunction() {
    string k = "chiranS";
    function() func = function () {
                                    record {@annot {value: k} string x;} r = {x : ""}; 
                                    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality({value: "chiranS"} , <map<anydata>>m["annot"]);
                                };
    func();
}

function testAnnotationOnLocalRecordWithinLambdaFunction1() {
    string k = "chiranS";
    function(string) func = function (string value) {
                                    record {@annot {value: value} string x;} r = {x : ""}; 
                                    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
                                };
    func(k);
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions() {
    string k = "chiranS";
    function () returns function() var1 = function () returns function() {
                                function() func = function () {
                                    record {@annot {value: k} string x;} r = {x : ""}; 
                                    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
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
                                    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
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
                                    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
                                    assertEquality({value: "Name: chiranS"}, <map<anydata>>m["annot"]);
                                };
                                return func;
                                };
    function() func = var1(k);
    func();
}

function testMultipleAnnotationsOnLocaRecord() {
    string k = "chiranS";
    record {@annot {value: k} @annot1 {value: k} string x;} r = {x : ""}; 

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot1"]);
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

function getLocalRecordAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalRecordAnnotationTest",
    name: "getLocalRecordAnnotations"
} external;

function assertEquality(anydata expected, anydata  actual) {

    if expected == actual {
        return;
    }

    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
