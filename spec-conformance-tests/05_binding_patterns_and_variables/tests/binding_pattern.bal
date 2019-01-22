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
@test:Config {}
function testSimpleBindingPattern() {
    // a simple-binding-pattern matches any value; if the simple-binding-pattern consists of a variable-name
    // then it causes the matched value to be assigned to the named variable;
    int var1;
    var1 = 5;
    test:assertEquals(var1, 5, msg = "expected simple value to be assigned to simple binding pattern");
}

@test:Config {}
function testTupleBindingPattern() {
    int var1;
    string var2;
    float var3;
    decimal var4;
    boolean var5;
    () var6;
    int[] var7;

    // a tuple-binding-pattern (p1, p2, …, pn) matches a list value of length n [v1, v2, …, vn]
    // if pi matches vi for each i in 1 to n;
    (var1, var2, var3, var4, var5, _, var7) = (10, "string", 15.5, <decimal>19.9, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = "expected tuple value to be destructured to variable references");
    test:assertEquals(var2, "string", msg = "expected tuple value to be destructured to variable references");
    test:assertEquals(var3, 15.5, msg = "expected tuple value to be destructured to variable references");
    test:assertEquals(var4, <decimal>19.9, msg = "expected tuple value to be destructured to variable references");
    test:assertEquals(var5, true, msg = "expected tuple value to be destructured to variable references");
    int[] expectedArray = [1, 2, 3];
    test:assertEquals(var7, expectedArray, msg = "expected tuple value to be destructured to variable references");
}

type BindingPattern record {
    int field1;
    string field2;
    float field3;
    decimal var4;
    boolean var5;
    () field6;
    int[] field7;
};

type ClosedBindingPattern record {
    int field1;
    string field2;
    float field3;
    !...;
};

@test:Config {}
function testRecordBindingPattern() {
    BindingPattern bindingPattern = { field1: 11, field2: "string2", field3: 16.5, var4: <decimal>18.9,
        var5: false, field6: (), field7: [1, 2, 4] };

    int var1;
    string var2;
    float var3;
    decimal var4;
    boolean var5;
    () var6;
    int[] var7;
    string var8;
    map<anydata> restParam = {};
    map<anydata> restParam2 = {};
    map<anydata> restParam3 = {};

    // a record-binding-pattern { f1: p1, f2: p2, …, fn: pn, r } matches a mapping value m that has fields f1, f2, ... ,
    // fn if pi matches the value of field fi for each i in 1 to n
    // a field-binding-pattern consisting of just a variable name x is equivalent to a field-binding-pattern x: x
    { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, 11, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var2, "string2", msg = "expected record value to be destructured to variable references");
    test:assertEquals(var3, 16.5, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var4, <decimal>18.9, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var5, false, msg = "expected record value to be destructured to variable references");
    int[] expectedArray = [1, 2, 4];
    test:assertEquals(var7, expectedArray, msg = "expected record value to be destructured to variable references");

    // and if the rest-binding-pattern r, if present,
    // matches a new mapping x consisting of all the fields other than f1, f2,...,fn
    //  if r is !..., it matches x if x is empty
    //  if r is ...v it matches any mapping x and causes x to be assigned to v
    //  if r is missing, the match of the record-binding-pattern is not affected by x
    ClosedBindingPattern cBindingPattern = { field1: 15, field2: "string3", field3: 100.1 };

    { field1: var1, field2: var2, field3: var3, !... } = cBindingPattern;

    test:assertEquals(var1, 15, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var2, "string3", msg = "expected record value to be destructured to variable references");
    test:assertEquals(var3, 100.1, msg = "expected record value to be destructured to variable references");

    bindingPattern.field2 = "string4";
    bindingPattern.field3 = 19.9;
    bindingPattern.field7 = [8, 9, 10, 11];

    { field2: var2, var5, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var2, "string4", msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam.field3, 19.9, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam.var4, <decimal>18.9,
        msg = "expected record value to be destructured to variable references");
    test:assertEquals(var5, false, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam.field6, (), msg = "expected record value to be destructured to variable references");
    expectedArray = [8, 9, 10, 11];
    test:assertEquals(restParam.field7, expectedArray,
        msg = "expected record value to be destructured to variable references");

    { field2: var8, ...restParam2 } = bindingPattern;

    test:assertEquals(restParam2.field1, 11, msg = "expected record value to be destructured to variable references");
    test:assertEquals(var8, "string4", msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam2.field3, 19.9, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam2.var4, <decimal>18.9,
    msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam2.var5, false, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam2.field6, (), msg = "expected record value to be destructured to variable references");
    expectedArray = [8, 9, 10, 11];
    test:assertEquals(restParam2.field7, expectedArray,
    msg = "expected record value to be destructured to variable references");

    { ...restParam3 } = bindingPattern;

    test:assertEquals(restParam3.field1, 11, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam3.field2, "string4",
        msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam3.field3, 19.9, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam3.var4, <decimal>18.9,
        msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam3.var5, false, msg = "expected record value to be destructured to variable references");
    test:assertEquals(restParam3.field6, (), msg = "expected record value to be destructured to variable references");
    expectedArray = [8, 9, 10, 11];
    test:assertEquals(restParam3.field7, expectedArray,
        msg = "expected record value to be destructured to variable references");
}

type FooRecord record {
    string field1;
    (int, float) field2;
};

@test:Config {}
function testComplexTupleBindingPattern() {
    FooRecord foo = { field1: "string value", field2: (25, 12.5) };
    map<anydata> anydataMap = { x: "string in map", y: 100 };
    json jsonValue = "json value";
    (boolean, FooRecord, (FooRecord, int, (string, float)), (map<anydata>, json)) tupleValue =
        (true, foo, (foo, 50, ("string value 2", 75.5)), (anydataMap, jsonValue));

    boolean a;
    FooRecord b;
    string c;
    (int, float) d;
    int e;
    string f;
    float g;
    map<anydata> h;
    json i;

    (a, b, ({ field1: c, field2: d }, e, (f, g)), (h, i)) = tupleValue;

    test:assertTrue(a == true, msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(c == "string value",
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(d == (25, 12.5),
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(e == 50,
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(f == "string value 2",
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(g == 75.5, msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected tuple binding pattern value to be destructured to variable references");
    test:assertTrue(i == "json value",
        msg = "expected tuple binding pattern value to be destructured to variable references");
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
    ComplexRecordTwo bRecord = { bField1: 12.5, bField2: ("string value", true) };
    ComplexRecord aRecord = { aField1: bRecord, aField2: ("json value", bRecord), aField3:
        ("string value 2", 50, { x: "string in map", y: 100 }) };

    ComplexRecordTwo a;
    json b;
    float c;
    (string, boolean) d;
    string e;
    int f;
    map<anydata> g;

    { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;
    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(b == "json value",
        msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(c == 12.5, msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(d == ("string value", true),
        msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(e == "string value 2",
        msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(f == 50,
        msg = "expected record binding pattern value to be destructured to variable references");
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected record binding pattern value to be destructured to variable references");
}
