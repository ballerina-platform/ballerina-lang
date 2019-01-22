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

const string B7A_INVALID_UPDATE_REASON = "{ballerina}InvalidUpdate";
const string IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE = "invalid reason on immutable value update";

// Values of the container basic types are containers for other values, which are called their
// members. Containers are mutable: the members contained in a particular container can be
// changed. However, a container value can also be frozen at runtime or compile time, which
// prevents any change to its members. A frozen container value can refer only to immutable
// values: either other frozen values or values of basic types that are always immutable. Once
// frozen, a container value remains frozen.
@test:Config {}
function testFreezeOnContainer() {
    int[] a1 = [1, 2, 3];
    _ = a1.freeze();
    utils:assertErrorReason(trap utils:insertMemberToArray(a1, 0, 1), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    (int, boolean) a2 = (1, false);
    _ = a2.freeze();
    utils:assertErrorReason(trap utils:insertMemberToTuple(a2, 2), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    map<string|int|utils:FooObject> a3 = { one: 1, two: "two" };
    var result = a3.freeze();
    if (result is map<string|int|utils:FooObject>) {
        utils:assertErrorReason(trap utils:insertMemberToMap(result, "two", 2), B7A_INVALID_UPDATE_REASON, 
                                IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
    }

    utils:FooRecord a4 = { fooFieldOne: "test string 1" };
    _ = a4.freeze();
    utils:assertErrorReason(trap utils:updateFooRecord(a4, "test string 2"), B7A_INVALID_UPDATE_REASON, 
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);

    table<utils:BarRecord> a5 = table{};
    utils:BarRecord b1 = { barFieldOne: 100 };
    _ = a5.freeze();
    utils:assertErrorReason(trap a5.add(b1), B7A_INVALID_UPDATE_REASON,
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

// A frozen container value can refer only to immutable values:
// either other frozen values or values of basic types that are always immutable.
// Once frozen, a container value remains frozen.
@test:Config {}
function testFrozenStructureMembersFrozenness() {
    int[] a1 = [1, 2, 3];
    int[] a2 = [11, 12, 13];
    int[][] a3 = [a1, [5, 6], a2];
    test:assertFalse(a1.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a2.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a3.isFrozen(), msg = "exected value to not be frozen");
    _ = a3.freeze();
    test:assertTrue(a1.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a2.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a3.isFrozen(), msg = "exected value to be frozen");

    (int, boolean) a4 = (1, false);
    (float, string, boolean) a5 = (100.0, "test string 1", false);
    (anydata, int, (int, boolean)) a6 = (a5, 1, a4);
    test:assertFalse(a4.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a5.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a6.isFrozen(), msg = "exected value to not be frozen");
    _ = a6.freeze();    
    test:assertTrue(a4.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a5.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a6.isFrozen(), msg = "exected value to be frozen");

    map<string|int|utils:FooObject> a7 = { one: 1, two: "two" };
    map<int|boolean> a8 = { three: 12, four: true, five: false };
    map<any> a9 = { intVal: 4, mapValOne: a7, mapValTwo: a8 };
    test:assertFalse(a7.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a8.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a9.isFrozen(), msg = "exected value to not be frozen");
    var result = a9.freeze();
    if (result is map<string|int|utils:FooObject>) {
        test:assertTrue(a7.isFrozen(), msg = "exected value to be frozen");
        test:assertTrue(a8.isFrozen(), msg = "exected value to be frozen");        
    }

    utils:FooRecord a10 = { fooFieldOne: "test string 1" }; 
    utils:BarRecord a11 = { barFieldOne: 1 }; 
    utils:BazRecord a12 = { bazFieldOne: 1.0, fooRecord: a10, bazRecord: a11 };
    test:assertFalse(a10.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a11.isFrozen(), msg = "exected value to not be frozen");
    test:assertFalse(a12.isFrozen(), msg = "exected value to not be frozen");
    _ = a12.freeze();    
    test:assertTrue(a10.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a11.isFrozen(), msg = "exected value to be frozen");
    test:assertTrue(a12.isFrozen(), msg = "exected value to be frozen");
}

// Values of basic type xml can also be frozen.
@test:Config {}
function testFrozenXml() {
    // TODO
}

// The shape of the members of a container value contribute to the shape of the container.
// Mutating a member of a container can thus cause the shape of the container to change.
@test:Config {}
function testShapeOfContainters() {
    (int|string)[] a1 = [1, 2];
    var a2 = int[].convert(a1);
    test:assertTrue(a2 is int[], 
                    msg = "expected convert to succeed since the value is of the expected shape");
    a1[2] = "test string 1";
    a2 = int[].convert(a1);
    test:assertFalse(a2 is int[], 
                    msg = "expected convert to fail since the value is not of the expected shape");

    (float, int|string, boolean) a3 = (1.1, 1, true);
    var a4 = (float, int, boolean).convert(a3);
    test:assertTrue(a4 is (float, int, boolean), 
                    msg = "expected convert to succeed since the value is of the expected shape");
    a3[1] = "test string 1";
    a4 = (float, int, boolean).convert(a3);
    test:assertFalse(a4 is (float, int, boolean), 
                    msg = "expected convert to fail since the value is not of the expected shape");

    map<int|boolean> a5 = { one: 1, two: 2, three: 3 };
    var a6 = map<int>.convert(a5);
    test:assertTrue(a6 is map<int>, 
                    msg = "expected convert to succeed since the value is of the expected shape");
    a5.four = true;
    a6 = map<int>.convert(a5);
    test:assertFalse(a6 is map<int>, 
                    msg = "expected convert to fail since the value is not of the expected shape");

    utils:BazRecordThree a7 = { bazFieldOne: 1.0 };
    var a8 = utils:BazRecord.convert(a7);
    test:assertTrue(a8 is utils:BazRecord, 
                    msg = "expected convert to succeed since the value is of the expected shape");
    a7.bazFieldOne = "1.0";
    a8 = utils:BazRecord.convert(a7);
    test:assertFalse(a8 is utils:BazRecord, 
                    msg = "expected convert to fail since the value is not of the expected shape");
}

// A type descriptor for a container basic type describe the shape of the container in terms of
// the shapes of its members. A container has an inherent type, which is a type descriptor
// which is part of the container’s runtime value. At runtime, the container prevents any
// mutation that might lead to the container having a shape that is not a member of its inherent
// type. Thus a container value belongs to a type if and only if that type is its inherent type or a
// subset of its inherent type.
@test:Config {}
function testContainerValueInherentType() {
    int intVal = 1;
    boolean booleanVal = true;
    float floatVal = 1.0;

    (int|string)[] a1 = [1, "2"];
    any anyVal = intVal;    
    var result = trap utils:insertMemberToArray(a1, a1.length() - 1, anyVal);
    test:assertTrue(anyVal is int|string, 
                    msg = "expected value's type to be of same type or sub type");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a value of same type or sub type");

    anyVal = booleanVal;
    result = trap utils:insertMemberToArray(a1, a1.length() - 1, anyVal);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value of type that is not same type or sub type");
    test:assertTrue(!(anyVal is int|string), 
                    msg = "expected value's type to not be of same type or sub type");

    (string|float, int) a2 = ("test string 1", 1);
    anyVal = floatVal;
    result = trap utils:insertMemberToTuple(a2, anyVal);
    test:assertTrue(anyVal is string|float, 
                    msg = "expected value's type to be of same type or sub type");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a value of same type or sub type");

    anyVal = booleanVal;
    result = trap utils:insertMemberToTuple(a2, anyVal);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value of type that is not same type or sub type");
    test:assertTrue(!(anyVal is string|float), 
                    msg = "expected value's type to not be of same type or sub type");

    map<int> a3 = { one: 1 };
    anyVal = intVal;
    result = trap utils:insertMemberToMap(a3, "two", anyVal);
    test:assertTrue(anyVal is int, 
                    msg = "expected value's type to be of same type or sub type");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a value of same type or sub type");

    anyVal = booleanVal;
    result = trap utils:insertMemberToMap(a3, "two", anyVal);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value of type that is not same type or sub type");
    test:assertTrue(!(anyVal is int), 
                    msg = "expected value's type to not be of same type or sub type");
    
    utils:FooRecord a4 = { fooFieldOne: "test string 1" };
    anyVal = "test string 2";
    result = trap utils:updateFooRecord(a4, anyVal);
    test:assertTrue(anyVal is string, 
                    msg = "expected value's type to be of same type or sub type");
    test:assertTrue(!(result is error), 
                    msg = "expected to be able to add a value of same type or sub type");

    anyVal = booleanVal;
    result = trap utils:updateFooRecord(a4, anyVal);
    test:assertTrue(result is error, 
                    msg = "expected to not be able to add a value of type that is not same type or sub type");
    test:assertTrue(!(anyVal is string), 
                    msg = "expected value's type to not be of same type or sub type");

    table<utils:BarRecord> a5 = table{};
    utils:BazRecord b1 = { bazFieldOne: 1.0 };
    result = a5.add(b1);
    test:assertTrue(result is error,
                    msg = "expected to not be able to add a value of type that is not same type or sub type");
}
