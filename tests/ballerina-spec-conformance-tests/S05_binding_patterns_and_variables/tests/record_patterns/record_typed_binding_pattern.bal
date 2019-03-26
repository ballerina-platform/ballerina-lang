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
const int INT_FIELD = 11;
const int INT_FIELD_2 = 50;
const int INT_FIELD_3 = 100;
const string STRING_FIELD = "string value";
const string STRING_FIELD_2 = "string value 2";
const string STRING_FIELD_3 = "string in map";
const float FLOAT_FIELD = 16.5;
const float FLOAT_FIELD_2 = 12.1;
const decimal DECIMAL_FIELD = 18.9;
const boolean BOOLEAN_FIELD = true;
const () NIL_FIELD = ();
json jsonField = "json value";

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
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    BindingPattern { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[8, 9, 10, 11], msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    BindingPattern { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var8, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithNoRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    BindingPattern { field2: var8, var6 } = bindingPattern;

    test:assertEquals(var8, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithClosedRestParam() {
    ClosedBindingPattern cBindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD
    };

    ClosedBindingPattern {| field1: field1, field2: field2, field3 |} = cBindingPattern;

    test:assertEquals(field1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTypedBindingPatternWithOnlyRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    BindingPattern { ...restParam } = bindingPattern;
    test:assertEquals(restParam.field1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPattern() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    var { field2: var2, var5, field6: _, field1: var1, var4, field7: var7, field3: var3 } = bindingPattern;

    test:assertEquals(var1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[8, 9, 10, 11], msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    var { field2: var8, var6, ...restParam } = bindingPattern;

    test:assertEquals(restParam.field1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var8, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithNoRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    var { field2: var8, var6 } = bindingPattern;

    test:assertEquals(var8, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithClosedRestParam() {
    ClosedBindingPattern cBindingPattern = {
        field1: 15,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD_2
    };

    var {| field1: field1, field2: field2, field3 |} = cBindingPattern;

    test:assertEquals(field1, 15, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(field3, FLOAT_FIELD_2, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedRecordTypedBindingPatternWithOnlyRestParam() {
    BindingPattern bindingPattern = {
        field1: INT_FIELD,
        field2: STRING_FIELD,
        field3: FLOAT_FIELD,
        var4: DECIMAL_FIELD,
        var5: BOOLEAN_FIELD,
        field6: NIL_FIELD,
        field7: [8, 9, 10, 11]
    };

    var { ...restParam } = bindingPattern;
    test:assertEquals(restParam.field1, INT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field2, STRING_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field3, FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var4, DECIMAL_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.var5, BOOLEAN_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field6, (), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(restParam.field7, <int[]>[8, 9, 10, 11],
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testComplexRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = {
        bField1: FLOAT_FIELD,
        bField2: (STRING_FIELD, BOOLEAN_FIELD)
    };

    ComplexRecord aRecord = {
        aField1: bRecord,
        aField2: (jsonField, bRecord),
        aField3: (STRING_FIELD_2, INT_FIELD_2, { x: STRING_FIELD_3, y: INT_FIELD_3 })
    };

    ComplexRecord { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: FLOAT_FIELD, bField2: (STRING_FIELD, BOOLEAN_FIELD) },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == jsonField, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == (STRING_FIELD, BOOLEAN_FIELD), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == STRING_FIELD_2, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == INT_FIELD_2, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == <map<anydata>>{ x: STRING_FIELD_3, y: INT_FIELD_3 },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testComplexImpliedRecordTypedBindingPattern() {
    ComplexRecordTwo bRecord = {
        bField1: FLOAT_FIELD,
        bField2: (STRING_FIELD, BOOLEAN_FIELD)
    };

    ComplexRecord aRecord = {
        aField1: bRecord,
        aField2: (jsonField, bRecord),
        aField3: (STRING_FIELD_2, INT_FIELD_2, { x: STRING_FIELD_3, y: INT_FIELD_3 })
    };

    var { aField1: a, aField2: (b, { bField1: c, bField2: d }), aField3: (e, f, g) } = aRecord;

    test:assertTrue(a == <ComplexRecordTwo>{ bField1: FLOAT_FIELD, bField2: (STRING_FIELD, BOOLEAN_FIELD) },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == jsonField, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == FLOAT_FIELD, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == (STRING_FIELD, BOOLEAN_FIELD), msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == STRING_FIELD_2, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == INT_FIELD_2, msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == <map<anydata>>{ x: STRING_FIELD_3, y: INT_FIELD_3 },
        msg = EXPECTED_RECORD_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}
