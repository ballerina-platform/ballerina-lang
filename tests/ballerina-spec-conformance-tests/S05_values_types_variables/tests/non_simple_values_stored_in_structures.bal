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

// However, for other types of value, what is stored in the variable or member is a
// reference to the value; the value itself has its own separate storage.
@test:Config {}
function testNonSimpleValuesStoredInArrays() {
    int[] s1 = [12, 13, 14, 15];
    int[][] s2 = [s1, [1, 2, 3]];
    s1[0] = I;
    test:assertEquals(s2[0][0], I, msg = "expected array member to have been updated");
}

public type BarRecord record {|
    string barFieldOne;
|};

@test:Config {}
function testNonSimpleValuesStoredInTuples() {
    BarRecord f1 = { barFieldOne: "test string 1" };
    (int, BarRecord) s3 = (1, f1);
    f1.barFieldOne = S;
    BarRecord f2 = s3[1];
    test:assertEquals(f2.barFieldOne, S, msg = "expected tuple member to have been updated");
}

public type FooObject object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

@test:Config {}
function testNonSimpleValuesStoredInMaps() {
    FooObject f3 = new("test string 3");
    FooObject f4 = new("test string 4");
    map<FooObject> s4 = { one: f3, two: f4 };
    f3.fooFieldOne = S;
    test:assertEquals(s4.one.fooFieldOne, S, msg = "expected map member to have been updated");
}

public type BazRecord record {
    float bazFieldOne;
};

@test:Config {}
function testNonSimpleValuesStoredInRecords() {
    BarRecord f5 = { barFieldOne: "test string 5" };
    BazRecord b1 = { bazFieldOne: 1.0, fooRecField: f5 };
    f5.barFieldOne = S;
    BarRecord f6 = <BarRecord>b1.fooRecField;
    test:assertEquals(f6.barFieldOne, S, msg = "expected record member to have been updated");
}
