// Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

int num = 0;
string str = "a";

class ArrayTest {

    int num = 0;

    function getNum() returns int {
        self.num += 1;
        return 2;
    }

    function testArrayInitWithSelfVars() {
        int[] arr = [0, 1, self.num, self.getNum(), self.num, num, getNum(), num];
        test:assertEquals(arr, [0, 1, 0, 2, 1, 1, 2, 2], "Array values are not equal");
    }
}

function testArrayInitWithGlobalVars() {
    int[] arr = [1, num, 3, getNum(), num];
    test:assertEquals(arr, [1, 0, 3, 1, 1], "Array values are not equal");

    string[] arr2 = ["q", str, "w", getStr(), "e", str];
    test:assertEquals(arr2, ["q", "ab", "w", "ab", "e", "ab"], "Array values are not equal");

    ArrayTest arrayTest = new ArrayTest();
    arrayTest.testArrayInitWithSelfVars();
}

function getNum() returns int {
    num = num + 1;
    return num;
}

function getStr() returns string {
    str = str + "b";
    return str;
}

function arrayInitTest() returns (int) {
    int[] arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
    int size = 11;
    int index = 0;
    int sum = 0;

    arr[5] = 50;

    while (index < size) {
        sum = sum + arr[index];
        index = index + 1;
    }


    return sum;
}

function arrayReturnTest() returns (string[]) {
    string[] animals;

    animals = ["Lion", "Cat", "Leopard", "Dog", "Tiger", "Croc"];

    return animals;
}

function testNestedArrayInit() returns (int[][]) {
    int[][] array = [[1,2,3], [6,7,8,9]];

    return array;
}

function testArrayOfMapsInit() returns (map<any>[]) {
    map<any> addressOne = {city:"Colombo", "country":"SriLanka"};
    map<any> addressTwo = {city:"Kandy", "country":"SriLanka"};
    map<any> addressThree = {city:"Galle", "country":"SriLanka"};
    map<any>[] array = [
                     {address: addressOne},
                     {address: addressTwo},
                     {address: addressThree}
                  ];
    return array;
}

function floatArrayInitWithInt() returns (float[]) {
    float[] abc = [2, 4, 5];
    return abc;
}

public type FiniteType "Terminating"|"NotTerminating"|"BestEffort"|"NotBestEffort";

function finiteTypeArray() returns FiniteType {
    FiniteType?[]? var1 = ["Terminating"];
    if var1 is FiniteType?[] {
        var temp = var1[0];
        if (temp is FiniteType) {
            return temp;
        }
    }
    error e = error ("FAILED TEST");
    panic e;
}

function testInferredArrayInitWithInGrpExpr() {
    anydata[*] arr = (([1, 2, "a"]));
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 1);
    assertEquality(arr[1], 2);
    assertEquality(arr[2], "a");
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("AssertionError", message = "expected '" + expected.toString() +
        "', found '" + actual.toString() + "'");
}
