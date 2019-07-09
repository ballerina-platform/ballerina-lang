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

const EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE = "expected the value to be updated";
const EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE = "expected the original value to be updated";

// All basic types of structural values, with the exception of the XML,
// are mutable, meaning the value referred to by a particular reference
// can be changed.

@test:Config {}
function testArrayMutation() {
    int[2] s1 = [12, 13];
    int[4][2] s2 = [[1, 2], [3, 4], [11, 12], [21, 24]];
    int[4][2] s3 = s2;

    s2[1] = s1;
    test:assertEquals(s2[1], s1, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertTrue(s2 === s3, msg = EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
}

@test:Config {}
function testTupleMutation() {
    FooRecordOne f1 = { fooOneFieldOne: "test string 1" };
    FooRecordOne f2 = { fooOneFieldOne: "test string 2" };
    (int, FooRecordOne, FooRecordOne) s1 = (1, f1, f1);
    (int, FooRecordOne, FooRecordOne) s2 = s1;

    s1[2] = f2;
    test:assertEquals(s1[2], f2, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertTrue(s1 === s2, msg = EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
}

@test:Config {}
function testMapMutation() {
    FooObject f1 = new("test string 1");
    FooObject f2 = new("test string 2");
    FooObject f3 = new("test string 3");

    map<FooObject> s1 = { one: f1 };
    map<FooObject> s2 = s1;

    s2.one = f3;
    s2.two = f2;
    test:assertEquals(s2.one, f3, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertEquals(s2.two, f2, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertTrue(s1 === s2, msg = EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
}

type CorgeRecord record {
   int fieldOne;
};

@test:Config {}
function testRecordMutation() {
    CorgeRecord q1 = { fieldOne: 1 };
    CorgeRecord q2 = q1;

    int i = 2;
    string s = "test string 2";

    q1.fieldOne = i;
    q1.fieldTwo = s;

    test:assertEquals(q1.fieldOne, i, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertEquals(q1.fieldTwo, s, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertTrue(q1 === q2, msg = EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
}

public type TableConstraintTwo record {|
    int constraintField;
|};

@test:Config {}
function testTableMutation() {
    TableConstraintTwo tableEntry1 = { constraintField: 1 };
    TableConstraintTwo tableEntry2 = { constraintField: 2 };
    table<TableConstraintTwo> t1 = table{};
    error? err1 = t1.add(tableEntry1);
    if err1 is error {
        test:assertFail(msg = "failed in adding record to table");
    }

    table<TableConstraintTwo> t2 = t1;

    error? err2 = t1.add(tableEntry2);
    if err2 is error {
        test:assertFail(msg = "failed in adding record to table");
    }

    int count = 0;
    foreach TableConstraintTwo entry in t1 {
        count += entry.constraintField;
    }
    test:assertEquals(count, 3, msg = EXPECTED_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
    test:assertTrue(t1 === t2, msg = EXPECTED_ORIGINAL_VALUE_TO_BE_UPDATED_FAILURE_MESSAGE);
}
