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

// typed-binding-pattern := impliable-type-descriptor binding-pattern
// impliable-type-descriptor := type-descriptor | var

// A typed-binding-pattern combines a type-descriptor and a binding-pattern,
// and is used to create the variables occurring in the binding-pattern. If var is used instead of a type-descriptor,
// it means the type is implied. How the type is implied depends on the context of the typed-binding-pattern.
// The simplest and most common form of a typed-binding-pattern is for the binding pattern to consist of just a
// variable name. In this cases, the variable is constrained to contain only values matching the type descriptor.
@test:Config {}
function testSimpleTypedBindingPattern() {
    int var1 = 5;
    test:assertEquals(var1, 5, msg = "expected simple value to be assigned to simple binding pattern");
}

@test:Config {}
function testTupleTypedBindingPattern() {
    (int, string, float, decimal, boolean, (), int[]) (var1, var2, var3, var4, _, var6, var7) =
        (10, "string", 15.5, <decimal>19.9, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var2, "string", msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var3, 15.5, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var4, <decimal>19.9, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var6, (), msg = "expected tuple value to be destructured to variable defintions");
    int[] expectedArray = [1, 2, 3];
    test:assertEquals(var7, expectedArray, msg = "expected tuple value to be destructured to variable defintions");
}

@test:Config {}
function testRecordTypedBindingPattern() {
    BindingPattern bindingPattern = { field1: 11, field2: "string2", field3: 16.5, var4: <decimal>18.9,
        var5: false, field6: (), field7: [1, 2, 4] };

    BindingPattern { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;
    ClosedBindingPattern cBindingPattern = { field1: 15, field2: "string3", field3: 100.1 };

    test:assertEquals(var1, 11, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var2, "string2", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var3, 16.5, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var4, <decimal>18.9, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var5, false, msg = "expected record value to be destructured to variable defintions");
    int[] expectedArray = [1, 2, 4];
    test:assertEquals(var7, expectedArray, msg = "expected record value to be destructured to variable defintions");

    ClosedBindingPattern { field1: field1, field2: field2, field3, !... } = cBindingPattern;

    test:assertEquals(field1, 15, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(field2, "string3", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(field3, 100.1, msg = "expected record value to be destructured to variable defintions");

    bindingPattern.field2 = "string4";
    bindingPattern.field3 = 19.9;
    bindingPattern.field7 = [8, 9, 10, 11];

    BindingPattern { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var8, "string4", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.field3, 19.9, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.var4, <decimal>18.9, 
        msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.var5, false, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var6, (), msg = "expected record value to be destructured to variable defintions");
    expectedArray = [8, 9, 10, 11];
    test:assertEquals(restParam.field7, expectedArray,
        msg = "expected record value to be destructured to variable defintions");
}

@test:Config {}
function testImpliedSimpleTypedBindingPattern() {
    var var1 = 5;
    test:assertEquals(var1, 5, msg = "expected simple value to be assigned to simple binding pattern");
}

@test:Config {}
function testImpliedTupleTypedBindingPattern() {
    var (var1, var2, var3, var4, _, var6, var7) =
    (10, "string", 15.5, <decimal>19.9, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var2, "string", msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var3, 15.5, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var4, <decimal>19.9, msg = "expected tuple value to be destructured to variable defintions");
    test:assertEquals(var6, (), msg = "expected tuple value to be destructured to variable defintions");
    int[] expectedArray = [1, 2, 3];
    test:assertEquals(var7, expectedArray, msg = "expected tuple value to be destructured to variable defintions");
}

@test:Config {}
function testImpliedRecordTypedBindingPattern() {
    BindingPattern bindingPattern = { field1: 11, field2: "string2", field3: 16.5, var4: <decimal>18.9,
        var5: false, field6: (), field7: [1, 2, 4] };
    ClosedBindingPattern cBindingPattern = { field1: 15, field2: "string3", field3: 100.1 };

    var { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;
    test:assertEquals(var1, 11, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var2, "string2", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var3, 16.5, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var4, <decimal>18.9, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var5, false, msg = "expected record value to be destructured to variable defintions");
    int[] expectedArray = [1, 2, 4];
    test:assertEquals(var7, expectedArray, msg = "expected record value to be destructured to variable defintions");

    var { field1: field1, field2: field2, field3, !... } = cBindingPattern;

    test:assertEquals(field1, 15, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(field2, "string3", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(field3, 100.1, msg = "expected record value to be destructured to variable defintions");

    bindingPattern.field2 = "string4";
    bindingPattern.field3 = 19.9;
    bindingPattern.field7 = [8, 9, 10, 11];

    var { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var8, "string4", msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.field3, 19.9, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.var4, <decimal>18.9,
        msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(restParam.var5, false, msg = "expected record value to be destructured to variable defintions");
    test:assertEquals(var6, (), msg = "expected record value to be destructured to variable defintions");
    expectedArray = [8, 9, 10, 11];
    test:assertEquals(restParam.field7, expectedArray,
        msg = "expected record value to be destructured to variable defintions");
}

@test:Config {}
function testComplexTupleTypedBindingPattern() {
    FooRecord foo = { field1: "string value", field2: (25, 12.5) };
    map<anydata> anydataMap = { x: "string in map", y: 100 };
    json jsonValue = "json value";
    (boolean, FooRecord, (FooRecord, int, (string, float)), (map<anydata>, json)) tupleValue =
                                            (true, foo, (foo, 50, ("string value 2", 75.5)), (anydataMap, jsonValue));

    (boolean, FooRecord, (FooRecord, int, (string, float)), (map<anydata>, json))
                                                    (a, b, ({ field1: c, field2: d }, e, (f, g)), (h, i)) = tupleValue;

    test:assertTrue(a == true, msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(c == "string value",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(d == (25, 12.5),
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(e == 50,
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(f == "string value 2",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(g == 75.5, msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(i == "json value",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
}

@test:Config {}
function testComplexImpliedTupleTypedBindingPattern() {
    FooRecord foo = { field1: "string value", field2: (25, 12.5) };
    map<anydata> anydataMap = { x: "string in map", y: 100 };
    json jsonValue = "json value";
    (boolean, FooRecord, (FooRecord, int, (string, float)), (map<anydata>, json)) tupleValue =
        (true, foo, (foo, 50, ("string value 2", 75.5)), (anydataMap, jsonValue));

    var (a, b, ({ field1: c, field2: d }, e, (f, g)), (h, i)) = tupleValue;

    test:assertTrue(a == true, msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(c == "string value",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(d == (25, 12.5),
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(e == 50,
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(f == "string value 2",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(g == 75.5, msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected tuple binding pattern value to be destructured to variable definition");
    test:assertTrue(i == "json value",
        msg = "expected tuple binding pattern value to be destructured to variable definition");
}

@test:Config {}
function testComplexRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = { bField1: 12.5, bField2: ("string value", true) };
    ComplexRecord aRecord = { aField1: bRecord, aField2: ("json value", bRecord),
                                                aField3: ("string value 2", 50, { x: "string in map", y: 100 }) };

    ComplexRecord { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(b == "json value",
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(c == 12.5, msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(d == ("string value", true),
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(e == "string value 2",
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(f == 50,
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected record binding pattern value to be destructured to variable definition");
}

@test:Config {}
function testComplexImpliedRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = { bField1: 12.5, bField2: ("string value", true) };
    ComplexRecord aRecord = { aField1: bRecord, aField2: ("json value", bRecord),
                                                aField3: ("string value 2", 50, { x: "string in map", y: 100 }) };

    var { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(b == "json value",
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(c == 12.5, msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(d == ("string value", true),
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(e == "string value 2",
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(f == 50,
        msg = "expected record binding pattern value to be destructured to variable definition");
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = "expected record binding pattern value to be destructured to variable definition");
}
