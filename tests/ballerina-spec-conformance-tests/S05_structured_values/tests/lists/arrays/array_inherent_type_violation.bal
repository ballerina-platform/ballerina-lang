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

const INHERENT_TYPE_VIOLATION_REASON = "{ballerina}InherentTypeViolation";
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
    utils:assertPanic(function() { anyArray[intArray.length() - 1] = "not an int"; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    string[][] string2DArray = [["test string 1", "test string 2"], ["test string 3"]];
    anyArray = string2DArray;

    // `stringOrIntArray` looks like `string[]`
    (string|int)[2] stringOrIntArray = ["test string 1", "test string 2"];
    utils:assertPanic(function() { anyArray[0] = stringOrIntArray; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordArrayInherentTypeViolation() {
    FooRecordOne[] fooRecordArray = [<FooRecordOne>{ fooFieldOne: "test string 1" }];
    any[] anyArray = fooRecordArray;
    utils:assertPanic(function() { anyArray[fooRecordArray.length() - 1] = <BarRecordOne> { barFieldOne: 1 }; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    map<FooRecordOne>[] fooRecordMapArray = [
        {
            one: <FooRecordOne>{ fooFieldOne: "test string 1" }
        },
        {
            two: <FooRecordOne>{ fooFieldOne: "test string 2" },
            three: <FooRecordOne>{ fooFieldOne: "test string 3" }
        }
    ];
    anyArray = fooRecordMapArray;

    // `fooRecordOrBarRecordMap` looks like `map<FooRecordOne>`
    map<FooRecordOne|BarRecordOne> fooRecordOrBarRecordMap = {
        one: <FooRecordOne>{ fooFieldOne: "test string 1" }
    };
    utils:assertPanic(function() { anyArray[0] = fooRecordOrBarRecordMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

@test:Config {}
function testObjectArrayInherentTypeViolation() {
    FooObjectOne f1 = new("test string 1");
    FooObjectOne f2 = new("test string 2");
    FooObjectOne f3 = new("test string 3");
    FooObjectOne[3] fooObjectArray = [f1, f2, f3];
    any[] anyArray = fooObjectArray;

    BarObjectOne b1 = new(1);
    utils:assertPanic(function() { anyArray[0] = b1; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);

    map<FooObjectOne>[] fooObjectMapArray = [
        {
            one:f1,
            two: f2
        },
        {
            three: f3
        }
    ];
    anyArray = fooObjectMapArray;

    // `fooObjectOneOrBarObjectOneMap` looks like `map<FooObjectOne>`
    map<FooObjectOne|BarObjectOne> fooObjectOneOrBarObjectOneMap = {
        one: f1
    };
    utils:assertPanic(function() { anyArray[0] = fooObjectOneOrBarObjectOneMap; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

public type FooRecordOne record {|
    string fooFieldOne;
|};

public type BarRecordOne record {|
    int barFieldOne;
|};

public type FooObjectOne object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

public type BarObjectOne object {
    public int barFieldOne;

    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }

    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};
