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

// Values can be stored in variables or as members of structures.
// A simple value is stored directly in the variable or structure.
const decimal D = 1.23;
const I = 1;
const S = "test string const";
const F = 1.0;
const B = true;

@test:Config {}
function testSimpleValuesStoredInArrays() {
    decimal d = D;
    decimal[] s1 = [1.2, 1.3, d, 1.4];
    d = D + 10;
    test:assertEquals(s1[2], D, msg = "expected array member to not have changed");
}

@test:Config {}
function testSimpleValuesStoredInTuples() {
    boolean b = B;
    (int, boolean) s2 = (12, b);
    b = false;
    test:assertEquals(s2[1], B, msg = "expected tuple member to not have changed");
}

@test:Config {}
function testSimpleValuesStoredInMaps() {
    float f = F;
    map<float> s3 = { one: f, two: 2.00 };
    f = 3.0;
    test:assertEquals(s3.one, F, msg = "expected map member to not have changed");
}

public type FooRecord record {|
    string fooFieldOne;
|};

@test:Config {}
function testSimpleValuesStoredInRecords() {
    string s = S;
    FooRecord s4 = { fooFieldOne: s };
    s = "test string 1";
    test:assertEquals(s4.fooFieldOne, S, msg = "expected record member to not have changed");
}

public type BarObject object {
    public int barFieldOne;

    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }

    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};

@test:Config {}
function testSimpleValuesStoredInObjects() {
    int i = I;
    BarObject s5 = new(i);
    i = 25;
    test:assertEquals(s5.barFieldOne, I, msg = "expected object member to not have changed");
}
