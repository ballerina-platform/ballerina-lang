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

import ballerina/jballerina.java;

function waitTest1() returns int {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<int> f2 = @strand{thread:"any"} start add_2(10, 12);
    int result = checkpanic wait f1 | f2;
    return result;
}

function waitTest2() returns int {
    future<int> f3 = @strand{thread:"any"} start add_3(60, 22);
    future<int> f2 = @strand{thread:"any"} start add_2(11, 11);
    future<int> f1 = @strand{thread:"any"} start add_1(1, 1);
    int result = checkpanic wait f3 | f2 | f1;
    return result;
}

function waitTest3() returns string {
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    future<string> f5 = @strand{thread:"any"} start concat("bar");
    string result = checkpanic wait f4 | f5;
    return result;
}

function waitTest4() returns any {
    future<int> f1 = @strand{thread:"any"} start add_1(15, 15);
    future<string> f5 = @strand{thread:"any"} start concat("bar");
    future<boolean> f6 = @strand{thread:"any"} start status();
    any result = checkpanic wait f1 | f5 | f6;
    return result;
}

function waitTest5() returns map<string> {
    future<map<string>> f9 = @strand{thread:"any"} start getAddrMap();
    future<map<string>> f10 = @strand{thread:"any"} start getEmpMap();
    map<string> m = checkpanic wait f9 | f10;
    return m;
}

function waitTest6() returns string|int {
    future<int> f1 = @strand{thread:"any"} start add_1(88, 88);
    future<int> f3 = @strand{thread:"any"} start add_3(50, 100);
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    string|int result = checkpanic wait f1 | f3 | f4;
    return result;
}

function waitTest7() returns string|int|boolean {
    future<int> f1 = @strand{thread:"any"} start add_1(2, 2);
    future<int> f2 = @strand{thread:"any"} start add_2(10, 12);
    future<boolean> f6 = @strand{thread:"any"} start status();
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    string|int|boolean result = checkpanic wait f1 | f2 | f6 | f4;
    return result;
}

function waitTest8() returns int|string {
    future<int|string> f7 =  @strand{thread:"any"} start concat("xyz");
    int|string result = checkpanic wait fuInt() | f7;
    return result;
}

function waitTest9() returns int|string|float {
    future<int|string> f7 =  @strand{thread:"any"} start concat("xyz");
    future<int|float> f8 = @strand{thread:"any"} start add_3(66, 33);
    int|string|float result  = checkpanic wait f7 | f8;
    return result;
}

function waitTest10() returns int|string|float {
    future<int|float> f8 = @strand{thread:"any"} start add_3(66, 11);
    future<int|string> f7 =  @strand{thread:"any"} start concat("xyz");
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    future<string> f5 = @strand{thread:"any"} start concat("bar");
    int|string|float result  = checkpanic wait f8 | f7 | f4 |f5;
    return result;
}

function waitTest11() returns int|string|float {
    future<int> f1 = @strand{thread:"any"} start add_panic1(88, 88);
    future<int> f3 = @strand{thread:"any"} start add_panic2(50, 100);
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    string|int result = checkpanic wait f1 | f3 | f4;
    return result;
}

function waitTest12() returns int {
    future<int> f1 = @strand{thread:"any"} start add_panic1(88, 88);
    future<int> f3 = start add_panic2(50, 100);
    future<int> f4 = @strand{thread:"any"} start add_panic3(4, 3);
    int result = checkpanic wait f1 | f3 | f4;
    return result;
}

function waitTest13() returns int {
    future<int> f1 = @strand{thread:"any"} start add_panic1(88, 88);
    future<int> f3 = @strand{thread:"any"} start add_panic2(50, 100);
    var result = trap checkpanic wait f1 | f3;
    if (result is int) {
        return result;
    } else {
        return 0;
    }
}

