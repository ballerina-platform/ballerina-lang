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
import utils;

const string INHERENT_TYPE_VIOLATION_REASON = "{ballerina}InherentTypeViolation";
const string INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE =
                                            "invalid reason on inherent type violating array insertion";

// The inherent type of a list value determines a type Ti for a member with index i.
// The runtime system will enforce a constraint that a value written to index i will
// belong to type Ti. Note that the constraint is not merely that the value looks
// like Ti.

@test:Config {}
function testBasicTypeArrayInherentTypeViolation() {
    int[] intArray = [1, 2];
    any[] anyArray = intArray;
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, intArray.length() - 1, "not an int"),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    string[][] string2DArray = [["test string 1", "test string 2"], ["test string 3"]];
    anyArray = string2DArray;

    // `stringOrIntArray` looks like `string[]`
    (string|int)[] stringOrIntArray = ["test string 1", "test string 2"];
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, stringOrIntArray),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordArrayInherentTypeViolation() {
    utils:FooRecord[] fooRecordArray = [<utils:FooRecord>{ fooFieldOne: "test string 1" }];
    any[] anyArray = fooRecordArray;
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, fooRecordArray.length() - 1,
                                                           <utils:BarRecord> { barFieldOne: 1 }),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    map<utils:FooRecord>[] fooRecordMapArray = [
        {
            one: <utils:FooRecord>{ fooFieldOne: "test string 1" }
        },
        {
            two: <utils:FooRecord>{ fooFieldOne: "test string 2" },
            three: <utils:FooRecord>{ fooFieldOne: "test string 3" }
        }
    ];
    anyArray = fooRecordMapArray;

    // `fooRecordOrBarRecordMap` looks like `map<utils:FooRecord>`
    map<utils:FooRecord|utils:BarRecord> fooRecordOrBarRecordMap = {
        one: <utils:FooRecord>{ fooFieldOne: "test string 1" }
    };
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, fooRecordOrBarRecordMap),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

@test:Config {}
function testObjectArrayInherentTypeViolation() {
    utils:FooObject f1 = new("test string 1");
    utils:FooObject f2 = new("test string 2");
    utils:FooObject f3 = new("test string 3");
    utils:FooObject[] fooObjectArray = [f1, f2, f3];
    any[] anyArray = fooObjectArray;

    utils:BarObject b1 = new(1);
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, b1),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    map<utils:FooObject>[] fooObjectMapArray = [
        {
            one:f1,
            two: f2
        },
        {
            three: f3
        }
    ];
    anyArray = fooObjectMapArray;

    // `fooRecordOrBarObjectMap` looks like `map<utils:FooObject>`
    map<utils:FooObject|utils:BarObject> fooRecordOrBarObjectMap = {
        one: f1
    };
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, fooRecordOrBarObjectMap),
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}
