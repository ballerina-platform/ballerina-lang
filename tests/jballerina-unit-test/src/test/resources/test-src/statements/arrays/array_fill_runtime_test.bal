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

const constLength = 2;

type Rec record {
    int i?;
    int j = 10;
};

class Obj {
    int i;

    function init() {
        self.i = 1;
    }
}

class ObjInitNullable {
    int i;

    function init() returns () {
        self.i = 1;
    }
}

class Age {
    public int age;
    public function init(int age = 5) {
    	 self.age = age;
    }
}

// TODO: fix me https://github.com/ballerina-platform/ballerina-lang/issues/20983
function testObjectDynamicArrayFilling() {
    //Age[2] y = [];
    //y[1] = new(10);
    //
    //assertEqualPanic(5, y[0].age);
    //assertEqualPanic(10, y[1].age);
}

function testObjectRetNullableDynamicArrayFilling() {
    ObjInitNullable[2] y = [];
    y[1] = new();

    assertEqualPanic(1, y[0].i);
    assertEqualPanic(1, y[1].i);
}

function testObjectNoRetDynamicArrayFilling() {
    Obj[2] y = [];
    y[1] = new();

    assertEqualPanic(1, y[0].i);
    assertEqualPanic(1, y[1].i);
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

function testTwoDimensionalSealedArrayFill() {
    int[][2] x = [];
    x[1] = [1, 3];
    assertEqualPanic(2, x.length());
    assertEqualPanic([0, 0], x[0]);
    assertEqualPanic([1, 3], x[1]);

    int[2][3] y = [];
    assertEqualPanic(2, y.length());

    foreach var item in y {
        assertEqualPanic([0, 0, 0], item);
    }

    Rec[2][2] z = [];
    assertEqualPanic(z.length(), 2);

    foreach var item in z {
        assertEqualPanic(10, item[0].j);
        assertEqualPanic(10, item[1].j);
    }
}

const FOO_ZERO = 0;
type FOO_FOUR_THREE  4 | 3;
type BarMultiple FOO_ZERO | 1 | FOO_FOUR_THREE;
function createMultipleConstLiteralAutoFilledSealedArray() {
    BarMultiple a = 1;
    BarMultiple[5] sealedArray = [a, a];
    sealedArray[3] = a;
    assertEqualPanic(1, sealedArray[0]);
    assertEqualPanic(1, sealedArray[1]);
    // TODO : enable this after fixing - does not get filled
    //assertEqualPanic(0, sealedArray[2]);
    assertEqualPanic(1, sealedArray[3]);
}

function tesOneDimensionalArrayWithConstantSizeReferenceFill() {
    int[constLength] a = [];
    a[1] = 2;
    assertEqualPanic(2, a.length());
    assertEqualPanic(0, a[0]);
    assertEqualPanic(2, a[1]);
}

function tesTwoDimensionalArrayWithConstantSizeReferenceFill() {
    int[2][constLength] a = [];
    a[1][0] = 3;
    assertEqualPanic(2, a.length());
    assertEqualPanic([0, 0], a[0]);
    assertEqualPanic([3, 0], a[1]);

    int[constLength][] b = [];
    b[1] = [1,2,3];
    assertEqualPanic(2, b.length());
    assertEqualPanic([], b[0]);
    assertEqualPanic([1,2,3], b[1]);
}

public function main() {
    testObjectDynamicArrayFilling();
    testRecordTypeWithOptionalFieldsArrayFill();
    testRecordTypeWithOptionalFieldsSealedArrayFill();
    testObjectRetNullableDynamicArrayFilling();
    testObjectNoRetDynamicArrayFilling();
    testTwoDimensionalSealedArrayFill();
    createMultipleConstLiteralAutoFilledSealedArray();
    tesOneDimensionalArrayWithConstantSizeReferenceFill();
    tesTwoDimensionalArrayWithConstantSizeReferenceFill();
}

function assertEqualPanic(anydata expected, anydata actual, string message = "Value mismatch") {
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}
