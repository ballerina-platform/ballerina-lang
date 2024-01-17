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
    record {
        @annot {value: k}
        string x;
    } r = {x: ""};

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
}

function testAnnotationWithMultipleFieldsOnLocalRecord() {
    string k = "chiranS";
    record {
        @details {name: k, age: 26}
        string x;
    } r = {x: ""};

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({name: "chiranS", age: 26}, <map<anydata>>m["details"]);
}

function testAnnotationOnLocalRecordWithMultipleFields() {
    string k = "chiranS";
    record {
        @annot {value: k}
        string x;
        @annot {value: k}
        string y;
    } r = {x: "", y: ""};

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);

    map<any> y = getLocalRecordAnnotations(typeof r, "$field$.y");
    assertEquality({value: "chiranS"}, <map<anydata>>y["annot"]);
}

string gVar = "foo";

function testAnnotationOnLocalRecordWithGlobalVariable() {
    record {
        @annot {value: gVar}
        string x;
    } r = {x: ""};

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "foo"}, <map<anydata>>m["annot"]);
}

function testAnnotationOnLocalRecordWithinLambdaFunction() {
    string k = "chiranS";
    function () returns map<any> func = function() returns map<any> {
        record {
            @annot {value: k}
            string x;
        } r = {x: ""};
        return getLocalRecordAnnotations(typeof r, "$field$.x");
    };
    map<any> x = func();
    assertEquality({value: "chiranS"}, <map<anydata>>x["annot"]);
}

function testAnnotationOnLocalRecordWithinLambdaFunction1() {
    string k = "chiranS";
    function (string) returns map<any> func = function(string value) returns map<any> {
        record {
            @annot {value: value}
            string x;
        } r = {x: ""};
        return getLocalRecordAnnotations(typeof r, "$field$.x");
    };
    map<any> x = func(k);
    assertEquality({value: "chiranS"}, <map<anydata>>x["annot"]);
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions() {
    string k = "chiranS";
    function () returns function () returns map<any> var1 = function() returns function () returns map<any> {
        function () returns map<any> func = function() returns map<any> {
            record {@annot {value: k}
                string x;} r = {x: ""};
            return getLocalRecordAnnotations(typeof r, "$field$.x");
        };
        return func;
    };
    function () returns map<any> func = var1();
    map<any> x = func();
    assertEquality({value: "chiranS"}, <map<anydata>>x["annot"]);
}

function testAnnotationOnLocalRecordWithinNestedLambdaFunctions1() {
    string k = "chiranS";
    function (string) returns function () returns map<any> var1 = function(string val) returns function () returns map<any> {
        function () returns map<any> func = function() returns map<any> {
            record {@annot {value: val} string x;} r = {x: ""};
            return getLocalRecordAnnotations(typeof r, "$field$.x");
        };
        return func;
    };
    function () returns map<any> func = var1(k);
    map<any> m = func();
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
}

function testMultipleAnnotationsOnLocaRecord() {
    string k = "chiranS";
    record {
        @annot {value: k}
        @annot1 {value: k}
        string x;
    } r = {x: ""};

    map<any> m = getLocalRecordAnnotations(typeof r, "$field$.x");
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot"]);
    assertEquality({value: "chiranS"}, <map<anydata>>m["annot1"]);
}

string gVar1 = "bar";

function () returns record {string x;} x = function() returns record {@annot {value: "value"}
    string x;} {
    return {x: ""};
};
function () returns record {string x;} x2 = function() returns record {
    @annot {value: gVar1}
    @annot1 {value: gVar2}
    string x;
} {
    return {x: ""};
};
function () returns record {int x; int y;} x3 = function() returns record {
    @annot {value: gVar1}
    int x;
    @details {name: "name", age: gVar3}
    int y;
} {
    return {x: 10, y: 10};
};

function testGlobalAnnotationsOnFunctionPointerReturnType() {
    map<any> m1 = getLocalRecordAnnotations(typeof x(), "$field$.x");
    map<any> m2 = getLocalRecordAnnotations(typeof x2(), "$field$.x");
    map<any> m3 = getLocalRecordAnnotations(typeof x3(), "$field$.x");
    map<any> m4 = getLocalRecordAnnotations(typeof x3(), "$field$.y");
    assertEquality({value: "value"}, <map<anydata>>m1["annot"]);
    assertEquality({value: "bar"}, <map<anydata>>m2["annot"]);
    assertEquality({value: "baz"}, <map<anydata>>m2["annot1"]);
    assertEquality({value: "bar"}, <map<anydata>>m3["annot"]);
    assertEquality({name: "name", age: 10}, <map<anydata>>m4["details"]);
}

string gVar2 = "baz";
int gVar3 = 10;

function getLocalRecordAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalRecordAnnotationTest",
    name: "getLocalRecordAnnotations"
} external;

function assertEquality(anydata expected, anydata actual) {

    if expected == actual {
        return;
    }

    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
