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

public type FooRecord record {
    string fooFieldOne;
    !...;
};

public type BarRecord record {
    int barFieldOne;
    !...;
};

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

public type FooObject object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

public type BarObject object {
    public int barFieldOne;

    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }

    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};

public type BazObject object {
    public BarObject bazFieldOne;
    public BarObject? bazFieldTwo = ();
    public BazObject? bazFieldThree = ();

    public function __init(BarObject bazFieldOne) {
        self.bazFieldOne = bazFieldOne;
    }

    public function getBazFieldOne() returns BarObject {
        return self.bazFieldOne;
    }
};

# Util function expected to be used with the result of a trapped expression. 
# Validates that `result` is of type `error` and that the error has the reason specified as `expectedReason`,
# and fails with the `invalidReasonFailureMessage` string if the reasons mismatch.
# 
# + result - the result of the trapped expression
# + expectedReason - the reason the error is expected to have
# + invalidReasonFailureMessage - the failure message on reason mismatch
public function assertErrorReason(any|error result, string expectedReason, string invalidReasonFailureMessage) {
    if (result is error) {
        test:assertEquals(result.reason(), expectedReason, msg = invalidReasonFailureMessage);
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}

# Util function to assert the occurrence of a panic, and the error reason.
# Validates that a panic occurs and that the error has the reason specified as `expectedReason`,
# fails with the `invalidReasonFailureMessage` string if the reasons mismatch.
#
# + func - the function to call
# + expectedReason - the reason the error is expected to have
# + invalidReasonFailureMessage - the failure message on reason mismatch
public function assertPanic(function() returns any func, string expectedReason, string invalidReasonFailureMessage) {
    assertErrorReason(trap func.call(), expectedReason, invalidReasonFailureMessage);
}

# Util function to add a member to an array at a specified index.
# 
# + array - the array to which the member should be added
# + index - the index at which the member should be added
# + member - the member to be added
public function insertMemberToArray(any[] array, int index, any member) {
    array[index] = member;
}

# Util function to add a member to a tuple at the 0th index.
# 
# + tuple - the tuple to which the member should be added
# + member - the member to be added
public function insertMemberToTuple((any, any) tuple, any member) {
    tuple[0] = member;
}

# Util function to add a member to a map with a specified key.
# 
# + mapVal - the map to which the member should be added
# + index - the key with which the member should be added
# + member - the member to be added
public function insertMemberToMap(map<any|error> mapVal, string index, any|error member) {
    mapVal[index] = member;
}

# Util function to update a `FooRecord`'s `fooFieldOne` field.
# 
# + fooRecord - the `FooRecord` to update
# + newFooFieldOne - the new value for `fooFieldOne`
public function updateFooRecord(FooRecord fooRecord, any newFooFieldOne) {
    if (newFooFieldOne is string) {
        fooRecord.fooFieldOne = newFooFieldOne;
    } else {
        panic error("expected a `string` field for `fooFieldOne`");
    }
}

# Util function to update a `BazRecordThree`'s `bazFieldTwo` field.
# 
# + bazRecordThree - the `BazRecordThree` to update
# + newBazFieldTwo - the new value for `bazRecordTwo`
public function updateBazRecordThree(BazRecordThree bazRecordThree, any newBazFieldTwo) {
    if (newBazFieldTwo is BazRecord) {
        bazRecordThree.bazFieldTwo = newBazFieldTwo;
    } else {
        panic error("expected a `BazRecord` field for `bazFieldTwo`");
    }
}
