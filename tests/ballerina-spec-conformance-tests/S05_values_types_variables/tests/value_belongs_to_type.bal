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

const EXPECTED_VALUE_TO_BELONG_TO_TYPE_FAILURE_MESSAGE = "expected value to belong to type";
const EXPECTED_VALUE_NOT_TO_BELONG_TO_TYPE_FAILURE_MESSAGE = "expected value to not belong to type";

// a value belongs to a type if it looks like the type, and it will necessarily continue to
// look like the type no matter how the value is mutated
@test:Config {}
function testBelongsToWithArrays() {
    (int|boolean)[3] a1 = [true, false, false];
    any a2 = a1;
    // TODO: uncomment when issue #13606 is fixed.
    //if !(a2 is anydata[]) || !(a2 is (int|boolean)[]) {
    //    test:assertFail(msg = EXPECTED_VALUE_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    //}

    if (a2 is boolean[]) {
        test:assertFail(msg = EXPECTED_VALUE_NOT_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }
}

@test:Config {}
function testBelongsToWithTuples() {
    (int|boolean, string|float, decimal) a3 = (false, 1.0, 5.2d);
    anydata a4 = a3;
    if !(a4 is (anydata, anydata, decimal)) || !(a4 is (int|boolean, string|float, decimal)) {
        test:assertFail(msg = EXPECTED_VALUE_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }

    if (a4 is (boolean, float, decimal)) {
        test:assertFail(msg = EXPECTED_VALUE_NOT_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }
}

@test:Config {}
function testBelongsToWithMaps() {
    map<string|int> a5 = {
        zero: "map with strings only",
        one: "test string 1"
    };
    any a6 = a5;
    if !(a6 is map<anydata>) || !(a6 is map<string|int>) {
        test:assertFail(msg = EXPECTED_VALUE_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }

    if (a6 is map<string>) {
        test:assertFail(msg = EXPECTED_VALUE_NOT_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }
}

public type ARecord record {
    float fieldOne;
};

public type BRecord record {
    string|float fieldOne;
    ARecord aRecord?;
};

@test:Config {}
function testBelongsToWithRecords() {
    anydata a7 = <BRecord>{ fieldOne: 1.0 };
    if !(a7 is record {}) || !(a7 is BRecord) {
        test:assertFail(msg = EXPECTED_VALUE_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }

    if (a7 is ARecord) {
        test:assertFail(msg = EXPECTED_VALUE_NOT_TO_BELONG_TO_TYPE_FAILURE_MESSAGE);
    }
}
