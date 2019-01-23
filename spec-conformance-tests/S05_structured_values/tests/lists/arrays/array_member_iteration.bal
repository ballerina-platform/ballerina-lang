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

const string INCORRECT_MEMBER_VALUE_ON_ITERATION_FAILURE_MESSAGE = "incorrect member value found on iteration";

// A list is iterable as a sequence of its members.
// An array T[] is iterable as a sequence of values of type T.

@test:Config {}
function testBasicTypeArrayMemberIteration() {
    int a = 4;
    string b = "string 1";
    string c = "string 2";
    int d = 1;

    (int|string)[] array = [a, b, c, d];
    (int|string)[] arrayTwo = [a, b, c, d];
    int currentIndex = 0;

    foreach string|int value in array {
        test:assertEquals(value, arrayTwo[currentIndex],
                          msg = INCORRECT_MEMBER_VALUE_ON_ITERATION_FAILURE_MESSAGE);
        currentIndex = currentIndex + 1;
    }
}

@test:Config {}
function testRecordArrayMemberIteration() {
    int currentIndex = 0;
    utils:FooRecord e = { fooFieldOne: "test string 1" };
    utils:BarRecord f = { barFieldOne: 1 };
    (utils:FooRecord|utils:BarRecord)[] arrayThree = [e, f];
    (utils:FooRecord|utils:BarRecord)[] arrayFour = [e, f];

    foreach utils:FooRecord|utils:BarRecord value in arrayThree {
        test:assertEquals(value, arrayFour[currentIndex],
                          msg = INCORRECT_MEMBER_VALUE_ON_ITERATION_FAILURE_MESSAGE);
        currentIndex = currentIndex + 1;
    }
}

@test:Config {}
function testObjectArrayMemberIteration() {
    int currentIndex = 0;
    utils:FooObject g = new("test string 1");
    utils:BarObject h = new(1);
    utils:BarObject i = new(1);
    (utils:FooObject|utils:BarObject)[] arrayFive = [g, h, i];
    (utils:FooObject|utils:BarObject)[] arraySix = [g, h, i];

    foreach utils:FooObject|utils:BarObject value in arrayFive {
        test:assertEquals(value, arraySix[currentIndex],
                          msg = INCORRECT_MEMBER_VALUE_ON_ITERATION_FAILURE_MESSAGE);
        currentIndex = currentIndex + 1;
    }
}
