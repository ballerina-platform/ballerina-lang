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

const EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE = "expected values to be at the same location";

// Mutation makes it possible for the graphs of references between values to have cycles.
@test:Config {}
function testCyclicReferenceViaMutationInArrays() {
    any[] a = [1, "test string 1", 3.0];
    a[3] = a;
    test:assertTrue(a === a[3], msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

@test:Config {}
function testCyclicReferenceViaMutationInTuples() {
    (int, anydata, float) b = (1, 2, 3.0);
    b[1] = b;
    test:assertTrue(b === b[1], msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

@test:Config {}
function testCyclicReferenceViaMutationInMaps() {
    map<any> c = { one: 1, two: 2.0 };
    c.three = c;
    test:assertTrue(c === c.three, msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

public type QuxRecord record {
    float quxFieldOne;
};

@test:Config {}
function testCyclicReferenceViaMutationInRecords() {
    QuxRecord d = { quxFieldOne: 1.2 };
    d.quxRecord = d;
    test:assertTrue(d === d.quxRecord, msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

public type BarObjectOne object {
    public int barOneFieldOne;

    public function __init(int barOneFieldOne) {
        self.barOneFieldOne = barOneFieldOne;
    }

    public function getbarOneFieldOne() returns int {
        return self.barOneFieldOne;
    }
};

public type BarObjectTwo object {
    public BarObjectOne barTwoFieldOne;
    public BarObjectOne? barTwoFieldTwo = ();
    public BarObjectTwo? barTwoFieldThree = ();

    public function __init(BarObjectOne barTwoFieldOne) {
        self.barTwoFieldOne = barTwoFieldOne;
    }

    public function getbarTwoFieldOne() returns BarObjectOne {
        return self.barTwoFieldOne;
    }
};

@test:Config {}
function testCyclicReferenceViaMutationInObjects() {
    BarObjectOne e = new(1);
    BarObjectTwo f = new(e);
    f.barTwoFieldThree = f;
    test:assertTrue(f === f.barTwoFieldThree, msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}
