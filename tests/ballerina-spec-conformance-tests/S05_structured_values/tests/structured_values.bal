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

const B7A_INVALID_UPDATE_REASON = "{ballerina}InvalidUpdate";
const IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE = "invalid reason on immutable value update";

const EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE = "exected value to not be frozen";
const EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE = "exected value to be frozen";

const EXPECTED_CONVERT_TO_SUCCEED_FAILURE_MESSAGE =
                                "expected convert to succeed since the value is of the expected shape";
const EXPECTED_CONVERT_TO_FAIL_FAILURE_MESSAGE =
                                "expected convert to fail since the value is not of the expected shape";

const EXPECTED_VALUE_TO_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE = "expected value's type to be of same type or sub type";
const EXPECTED_VALUE_TO_NOT_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE =
                                "expected value's type to not be of same type or sub type";

const EXPECTED_TO_BE_ABLE_TO_ADD_VALUE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE =
                                "expected to be able to add a value of same type or sub type";
const EXPECTED_TO_NOT_BE_ABLE_TO_ADD_VALUE_NOT_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE =
                                "expected to not be able to add a value of type that is not same type or sub type";

// Values of the container basic types are containers for other values, which are called their
// members. Containers are mutable: the members contained in a particular container can be
// changed. However, a container value can also be frozen at runtime or compile time, which
// prevents any change to its members. A frozen container value can refer only to immutable
// values: either other frozen values or values of basic types that are always immutable. Once
// frozen, a container value remains frozen.
@test:Config {}
function testArrayFreezeOnContainer() {
    int[] a1 = [1, 2, 3];
    _ = a1.freeze();
    utils:assertPanic(function () { insertMemberToArray(a1, 0, 1); }, B7A_INVALID_UPDATE_REASON,
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

# Util function to add a member to an array at a specified index.
#
# + array - the array to which the member should be added
# + index - the index at which the member should be added
# + member - the member to be added
function insertMemberToArray(any[] array, int index, any member) {
    array[index] = member;
}

@test:Config {}
function testTupleFreezeOnContainer() {
    (int, boolean) a2 = (1, false);
    _ = a2.freeze();
    utils:assertPanic(function () { insertMemberToTuple(a2, 2); }, B7A_INVALID_UPDATE_REASON,
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

# Util function to add a member to a tuple at the 0th index.
#
# + tuple - the tuple to which the member should be added
# + member - the member to be added
function insertMemberToTuple((any, any) tuple, any member) {
    tuple[0] = member;
}

@test:Config {}
function testMapFreezeOnContainer() {
    map<string|int|FooObjectThirteen> a3 = { one: 1, two: "two" };
    var result = a3.freeze();
    if (result is map<string|int|FooObjectThirteen>) {
        var trappedResult = trap insertMemberToMap(result, "two", 2);
        if (trappedResult is error) {
            test:assertEquals(trappedResult.reason(), B7A_INVALID_UPDATE_REASON,
                msg = IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
        } else {
            test:assertFail(msg = "expected expression to panic");
        }
    }
}

# Util function to add a member to a map with a specified key.
#
# + mapVal - the map to which the member should be added
# + index - the key with which the member should be added
# + member - the member to be added
function insertMemberToMap(map<any|error> mapVal, string index, any|error member) {
    mapVal[index] = member;
}

@test:Config {}
function testRecordFreezeOnContainer() {
    FooRecordThirteen a4 = { fooFieldOne: "test string 1" };
    _ = a4.freeze();
    utils:assertPanic(function () { updateFooRecord(a4, "test string 2"); }, B7A_INVALID_UPDATE_REASON,
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

# Util function to update a `FooRecord`'s `fooFieldOne` field.
#
# + fooRecord - the `FooRecord` to update
# + newFooFieldOne - the new value for `fooFieldOne`
function updateFooRecord(FooRecordThirteen fooRecord, string newFooFieldOne) {
    fooRecord.fooFieldOne = newFooFieldOne;
}

@test:Config {}
function testTableFreezeOnContainer() {
    table<BarRecordThirteen> a5 = table{};
    BarRecordThirteen b1 = { barFieldOne: 100 };
    _ = a5.freeze();
    utils:assertPanic(function () { insertMemberToTable(a5, b1); }, B7A_INVALID_UPDATE_REASON,
                            IMMUTABLE_VALUE_UPDATE_INVALID_REASON_MESSAGE);
}

# Util function to add a record to a table.
#
# + tableVal - the table to which the member should be added
# + member - the member to be added
public function insertMemberToTable(table<record{}> tableVal, record{} member) {
    _ = tableVal.add(member);
}

// A frozen container value can refer only to immutable values:
// either other frozen values or values of basic types that are always immutable.
// Once frozen, a container value remains frozen.
@test:Config {}
function testArrayFrozenStructureMembersFrozenness() {
    int[] a1 = [1, 2, 3];
    int[] a2 = [11, 12, 13];
    int[][] a3 = [a1, [5, 6], a2];
    test:assertFalse(a1.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a2.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a3.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    _ = a3.freeze();
    test:assertTrue(a1.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a2.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a3.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
}

@test:Config {}
function testTupleFrozenStructureMembersFrozenness() {
    (int, boolean) a4 = (1, false);
    (float, string, boolean) a5 = (100.0, "test string 1", false);
    (anydata, int, (int, boolean)) a6 = (a5, 1, a4);
    test:assertFalse(a4.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a5.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a6.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    _ = a6.freeze();
    test:assertTrue(a4.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a5.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a6.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
}

@test:Config {}
function testMapFrozenStructureMembersFrozenness() {
    map<string|int|FooObjectThirteen> a7 = { one: 1, two: "two" };
    map<int|boolean> a8 = { three: 12, four: true, five: false };
    map<any> a9 = { intVal: 4, mapValOne: a7, mapValTwo: a8 };
    test:assertFalse(a7.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a8.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a9.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    var result = a9.freeze();
    if (result is map<string|int|FooObjectThirteen>) {
        test:assertTrue(a7.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
        test:assertTrue(a8.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    }
}

@test:Config {}
function testRecordFrozenStructureMembersFrozenness() {
    FooRecordThirteen a10 = { fooFieldOne: "test string 1" };
    BarRecordThirteen a11 = { barFieldOne: 1 };
    BazRecord a12 = { bazFieldOne: 1.0, fooRecord: a10, bazRecord: a11 };
    test:assertFalse(a10.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a11.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    test:assertFalse(a12.isFrozen(), msg = EXPECTED_VALUE_TO_NOT_BE_FROZEN_FAILURE_MESSAGE);
    _ = a12.freeze();
    test:assertTrue(a10.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a11.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
    test:assertTrue(a12.isFrozen(), msg = EXPECTED_VALUE_TO_BE_FROZEN_FAILURE_MESSAGE);
}

// Values of basic type xml can also be frozen.
@test:Config {}
function testFrozenXml() {
    // TODO
}

// The shape of the members of a container value contribute to the shape of the container.
// Mutating a member of a container can thus cause the shape of the container to change.

@test:Config {}
function testArrayShapeOfContainters() {
    (int|string)?[] a1 = [1, 2];
    var a2 = int[].convert(a1);
    test:assertTrue(a2 is int[], msg = EXPECTED_CONVERT_TO_SUCCEED_FAILURE_MESSAGE);
    a1[2] = "test string 1";
    a2 = int[].convert(a1);
    test:assertFalse(a2 is int[], msg = EXPECTED_CONVERT_TO_FAIL_FAILURE_MESSAGE);
}

@test:Config {}
function testTupleShapeOfContainters() {
    (float, int|string, boolean) a3 = (1.1, 1, true);
    var a4 = (float, int, boolean).convert(a3);
    test:assertTrue(a4 is (float, int, boolean), msg = EXPECTED_CONVERT_TO_SUCCEED_FAILURE_MESSAGE);
    a3[1] = "test string 1";
    a4 = (float, int, boolean).convert(a3);
    test:assertFalse(a4 is (float, int, boolean), msg = EXPECTED_CONVERT_TO_FAIL_FAILURE_MESSAGE);
}

@test:Config {}
function testMapShapeOfContainters() {
    map<int|boolean> a5 = { one: 1, two: 2, three: 3 };
    var a6 = map<int>.convert(a5);
    test:assertTrue(a6 is map<int>, msg = EXPECTED_CONVERT_TO_SUCCEED_FAILURE_MESSAGE);
    a5.four = true;
    a6 = map<int>.convert(a5);
    test:assertFalse(a6 is map<int>, msg = EXPECTED_CONVERT_TO_FAIL_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordShapeOfContainters() {
    BazRecordThree a7 = { bazFieldOne: 1.0 };
    var a8 = BazRecord.convert(a7);
    test:assertTrue(a8 is BazRecord, msg = EXPECTED_CONVERT_TO_SUCCEED_FAILURE_MESSAGE);
    a7.bazFieldOne = "1.0";
    a8 = BazRecord.convert(a7);
    test:assertFalse(a8 is BazRecord, msg = EXPECTED_CONVERT_TO_FAIL_FAILURE_MESSAGE);
}

// A type descriptor for a container basic type describe the shape of the container in terms of
// the shapes of its members. A container has an inherent type, which is a type descriptor
// which is part of the container’s runtime value. At runtime, the container prevents any
// mutation that might lead to the container having a shape that is not a member of its inherent
// type. Thus a container value belongs to a type if and only if that type is its inherent type or a
// subset of its inherent type.
int intVal = 1;
boolean booleanVal = true;
float floatVal = 1.0;

@test:Config {}
function testArrayContainerValueInherentType() {
    (int|string)[2] a1 = [1, "2"];
    any anyVal = intVal;
    var result = trap insertMemberToArray(a1, a1.length() - 1, anyVal);
    test:assertTrue(anyVal is int|string, msg = EXPECTED_VALUE_TO_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(result is error), msg = EXPECTED_TO_BE_ABLE_TO_ADD_VALUE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);

    anyVal = booleanVal;
    result = trap insertMemberToArray(a1, a1.length() - 1, anyVal);
    test:assertTrue(result is error,
                    msg = EXPECTED_TO_NOT_BE_ABLE_TO_ADD_VALUE_NOT_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(anyVal is int|string), msg = EXPECTED_VALUE_TO_NOT_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);

}

@test:Config {}
function testTupleContainerValueInherentType() {
    (string|float, int) a2 = ("test string 1", 1);
    any anyVal = floatVal;
    var result = trap insertMemberToTuple(a2, anyVal);
    test:assertTrue(anyVal is string|float, msg = EXPECTED_VALUE_TO_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(result is error), msg = EXPECTED_TO_BE_ABLE_TO_ADD_VALUE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);

    anyVal = booleanVal;
    result = trap insertMemberToTuple(a2, anyVal);
    test:assertTrue(result is error,
                    msg = EXPECTED_TO_NOT_BE_ABLE_TO_ADD_VALUE_NOT_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(anyVal is string|float), msg = EXPECTED_VALUE_TO_NOT_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
}

@test:Config {}
function testMapContainerValueInherentType() {
    map<int> a3 = { one: 1 };
    any anyVal = intVal;
    var result = trap insertMemberToMap(a3, "two", anyVal);
    test:assertTrue(anyVal is int, msg = EXPECTED_VALUE_TO_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(result is error), msg = EXPECTED_TO_BE_ABLE_TO_ADD_VALUE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);

    anyVal = booleanVal;
    result = trap insertMemberToMap(a3, "two", anyVal);
    test:assertTrue(result is error,
                    msg = EXPECTED_TO_NOT_BE_ABLE_TO_ADD_VALUE_NOT_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(anyVal is int), msg = EXPECTED_VALUE_TO_NOT_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordContainerValueInherentType() {
    FooRecordThirteen a4 = { fooFieldOne: "test string 1" };
    any anyVal = "test string 2";
    var result = trap updateFooRecord(a4, <string>anyVal);
    test:assertTrue(anyVal is string, msg = EXPECTED_VALUE_TO_BE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
    test:assertTrue(!(result is error), msg = EXPECTED_TO_BE_ABLE_TO_ADD_VALUE_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
}

@test:Config {}
function testTableContainerValueInherentType() {
    table<BarRecordThirteen> a5 = table{};
    BazRecord b1 = { bazFieldOne: 1.0 };
    var result = a5.add(b1);
    test:assertTrue(result is error,
                    msg = EXPECTED_TO_NOT_BE_ABLE_TO_ADD_VALUE_NOT_OF_SAME_OR_SUB_TYPE_FAILURE_MESSAGE);
}

// A frozen container value belongs to a type if and only if the type contains the shape of the
// value. Thus after a container value is frozen, its inherent type does not provide additional
// information that cannot be derived from the value. In other words, freezing a container
// narrows its inherent type to a type that consists of just its current shape.
@test:Config {}
function testRecordFrozenContainerShapeAndType() {
    BazRecordThree a8 = { bazFieldOne: "test string 1" };
    BazRecordFour a9 = { bazFieldOne: 1.0, bazFieldTwo: "test string 2" };
    anydata a10 = a9;
    var result = trap updateRecordBazField(a8, a10);
    test:assertTrue(result is error, msg = "expected to not be able to add a value that violates shape");
    test:assertTrue(!(a10 is BazRecord), msg = "expected value's type to not be of same type or sub type");

    _ = a10.freeze();
    result = trap updateRecordBazField(a8, a10);
    test:assertTrue(a10 is BazRecord, msg = "expected value's type to match shape after freezing");
    test:assertTrue(!(result is error), msg = "expected to be able to add a frozen value that conforms to shape");
}

public type BazRecord record {
    float bazFieldOne;
};

public type BazRecordTwo record {
    float bazFieldOne;
    string bazFieldTwo;
};

public type BazRecordThree record {
    string|float bazFieldOne;
    BazRecord bazFieldTwo?;
};

public type BazRecordFour record {
    float|string bazFieldOne;
    string bazFieldTwo;
};

# Util function to update a record's `bazFieldTwo` field.
#
# + rec - the record to update
# + value - the new value for `bazFieldTwo`
function updateRecordBazField(record{} rec, anydata value) {
    rec.bazFieldTwo = value;
}

public type FooRecordThirteen record {
    string fooFieldOne;
    !...;
};

public type BarRecordThirteen record {
    int barFieldOne;
    !...;
};

public type FooObjectThirteen object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

public type BarObjectThirteen object {
    public int barFieldOne;

    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }

    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};
