// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/runtime;

function waitTest1() returns int {
    future<int> f1 = @strand{thread:"any"} start add(5, 2);
    int result = wait f1;
    return result;
}

function waitTest2() returns boolean {
    future<boolean> f6 = @strand{thread:"any"} start status();
    boolean result = wait f6;
    return result;
}

function waitTest3() returns any {
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    any result = wait f4;
    return result;
}

function waitTest4() returns any[] {
    future<int> f2 = @strand{thread:"any"} start add(10, 12);
    future<string> f5 = @strand{thread:"any"} start concat("bar");
    future<int|string> f7 =  @strand{thread:"any"} start concat("xyz");

    int|string result1 = wait f2;
    int|string result2 = wait f5;
    int|string result3 = wait f7;

    any[] arr = [];
    arr[0] = result1;
    arr[1] = result2;
    arr[2] = result3;
    return arr;
}

function waitTest5() returns map<string> {
    future<map<string>> f9 = @strand{thread:"any"} start getEmpMap();
    map<string> m = wait f9;
    return m;
}

function waitTest6() returns int|float {
    future<int|float> f8 = @strand{thread:"any"} start add(66, 33);
    int|float result = wait f8;
    return result;
}

function waitTest7() returns int {
    int result = wait fuInt();
    return result;
}

function waitTest8() returns int {
    future<int> f1 = @strand{thread:"any"} start add_panic(5, 2);
    int result = wait f1;
    return result;
}

function waitTest9() returns () {
    future<()> f1 = start runtime:sleep(2000);
    () result = wait f1;
    return result;
}

//function waitTest10() returns int { // Needs to be tested out
//    int result = 0;
//    worker w1 {
//        future<int> f1 = start add(5, 5);
//        result = wait f1;
//    }
//    result = wait w1;
//    return result;
//}

function add(int i, int j) returns int {
    int k = i + j;
    return k;
}

function concat(string name) returns string {
    return "hello " + name;
}

function status() returns boolean {
    return true;
}

function fuInt() returns future<int> {
    future <int> i = @strand{thread:"any"} start add(11, 55);
    return i;
}

function getEmpMap() returns map<string> {
    map<string> empMap = { fname: "foo", lname: "bar"};
    return empMap;
}

function add_panic(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 8888888) {
        l = l + 1;
    }
    if (true) {
        error err = error("err from panic" );
        panic err;
    }
    return k;
}

class Student {
    public string name;
    public function init() {
        future<int> accumulator = start add(2, 4);
        self.name = "ABC";
    }
}

function asyncObjectCreationTest() {
    Student s = new();
    Student[] arr = [];
    arr[0] = s;
    arr[2] = s;
}
