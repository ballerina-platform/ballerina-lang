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

// However, a container value can also be frozen at runtime or compile time, which
// prevents any change to its members - the test functions called should fail at compile time.
// TODO: Need to analyze updates of container values known to be frozen
// https://github.com/ballerina-platform/ballerina-lang/issues/13188
@test:Config {
    groups: ["deviation"]
}
function testFreezeOnContainerBroken() {
    utils:assertPanic(testFrozenArrayUpdateBroken, B7A_INVALID_UPDATE_REASON,
                      IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertPanic(testFrozenTupleUpdateBroken, B7A_INVALID_UPDATE_REASON,
                      IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertPanic(testFrozenMapUpdateBroken, B7A_INVALID_UPDATE_REASON,
                      IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertPanic(testFrozenRecordUpdateBroken, B7A_INVALID_UPDATE_REASON,
                      IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertPanic(testFrozenTableUpdateBroken, B7A_INVALID_UPDATE_REASON,
                      IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

function testFrozenArrayUpdateBroken() {
    int[] a1 = [1, 2, 3];
    _ = a1.freeze();
    a1[0] = 100;
}

function testFrozenTupleUpdateBroken() {
    (int, boolean) a2 = (1, false);
    _ = a2.freeze();
    a2[0] = 100;
}

function testFrozenMapUpdateBroken() {
    map<string|int|FooObjectThirteen> a3 = { one: 1, two: "two" };
    var result = a3.freeze();
    if (result is map<string|int|FooObjectThirteen>) {
        result.two = 2;
    }
}

function testFrozenRecordUpdateBroken() {
    FooRecordThirteen a4 = { fooFieldOne: "test string 1" };
    _ = a4.freeze();
    a4.fooFieldOne = "test string update";
}

function testFrozenTableUpdateBroken() {
    table<BarRecordThirteen> a5 = table{};
    BarRecordThirteen b1 = { barFieldOne: 100 };
    _ = a5.freeze();
    error? err = a5.add(b1);
    if err is error {
        panic err;
    }
}

// A frozen container value can refer only to immutable values:
// either other frozen values or values of basic types that are always immutable.
// Once frozen, a container value remains frozen.
// TODO: Fix frozen table member frozenness
@test:Config {
    groups: ["deviation"]
}
function testFrozenStructureMembersFrozennessBroken() {
    table<BarRecordThirteen> a13 = table{};
    test:assertFalse(a13.isFrozen(), msg = "exected value to not be frozen");
    BarRecordThirteen a14 = { barFieldOne: 100 };
    error? err = a13.add(a14);
    if err is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    _ = a13.freeze();
    test:assertTrue(a13.isFrozen(), msg = "exected value to be frozen");
    //test:assertTrue(a14.isFrozen(), msg = "expected value to be frozen");
    test:assertFalse(a14.isFrozen(), msg = "exected value to not be frozen");
}

// The shape of the members of a container value contribute to the shape of the container.
// Mutating a member of a container can thus cause the shape of the container to change.
// TODO: add test once stamp/convert is supported for tables
// https://github.com/ballerina-platform/ballerina-lang/issues/12264
@test:Config {
    groups: ["deviation"]
}
function testTableShapeOfContainters() {

}