function waitTest14() returns int {
    worker w1 returns int {
        future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
        future<int> f2 = @strand{thread:"any"} start add_3(50, 100);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    int result = wait w1;
    return result;
}

function waitTest15() returns int {
    worker w1 returns int {
        future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
        future<int> f2 = @strand{thread:"any"} start add_3(50, 100);
        int l = 0;
        while (l < 9999) {
            l = l + 1;
        }
        int r = checkpanic wait f1 | f2;
        return r;
    }
    worker w2 returns int {
        future<int> f1 = @strand{thread:"any"} start add_1(50, 10);
        future<int> f2 = @strand{thread:"any"} start add_3(200, 99);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    int result = wait w1 | w2;
    return result;
}

function waitTest16() returns int|string|boolean {
    worker w1 returns int {
        future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
        future<int> f2 = @strand{thread:"any"} start add_3(50, 100);
        sleep(3000);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    worker w2 returns int|string {
        future<int> f1 = @strand{thread:"any"} start add_1(50, 10);
        future<string> f2 = @strand{thread:"any"} start concat("foo");
        int|string r = checkpanic wait f1 | f2;
        return r;
    }
    worker w3 returns int|string|boolean {
        future<int> f1 = @strand{thread:"any"} start add_1(6, 6);
        future<string> f2 = @strand{thread:"any"} start concat("bar");
        future<boolean> f3 = @strand{thread:"any"} start status();
        int|string|boolean r = checkpanic wait f1 | f2 | f3;
        sleep(2000);
        return r;
    }
    int|string|boolean result = wait w1 | w2 | w3;
    return result;
}

function waitTest17() returns int|error {
    map<any> m = {};
    m["x"] = 25;
    fork {
        @strand{thread:"any"}
        worker w1 {
            int a = 10;
            m["x"] = a;
            sleep(1000);
        }
        @strand{thread:"any"}
        worker w2 {
            int a = 5;
            int b = 15;
            sleep(2000);
            m["x"] = a + b;
        }
        @strand{thread:"any"}
        worker w3 {
            int b = 30;
            m["x"] = b;
        }
    }
    () results = wait w1 | w2 | w3;
    return <int>m["x"];
}

function waitTest18() returns int {
    @strand{thread:"any"}
    worker w1 returns int{
        int a = 10;
        a -> w2;
        a = <- w2;
        return a;
    }
    @strand{thread:"any"}
    worker w2 returns int{
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;
        sleep(2000);
        return a;
    }
    int result = wait w1 | w2;
    result = result + 50;
    return result;
}

function waitTest19() {
    future<int|error> f1 = @strand{thread:"any"} start addOrError(5, 2);
    future<int|error> f2 = @strand{thread:"any"} start addOrError(10, 12);
    int|error result = wait f1 | f2;
    validateError(result, "err returned");
}

function waitTest20() returns int|string|error {
    future<int|error> f1 = @strand{thread:"any"} start addOrError(10, 12);
    future<string> f2 = @strand{thread:"any"} start concat("moo");
    int|string|error result = checkpanic wait f1 | f2;
    return result;
}

function waitTest21() returns error? {
    future<error> f1 = @strand{thread:"any"} start funcWithErr();
    future<()> f2 = @strand{thread:"any"} start funcWithPanic();
    error? result = checkpanic wait f1 | f2;
    return result;
}

function waitTest22() returns int|string|error {
    future<string> f3 = @strand{thread:"any"} start concat("foo");
    future<int|error> f1 = @strand{thread:"any"} start addOrError(10, 12);
    future<int> f2 = @strand{thread:"any"} start add_panic1(55, 88);
    int|string|error result = wait f3 | f2 | f1;
    return result;
}

function waitTest23() returns int|string|() {
    future<()> f1 = start sleep(50);
    future<int> f2 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f3 = @strand{thread:"any"} start greet();
    int|string|() result = checkpanic wait f1 | f2 | f3;
    return result;
}

function waitTest24() {
    future<int|error> f1 = @strand{thread:"any"} start fError();
    future<int|error> f2 = @strand{thread:"any"} start sError();
    int|error result = wait f1 | f2;
    validateError(result, "A hazardous error occurred!!! Abort immediately!!");
}

function add_panic1(int i, int j) returns int {
    int k = i + j;
    if (0 < 1) {
        error err = error("err from panic" );
        panic err;
    }
    return k;
}

function add_panic2(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    if (0 < 1) {
        error err = error("err from panic" );
        panic err;
    }
    return k;
}

function add_panic3(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 8888888) {
        l = l + 1;
    }
    if (0 < 1) {
        error err = error("err from panic" );
        panic err;
    }
    return k;
}

function add_1(int i, int j) returns int {
    int k = i + j;
    // sleep for 2s
    sleep(2000);
    int l = 0;
    while(l < 999999) {
        l = l + 1;
    }
    return k;
}

function add_2(int i, int j) returns int {
    int k = i + j;
    return k;
}

function add_3(int i, int j) returns int {
    int k = i + j;
    return k;
}

function concat(string name) returns string {
    return "hello " + name;
}

function greet() returns string {
    sleep(3000);
    return "good morning";
}

function status() returns boolean {
    return true;
}

function fuInt() returns future<int> {
    future <int> i = @strand{thread:"any"} start add_3(11, 17);
    return i;
}

function getEmpMap() returns map<string> {
    map<string> empMap = { fname: "foo", lname: "bar"};
    sleep(2000);
    return empMap;
}

function getAddrMap() returns map<string> {
    map<string> addrMap = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03"};
    return addrMap;
}

function addOrError(int i, int j) returns int|error {
    int k = i + j;
    if (0 < 1) {
        error err = error("err returned" );
        return err;
    }
    return k;
}

function fError() returns int|error {
    error err = error("first error returned" );
    return err;
}

function sError() returns error {
    error err = error("A hazardous error occurred!!! Abort immediately!!" );
    sleep(2000);
    return err;
}

function funcWithErr() returns error {
    error err = error("A hazardous error occurred!!!" );
    return err;
}

function funcWithPanic() {
    sleep(1500);
    if (true) {
        error err = error("A hazardous error occurred!!! Panic!!" );
        panic err;
    }
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function validateError(any|error value, string message) {
    if (value is error) {
        if (value.message() == message) {
            return;
        }
        panic error("Expected error message: " + message + ", found: " + value.message());
    }
    panic error("Expected error, found: " + (typeof value).toString());
}
