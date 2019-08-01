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

import ballerina/test;

const EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE = "expected record value to be destructured to variable references";

// binding-pattern :=
//    simple-binding-pattern
//    | structured-binding-pattern
// simple-binding-pattern := variable-name | ignore
// variable-name := identifier
// ignore := _
// structured-binding-pattern :=
//    | tuple-binding-pattern
//    | record-binding-pattern
//    | error-binding-pattern
// tuple-binding-pattern := ( binding-pattern (, binding-pattern)+ )
// record-binding-pattern := { entry-binding-patterns }
// entry-binding-patterns :=
//    field-binding-patterns [, rest-binding-pattern]
//    | [ rest-binding-pattern ]
// field-binding-patterns :=
//   field-binding-pattern (, field-binding-pattern)*
// field-binding-pattern :=
//    field-name : binding-pattern
//    | variable-name
// rest-binding-pattern := ... variable-name | ! ...
// error-binding-pattern :=
//    error ( simple-binding-pattern [, error-detail-binding-pattern] )
// error-detail-binding-pattern :=
//    simple-binding-pattern | record-binding-pattern

// a record-binding-pattern { f1: p1, f2: p2, …, fn: pn, r } matches a mapping value m that has fields f1, f2, ... ,
// fn if pi matches the value of field fi for each i in 1 to n
// a field-binding-pattern consisting of just a variable name x is equivalent to a field-binding-pattern x: x

// and if the rest-binding-pattern r, if present,
// matches a new mapping x consisting of all the fields other than f1, f2,...,fn
//  if r is !..., it matches x if x is empty
//  if r is ...v it matches any mapping x and causes x to be assigned to v
//  if r is missing, the match of the record-binding-pattern is not affected by x

type BindingPattern record {
    int field1;
    string field2;
    float field3;
    decimal var4;
    boolean var5;
    () field6;
    int[] field7;
};

@test:Config {}
function testRecordBindingPatternWithNoRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: 18.9d,
        var5: false,
        field6: (),
        field7: [1, 2, 4]
    };

    int var1;
    string var2;
    float var3;
    decimal var4;
    boolean var5;
    () var6;
    int[] var7;
    string var8;

    { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var4, 18.9d, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 4], msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordBindingPatternWithRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: 18.9d,
        var5: false,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    string var1;
    boolean var5;
    map<anydata|error> restParam = {};

    { field2: var1, var5, ...restParam } = bindingPattern;

    test:assertEquals(var1, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);

    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, 18.9d, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11], msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordBindingPatternWithMissingRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: 18.9d,
        var5: true,
        field6: (),
        field7: [1, 2, 4]
    };

    string var1;
    boolean var5;

    { field2: var1, var5 } = bindingPattern;

    test:assertEquals(var1, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var5, true, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordBindingPatternWithOnlyRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: 18.9d,
        var5: true,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    map<anydata|error> restParam = {};

    { ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field2, "string value",
        msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, 18.9d, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, true, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11], msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}

type ClosedBindingPattern record {|
    int field1;
    string field2;
    float field3;
|};

@test:Config {}
function testRecordBindingPatternWithClosedRestParam() {
    ClosedBindingPattern cBindingPattern = {
        field1: 15,
        field2: "string value",
        field3: 100.1
    };

    int var1;
    string var2;
    float var3;

    {| field1: var1, field2: var2, field3: var3 |} = cBindingPattern;

    test:assertEquals(var1, 15, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var3, 100.1, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}

type ComplexRecord record {
    ComplexRecordTwo aField1;
    (json, ComplexRecordTwo) aField2;
    (string, int, map<anydata>) aField3;
};

type ComplexRecordTwo record {
    float bField1;
    (string, boolean) bField2;
};

@test:Config {}
function testComplexRecordBindingPattern() {
    ComplexRecordTwo bRecord = {
        bField1: 12.5,
        bField2: ("string value", true)
    };

    ComplexRecord aRecord = {
        aField1: bRecord,
        aField2: ("json value", bRecord),
        aField3: ("string value 2", 50, { x: "string in map", y: 100 })
    };

    ComplexRecordTwo a;
    json b;
    float c;
    (string, boolean) d;
    string e;
    int f;
    map<anydata> g;

    { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(b == "json value", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(c == 12.5, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(d == ("string value", true), msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(e == "string value 2", msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(f == 50, msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_RECORD_DESTRUCTURE_FAILURE_MESSAGE);
}
