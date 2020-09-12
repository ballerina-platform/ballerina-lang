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

class ObjInitWithParam {
    int i;

    function init(int i) {
        self.i = i;
    }
}

function testArrayFillWithObjWithInitParam() {
    ObjInitWithParam[] objWithParamsArray = [];
    ObjInitWithParam o = new(1);
    objWithParamsArray[0] = o;
    objWithParamsArray[3] = o; // this is invalid
}

// int based finite type.
type allIntNonZero 1|2|3;

function testArrayFillWithIntFiniteTypes() {
    allIntNonZero[] arr = [];
    arr[1] = 1;
}

// float based finite type.
type allFloatNonZero 1.0|3.143;

function testArrayFillWithFloatFiniteTypes() {
    allFloatNonZero[] arr = [];
    arr[1] = 1.0;
}

// string based finite type.
type allStrNonEmpty "a"|"b"|"c";

function testArrayFillWithStringFiniteTypes() {
    allStrNonEmpty[] strFiniteNonEmptyArr = [];
    strFiniteNonEmptyArr[1] = "a";
}

function testArrayFillWithTypedesc() {
    typedesc<any>[] typedescArr = [];
    typedescArr[1] = int;
}

class Student {
    public string name;
    public int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }
}

function testNonSequentialArrayInsertion() returns Student[] {
    Student s = new("Grainier", 28);
    Student[] arr = [];
    int i = 0;
    while (i < 10) {
        arr[i] = s;
        i += 2;
    }
    return arr;
}

function testIllegalArrayInsertion() returns (int|string|boolean)[] {
    (int|string|boolean)[] arr = [];
    arr[5] = 5;  // throws a runtime exception
    return arr;
}

function testIllegalTwoDimensionalArrayInsertion() returns (int|string|boolean)[][2] {
    (int|string|boolean)[][2] x = [];
    x[1] = [1, 3];
    return x;
}

type Rec record {
    int i;
};

function testRecordTypeWithRequiredFieldsArrayFill() {
    Rec[] x = [];
    x[1] = {i: 1};
}


const decimal ZERO = 1.5;
const int ONE_TWO = 1;
const decimal TWO_THREE = 2.3;

type DEC ZERO|ONE_TWO|TWO_THREE;
type LiteralsAndType 1|"abc"|int;

function testFiniteTypeArrayFill() returns DEC[] {
    DEC value = 1.5;
    DEC[] ar = [];
    ar[5] = value;
    return ar;
}

function testFiniteTypeArrayFill2() returns LiteralsAndType[] {
    LiteralsAndType value2 = 1;
    LiteralsAndType[] ar2 = [];
    ar2[5] = value2;
    return ar2;
}
