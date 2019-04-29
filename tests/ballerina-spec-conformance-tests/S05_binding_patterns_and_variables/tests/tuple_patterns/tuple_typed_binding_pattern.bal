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

const EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE =
                        "expected tuple value to be destructured to variable defintions";

// typed-binding-pattern := impliable-type-descriptor binding-pattern
// impliable-type-descriptor := type-descriptor | var

// A typed-binding-pattern combines a type-descriptor and a binding-pattern,
// and is used to create the variables occurring in the binding-pattern. If var is used instead of a type-descriptor,
// it means the type is implied. How the type is implied depends on the context of the typed-binding-pattern.
// The simplest and most common form of a typed-binding-pattern is for the binding pattern to consist of just a
// variable name. In this cases, the variable is constrained to contain only values matching the type descriptor.
@test:Config {}
function testTupleTypedBindingPattern() {
    (int, string, float, decimal, boolean, (), int[]) (var1, var2, var3, var4, _, var6, var7) =
        (10, "string", 15.5, 19.9d, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, "string", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, 15.5, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, 19.9d, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 3], msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testImpliedTupleTypedBindingPattern() {
    var (var1, var2, var3, var4, _, var6, var7) = (10, "string", 15.5, 19.9d, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var2, "string", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var3, 15.5, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var4, 19.9d, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var6, (), msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 3], msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
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

    test:assertTrue(a == true, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == "string value", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == (25, 12.5), msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == 50, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == "string value 2", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == 75.5, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(i == "json value", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}

@test:Config {}
function testComplexImpliedTupleTypedBindingPattern() {
    FooRecord foo = { field1: "string value", field2: (25, 12.5) };
    map<anydata> anydataMap = { x: "string in map", y: 100 };
    json jsonValue = "json value";
    (boolean, FooRecord, (FooRecord, int, (string, float)), (map<anydata>, json)) tupleValue =
        (true, foo, (foo, 50, ("string value 2", 75.5)), (anydataMap, jsonValue));

    var (a, b, ({ field1: c, field2: d }, e, (f, g)), (h, i)) = tupleValue;

    test:assertTrue(a == true, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(c == "string value", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(d == (25, 12.5), msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(e == 50, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(f == "string value 2", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(g == 75.5, msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
    test:assertTrue(i == "json value", msg = EXPECTED_TUPLE_DESTRUCTURE_TO_VAR_DEF_FAILURE_MESSAGE);
}
