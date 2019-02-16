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
    _ = a5.add(b1);
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
    _ = a13.add(a14);
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

// A frozen container value belongs to a type if and only if the type contains the shape of the
// value. Thus after a container value is frozen, its inherent type does not provide additional
// information that cannot be derived from the value. In other words, freezing a container
// narrows its inherent type to a type that consists of just its current shape.
// TODO: Need to consider the shape of a frozen container to be its type
// TODO: Add tests for tables
// https://github.com/ballerina-platform/ballerina-lang/issues/13189
@test:Config {
    groups: ["deviation"]
}
function testFrozenContainerShapeAndTypeBroken() {
    int[][] a1 = [[1, 2], [1]];
    (int|string)?[] a2 = [11, 12];
    var result = trap insertMemberToArray(a1, a1.length() - 1, a2);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a2 is int[]), 
                    msg = "expected value's type to not be of same type or sub type");

    _ = a2.freeze();
    result = trap insertMemberToArray(a1, a1.length() - 1, a2);
    test:assertTrue(a2 is int[], 
                    msg = "expected value's type to match shape after freezing");
    // test:assertTrue(!(result is error),
    //                 msg = "expected to be able to add a frozen value that conforms to shape");
    test:assertTrue((result is error),
                    msg = "expected to not be able to add a frozen value that conforms to shape");

    ((int, string), int) a3 = ((1, "test string 1"), 2);
    (int|float, string) a4 = (2, "test string 2");
    result = trap insertMemberToTuple(a3, a4);
    // https://github.com/ballerina-platform/ballerina-lang/issues/13230
    // test:assertTrue(result is error,
    //                 msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(result is error),
                    msg = "expected to be able to add a value that violates shape");
    test:assertTrue(!(a4 is (int, string)),
                    msg = "expected value's type to not be of same type or sub type");

    _ = a4.freeze();
    result = trap insertMemberToTuple(a3, a4);
    test:assertTrue(a4 is (int, string),
                    msg = "expected value's type to match shape after freezing");
    test:assertTrue(!(result is error),
                    msg = "expected to be able to add a frozen value that conforms to shape");

    map<map<string>|float> a5 = { one: { a: "a", bc: "b c" }, two: 1.0 };
    map<string|boolean> a6 = { three: "3", four: "4" };
    any a7 = a6;
    result = trap insertMemberToMap(a5, "three", a7);
    test:assertTrue(result is error,
                    msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a7 is map<string>|float),
                    msg = "expected value's type to not be of same type or sub type");

    _ = a7.freeze();
    result = trap insertMemberToMap(a5, "three", a7);
    test:assertTrue(a7 is map<string>|float,
                    msg = "expected value's type to match shape after freezing");
    // test:assertTrue(!(result is error),
    //                 msg = "expected to be able to add a frozen value that conforms to shape");
    test:assertTrue((result is error),
                    msg = "expected to not be able to add a frozen value that conforms to shape");
}
