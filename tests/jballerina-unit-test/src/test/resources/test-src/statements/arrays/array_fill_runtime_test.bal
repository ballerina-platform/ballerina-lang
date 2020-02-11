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

type Rec record {
    int i?;
    int j = 10;
};

type Obj object {
    int i;

    function __init() {
        self.i = 1;
    }
};

type Age object {
    public int age;
    public function __init(int age = 5) {
    	 self.age = age;
    }
};

// TODO: fix me
function testObjectDynamicArrayFilling() {
    //Age[2] y = [];
    //y[1] = new(10);
    //
    //assertEqualPanic(5, y[0].age);
    //assertEqualPanic(10, y[1].age);
}

function testRecordTypeWithOptionalFieldsArrayFill() {
    Rec[] x = [];
    x[1] = {i: 1, j: 2};

    assertEqualPanic(10, x[0].j);
    assertEqualPanic(2, x[1].j);
}

function testRecordTypeWithOptionalFieldsSealedArrayFill() {
    Rec[2] x = [];
    x[1] = {i: 1, j: 2};

    assertEqualPanic(10, x[0].j);
    assertEqualPanic(2, x[1].j);
}

public function main() {
    testObjectDynamicArrayFilling();
    testRecordTypeWithOptionalFieldsArrayFill();
    testRecordTypeWithOptionalFieldsSealedArrayFill();
}

function assertEqualPanic(anydata expected, anydata actual, string message = "Value mismatch") {
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}
