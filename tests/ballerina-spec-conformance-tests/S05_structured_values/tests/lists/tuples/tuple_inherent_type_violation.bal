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

const INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE =
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
    FooRecordSeven a1 = { fooFieldOne: "valueOne" };
    FooRecordSeven a2 = { fooFieldOne: "valueTwo" };
    (FooRecordSeven, FooRecordSeven) tuple2 = (a1, a2);
    (any, any) tupleWithAnyTypedMembers = tuple2;
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = "not a FooRecord"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);

    (map<FooRecordSeven>, map<FooRecordSeven>) fooRecordMapTuple = (
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
    map<FooRecordSeven|BarRecordSeven> FooRecordOrBarRecordMap = {
        one: { fooFieldOne: "valueOne" },
        two: { fooFieldOne: "valueTwo" }
    };
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = FooRecordOrBarRecordMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}

@test:Config {}
function testObjectTupleInherentTypeViolation() {
    FooObjectSeven a3 = new("valueOne");
    FooObjectSeven a4 = new("valueTwo");
    (FooObjectSeven, FooObjectSeven) tuple3 = (a3, a4);
    (any, any) tupleWithAnyTypedMembers = tuple3;
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = "not a FooRecord"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);

    (map<FooObjectSeven>, map<FooObjectSeven>) fooObjectMapTuple = (
        {
            one: new("valueOne"),
            two: new("valueTwo")
        },
        {
            three: new("valueThree")
        }
    );
    tupleWithAnyTypedMembers = fooObjectMapTuple;

    FooObjectSeven f1 = new("valueOne");
    FooObjectSeven f2 = new("valueTwo");

    // `map<FooObjectSeven|BarObjectSeven>` looks like `map<FooObjectSeven>`
    map<FooObjectSeven|BarObjectSeven> FooObjectOrBarObjectMap = {
        one: f1,
        two: f2
    };
    utils:assertPanic(function() { tupleWithAnyTypedMembers[0] = FooObjectOrBarObjectMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}

public type FooRecordSeven record {|
    string fooFieldOne;
|};

public type BarRecordSeven record {|
    int barFieldOne;
|};

public type FooObjectSeven object {
    public string fooFieldOne;
    
    public function __init(string fooFieldOne) {
    self.fooFieldOne = fooFieldOne;
    }
    
    public function getFooFieldOne() returns string {
    return self.fooFieldOne;
    }
};

public type BarObjectSeven object {
    public int barFieldOne;
    
    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }
    
    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};
