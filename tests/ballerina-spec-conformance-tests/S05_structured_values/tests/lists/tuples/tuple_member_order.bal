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

// A list value is a container that keeps its members in an ordered list.

@test:Config {}
function testBasicTypeTupleMemberOrder() {
    int a1 = 30;
    string a2 = "10";
    float a3 = 20.2;
    int a4 = 11;

    (int, string, float) tuple = (a1, a2, a3);

    test:assertEquals(tuple[0], a1, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(tuple[1], a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(tuple[2], a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");

    tuple[0] = a4;
    test:assertEquals(tuple[0], a4, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(tuple[1], a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(tuple[2], a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
}

@test:Config {}
function testRecordTupleMemberOrder() {
    FooRecordEight a5 = { fooFieldOne: "valueOne" };
    BarRecordEight a6 = { barFieldOne: 100 };
    FooRecordEight a7 = { fooFieldOne: "valueTwo" };

    (FooRecordEight, BarRecordEight) tuple2 = (a5, a6);

    test:assertEquals(tuple2[0], a5, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(tuple2[1], a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");

    tuple2[0] = a7;
    test:assertEquals(tuple2[0], a7, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
}

@test:Config {}
function testObjectTupleMemberOrder() {
    FooObjectEight a8 = new("valueOne");
    BarObjectEight a9 = new(200);
    FooObjectEight a10 = new("valueTwo");

    (FooObjectEight, BarObjectEight) tuple3 = (a8, a9);

    test:assertEquals(tuple3[0], a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(tuple3[1], a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");

    tuple3[0] = a10;
    test:assertEquals(tuple3[0], a10, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
}

public type FooRecordEight record {|
    string fooFieldOne;
|};

public type BarRecordEight record {|
    int barFieldOne;
|};

public type FooObjectEight object {
    public string fooFieldOne;
    
    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }
    
    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

public type BarObjectEight object {
    public int barFieldOne;
    
    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }
    
    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};
