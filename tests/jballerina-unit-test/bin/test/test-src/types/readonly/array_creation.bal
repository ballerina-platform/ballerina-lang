// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

public function testIntArray() {
    int[] arr = createIntArray();
    arr[3] = 5;
}

public function testBooleanArray() {
    boolean[] arr = createBooleanArray();
    arr[3] = true;
}

public function testByteArray() {
    byte[] arr = createByteArray();
    arr[3] = 5;
}

public function testFloatArray() {
    float[] arr = createFloatArray();
    arr[3] = 5;
}

public function testStringArray() {
    string[] arr = createStringArray();
    arr[3] = "string";
}

function testReadOnlyMappingWithOptionalNeverFieldArray() {
    record { never i?; int j; } a = {j: 1};
    (any|error)[] b = createArrayOfMaps(a);

    assertTrue(b is (record { never i?; int j; } & readonly)[]);
    assertTrue(b is record { never i?; int j; }[] & readonly);
    assertEquality(0, b.length());
}

public function createIntArray() returns int[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

public function createBooleanArray() returns boolean[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

public function createByteArray() returns byte[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

public function createFloatArray() returns float[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

public function createStringArray() returns string[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

public function createArrayOfMaps(map<any|error> m) returns (any|error)[] = @java:Method {
    'class:"org.ballerinalang.test.types.readonly.ReadonlyArrayCreator"
} external;

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
