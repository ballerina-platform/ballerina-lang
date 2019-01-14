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
@test:Config {
    groups: ["broken"]
}
function testFreezeOnContainerBroken() {
    utils:assertErrorReason(trap testFrozenArrayUpdateBroken(), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertErrorReason(trap testFrozenTupleUpdateBroken(), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertErrorReason(trap testFrozenMapUpdateBroken(), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    utils:assertErrorReason(trap testFrozenRecordUpdateBroken(), B7A_INVALID_UPDATE_REASON, 
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
    map<string|int|utils:FooObject> a3 = { one: 1, two: "two" };
    var result = a3.freeze();
    if (result is map<string|int|utils:FooObject>) {
        result.two = 2;
    }
}

function testFrozenRecordUpdateBroken() {
    utils:FooRecord a4 = { fooFieldOne: "test string 1" };
    _ = a4.freeze();
    a4.fooFieldOne = "test string update";
}

// A frozen container value belongs to a type if and only if the type contains the shape of the
// value. Thus after a container value is frozen, its inherent type does not provide additional
// information that cannot be derived from the value. In other words, freezing a container
// narrows its inherent type to a type that consists of just its current shape.
// TODO: Need to consider the shape of a frozen container to be its type
@test:Config {
    groups: ["broken"]
}
function testFrozenContainerShapeAndTypeBroken() {
    int[][] a1 = [[1, 2], [1]];
    (int|string)[] a2 = [11, 12];
    var result = trap utils:insertMemberToArray(a1, a1.length() - 1, a2);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a2 is int[]), 
                    msg = "expected value's type to not be of same type or sub type");

    _ = a2.freeze();
    result = trap utils:insertMemberToArray(a1, a1.length() - 1, a2);
    test:assertTrue(a2 is int[], 
                    msg = "expected value's type to be match shape after freezing");
    // test:assertTrue(!(result is error), 
    //                 msg = "expected to be able to add a frozen value that conforms to shape");
    test:assertTrue((result is error), 
                    msg = "expected to not be able to add a frozen value that conforms to shape");

    ((int, string), int) a3 = ((1, "test string 1"), 2);
    (int|float, string) a4 = (2, "test string 2");
    result = trap utils:insertMemberToTuple(a3, a4);
    // test:assertTrue(result is error, 
    //                 msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a value that violates shape");
    test:assertTrue(!(a4 is (int, string)), 
                    msg = "expected value's type to not be of same type or sub type");

    _ = a4.freeze();
    result = trap utils:insertMemberToTuple(a3, a4);
    test:assertTrue(a4 is (int, string), 
                    msg = "expected value's type to be match shape after freezing");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a frozen value that conforms to shape");

    map<map<string>|float> a5 = { one: { a: "a", bc: "b c" }, two: 1.0 };
    map<string|boolean> a6 = { three: "3", four: "4" };
    any a7 = a6;
    result = trap utils:insertMemberToMap(a5, "three", a7);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a7 is map<string>|float), 
                    msg = "expected value's type to not be of same type or sub type");

    _ = a7.freeze();
    result = trap utils:insertMemberToMap(a5, "three", a7);
    test:assertTrue(a7 is map<string>|float, 
                    msg = "expected value's type to be match shape after freezing");
    // test:assertTrue(!(result is error), 
    //                 msg = "expected to be able to add a frozen value that conforms to shape");
    test:assertTrue((result is error), 
                    msg = "expected to not be able to add a frozen value that conforms to shape");

    utils:BazRecordThree a8 = { bazFieldOne: "test string 1" };
    utils:BazRecordTwo a9 = { bazFieldOne: 1.0, bazFieldTwo: "test string 2" };
    any a10 = a9;
    result = trap utils:updateBazRecordThree(a8, a10);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a10 is utils:BazRecord), 
                    msg = "expected value's type to not be of same type or sub type");

    _ = a10.freeze();
    result = trap utils:updateBazRecordThree(a8, a10);
    test:assertTrue(a10 is utils:BazRecord, 
                    msg = "expected value's type to be match shape after freezing");
    // test:assertTrue(!(result is error), 
    //                 msg = "expected to be able to add a frozen value that conforms to shape");
    test:assertTrue((result is error), 
                    msg = "expected to not be able to add a frozen value that conforms to shape");
}
