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

const EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE =
                        "expected record value to be destructured to variable defintions";

// typed-binding-pattern := impliable-type-descriptor binding-pattern
// impliable-type-descriptor := type-descriptor | var

// A typed-binding-pattern combines a type-descriptor and a binding-pattern,
// and is used to create the variables occurring in the binding-pattern. If var is used instead of a type-descriptor,
// it means the type is implied. How the type is implied depends on the context of the typed-binding-pattern.
// The simplest and most common form of a typed-binding-pattern is for the binding pattern to consist of just a
// variable name. In this cases, the variable is constrained to contain only values matching the type descriptor.
@test:Config {}
function testRecordTypedBindingPattern() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [1, 2, 4]
    };

    BindingPattern { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 4], msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    BindingPattern { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var8, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithNoRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [1, 2, 4]
    };

    BindingPattern { field2: var8, var6} = bindingPattern;

    test:assertEquals(var8, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithClosedRestParam() {
    ClosedBindingPattern cBindingPattern = {
        field1: 15,
        field2: "string value",
        field3: 100.1
    };

    ClosedBindingPattern { field1: field1, field2: field2, field3, !... } = cBindingPattern;

    test:assertEquals(field1, 15, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field3, 100.1, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithOnlyRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    BindingPattern { ...restParam } = bindingPattern;
    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPattern() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [1, 2, 4]
    };

    var { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 4], msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    var { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var8, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithNoRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [1, 2, 4]
    };

    var { field2: var8, var6} = bindingPattern;

    test:assertEquals(var8, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithClosedRestParam() {
    ClosedBindingPattern cBindingPattern = {
        field1: 15,
        field2: "string value",
        field3: 100.1
    };

    var { field1: field1, field2: field2, field3, !... } = cBindingPattern;

    test:assertEquals(field1, 15, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field3, 100.1, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithOnlyRestParam() {
    BindingPattern bindingPattern = {
        field1: 11,
        field2: "string value",
        field3: 16.5,
        var4: <decimal>18.9,
        var5: false,
        field6: (),
        field7: [8, 9, 10, 11]
    };

    var { ...restParam } = bindingPattern;
    test:assertEquals(restParam.field1, 11, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field2, "string value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, 16.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, <decimal>18.9, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, false, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testComplexRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = {
        bField1: 12.5,
        bField2: ("string value", true)
    };

    ComplexRecord aRecord = {
        aField1: bRecord,
        aField2: ("json value", bRecord),
        aField3: ("string value 2", 50, { x: "string in map", y: 100 })
    };

    ComplexRecord { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == "json value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == 12.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == ("string value", true), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == "string value 2", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == 50, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testComplexImpliedRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = {
        bField1: 12.5,
        bField2: ("string value", true)
    };

    ComplexRecord aRecord = {
        aField1: bRecord,
        aField2: ("json value", bRecord),
        aField3: ("string value 2", 50, { x: "string in map", y: 100 })
    };

    var { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: 12.5, bField2: ("string value", true) },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == "json value", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == 12.5, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == ("string value", true), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == "string value 2", msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == 50, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}
