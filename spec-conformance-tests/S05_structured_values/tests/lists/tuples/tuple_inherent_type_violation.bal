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

const string INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE = 
                                        "invalid reason on inherent type violating tuple insertion";

// The inherent type of a list value determines a type Ti for a member with index i.
// The runtime system will enforce a constraint that a value written to index i will
// belong to type Ti. Note that the constraint is not merely that the value looks
// like Ti.

@test:Config {}
function testBasicTypeTupleInherentTypeViolation() {
    (int, int) tuple = (1, 2);
    (any, any) tupleWithAnyTypedMembers = tuple;
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = "not an int"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);

    (map<string>, map<string>) stringMapTuple = (
        {
            one: "test string 1",
            two: "test string 2"
        },
        {
            three: "test string 3"
        }
    );
    tupleWithAnyTypedMembers = stringMapTuple;

    // `map<string|int>` looks like `map<string>`
    map<string|int> stringOrIntMap = {
        one: "test string 1",
        two: "test string 2"
    };
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = stringOrIntMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordTupleInherentTypeViolation() {
    utils:FooRecord a1 = { fooFieldOne: "valueOne" };
    utils:FooRecord a2 = { fooFieldOne: "valueTwo" };
    (utils:FooRecord, utils:FooRecord) tuple2 = (a1, a2);
    (any, any) tupleWithAnyTypedMembers = tuple2;
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = "not a FooRecord"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);

    (map<utils:FooRecord>, map<utils:FooRecord>) fooRecordMapTuple = (
    {
        one: { fooFieldOne: "valueOne" },
        two: { fooFieldOne: "valueTwo" }
    },
    {
        three: { fooFieldOne: "valueThree" }
    }
    );
    tupleWithAnyTypedMembers = fooRecordMapTuple;

    // `map<FooRecord|BarRecord>` looks like `map<FooRecord>`
    map<utils:FooRecord|utils:BarRecord> FooRecordOrBarRecordMap = {
        one: { fooFieldOne: "valueOne" },
        two: { fooFieldOne: "valueTwo" }
    };
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = FooRecordOrBarRecordMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}

@test:Config {}
function testObjectTupleInherentTypeViolation() {
    utils:FooObject a3 = new("valueOne");
    utils:FooObject a4 = new("valueTwo");
    (utils:FooObject, utils:FooObject) tuple3 = (a3, a4);
    (any, any) tupleWithAnyTypedMembers = tuple3;
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = "not a FooRecord"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);

    (map<utils:FooObject>, map<utils:FooObject>) fooObjectMapTuple = (
        {
            one: new("valueOne"),
            two: new("valueTwo")
        },
        {
            three: new("valueThree")
        }
    );
    tupleWithAnyTypedMembers = fooObjectMapTuple;

    utils:FooObject f1 = new("valueOne");
    utils:FooObject f2 = new("valueTwo");

    // `map<utils:FooObject|utils:BarObject>` looks like `map<utils:FooObject>`
    map<utils:FooObject|utils:BarObject> FooObjectOrBarObjectMap = {
        one: f1,
        two: f2
    };
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = FooObjectOrBarObjectMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}
