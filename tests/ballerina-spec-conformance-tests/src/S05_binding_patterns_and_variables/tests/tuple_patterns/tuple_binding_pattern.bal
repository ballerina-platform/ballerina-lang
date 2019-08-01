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

const EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE = "expected tuple value to be destructured to variable references";

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
    (var1, var2, var3, var4, var5, _, var7) = (10, "string", 15.5, 19.9d, true, (), [1, 2, 3]);

    test:assertEquals(var1, 10, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var2, "string", msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var3, 15.5, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var4, 19.9d, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var5, true, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertEquals(var7, <int[]>[1, 2, 3], msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
}

type FooRecord record {
    string field1;
    (int, float) field2;
};

@test:Config {}
function testComplexTupleBindingPattern() {
    FooRecord foo = {
        field1: "string value",
        field2: (25, 12.5)
    };
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

    test:assertTrue(a == true, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(b == <FooRecord>{ field1: "string value", field2: (25, 12.5) },
        msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(c == "string value", msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(d == (25, 12.5), msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(e == 50, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(f == "string value 2", msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(g == 75.5, msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(h == <map<anydata>>{ x: "string in map", y: 100 },
        msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
    test:assertTrue(i == "json value", msg = EXPECTED_TUPLE_DESTRUCTURE_FAILURE_MESSAGE);
}
