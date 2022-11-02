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

import ballerina/lang.runtime;
import ballerina/test;

function waitTest1() {
    future<int> f3 = @strand {thread: "any"} start add_3(60, 22);
    future<int> f2 = @strand {thread: "any"} start add_2(11, 11);
    future<int> f1 = @strand {thread: "any"} start add_1(1, 1);
    int result = checkpanic wait f3 | f2 | f1;
    assertIsOneOfTheAnydataValues(result, [82, 22, 2]);
}

function waitTest2() {
    future<string> f4 = @strand {thread: "any"} start concat("foo");
    future<string> f5 = @strand {thread: "any"} start concat("bar");
    string result = checkpanic wait f4 | f5;
    assertIsOneOfTheAnydataValues(result, ["hello foo", "hello bar"]);
}

function waitTest3() {
    future<map<string>> f9 = @strand {thread: "any"} start getAddrMap();
    future<map<string>> f10 = @strand {thread: "any"} start getEmpMap();
    map<string> m = checkpanic wait f9 | f10;
    assertIsOneOfTheAnydataValues(m, [
        {line1: "No. 20", line2: "Palm Grove", city: "Colombo 03"},
        {fname: "foo", lname: "bar"}
    ]);
}

function waitTest4() {
    future<int> f1 = @strand {thread: "any"} start add_1(2, 2);
    future<int> f2 = @strand {thread: "any"} start add_2(10, 12);
    future<boolean> f6 = @strand {thread: "any"} start status();
    future<string> f4 = @strand {thread: "any"} start concat("foo");
    string|int|boolean result = checkpanic wait f1 | f2 | f6 | f4;
    assertIsOneOfTheAnydataValues(result, [4, 22, true, "hello foo"]);
}

function waitTest5() {
    future<int|string> f7 = @strand {thread: "any"} start concat("xyz");
    int|string result = checkpanic wait fuInt() | f7;
    assertIsOneOfTheAnydataValues(result, ["hello xyz", 28]);
}

function waitTest6() {
    future<int|string> f7 = @strand {thread: "any"} start concat("xyz");
    future<int|float> f8 = @strand {thread: "any"} start add_3(66, 33);
    int|string|float result = checkpanic wait f7 | f8;
    assertIsOneOfTheAnydataValues(result, ["hello xyz", 99]);
}

function waitTest7() {
    future<int|float> f8 = @strand {thread: "any"} start add_3(66, 11);
    future<int|string> f7 = @strand {thread: "any"} start concat("xyz");
    future<string> f4 = @strand {thread: "any"} start concat("foo");
    future<string> f5 = @strand {thread: "any"} start concat("bar");
    int|string|float result = checkpanic wait f8 | f7 | f4 | f5;
    assertIsOneOfTheAnydataValues(result, [77, "hello foo", "hello bar", "hello xyz"]);
}

function waitTest8() {
    future<int> f1 = @strand {thread: "any"} start add_panic1(88, 88);
    future<int> f3 = @strand {thread: "any"} start add_panic2(50, 100);
    future<string> f4 = @strand {thread: "any"} start concat("foo");
    string|int|error result = trap wait f1 | f3 | f4;
    if (result is error) {
        test:assertTrue(result.message() == "err from panic");
    } else {
        test:assertTrue(result == "hello foo");
    }
}

function waitTest9() {
    future<int> f1 = start add_panic1(88, 88);
    future<int> f3 = start add_panic2(50, 100);
    future<int> f4 = @strand {thread: "any"} start add_panic3(4, 3);
    int|error result = trap wait f1 | f3 | f4;
    test:assertTrue(result is error);
    if (result is error) {
        test:assertTrue(result.message() == "err from panic");
    }
}

function waitTest10() {
    future<int> f1 = @strand {thread: "any"} start add_panic1(88, 88);
    future<int> f3 = @strand {thread: "any"} start add_panic2(50, 100);
    var result = trap checkpanic wait f1 | f3;
    test:assertTrue(result is error);
    if (result is error) {
        test:assertTrue(result.message() == "err from panic");
    }
}

function waitTest11() {
    worker w1 returns int {
        future<int> f1 = @strand {thread: "any"} start add_1(5, 2);
        future<int> f2 = @strand {thread: "any"} start add_3(50, 100);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    int result = wait w1;
    assertIsOneOfTheAnydataValues(result, [150, 7]);
}

function waitTest12() {
    worker w1 returns int {
        future<int> f1 = @strand {thread: "any"} start add_1(5, 2);
        future<int> f2 = @strand {thread: "any"} start add_3(50, 100);
        int l = 0;
        while (l < 9999) {
            l = l + 1;
        }
        int r = checkpanic wait f1 | f2;
        return r;
    }
    worker w2 returns int {
        future<int> f1 = @strand {thread: "any"} start add_1(50, 10);
        future<int> f2 = @strand {thread: "any"} start add_3(200, 99);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    int result = wait w1 | w2;
    assertIsOneOfTheAnydataValues(result, [150, 7, 60, 299]);
}

function waitTest13() {
    worker w1 returns int {
        future<int> f1 = @strand {thread: "any"} start add_1(5, 2);
        future<int> f2 = @strand {thread: "any"} start add_3(50, 100);
        sleep(3000);
        int r = checkpanic wait f1 | f2;
        return r;
    }
    worker w2 returns int|string {
        future<int> f1 = @strand {thread: "any"} start add_1(50, 10);
        future<string> f2 = @strand {thread: "any"} start concat("foo");
        int|string r = checkpanic wait f1 | f2;
        return r;
    }
    worker w3 returns int|string|boolean {
        future<int> f1 = @strand {thread: "any"} start add_1(6, 6);
        future<string> f2 = @strand {thread: "any"} start concat("bar");
        future<boolean> f3 = @strand {thread: "any"} start status();
        int|string|boolean r = checkpanic wait f1 | f2 | f3;
        sleep(2000);
        return r;
    }
    int|string|boolean result = wait w1 | w2 | w3;
    assertIsOneOfTheAnydataValues(result, [150, "hello foo", 7, 60, 12, "hello bar", true]);
}

function waitTest14() {
    map<any> m = {};
    m["x"] = 25;
    fork {
        @strand {thread: "any"}
        worker w1 {
            int a = 10;
            m["x"] = a;
            sleep(1000);
        }
        @strand {thread: "any"}
        worker w2 {
            int a = 5;
            int b = 15;
            sleep(2000);
            m["x"] = a + b;
        }
        @strand {thread: "any"}
        worker w3 {
            int b = 30;
            m["x"] = b;
        }
    }
    () results = wait w1 | w2 | w3;
    assertIsOneOfTheAnydataValues(<int>m["x"], [10, 20, 30]);
}

function waitTest15() {
    @strand {thread: "any"}
    worker w1 returns int {
        int a = 10;
        a -> w2;
        a = <- w2;
        return a;
    }
    @strand {thread: "any"}
    worker w2 returns int {
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;
        sleep(2000);
        return a;
    }
    int result = wait w1 | w2;
    result = result + 50;
    assertIsOneOfTheAnydataValues(result, [60, 65]);
}

function waitTest16() {
    future<int|error> f1 = @strand {thread: "any"} start addOrError(5, 2);
    future<int|error> f2 = @strand {thread: "any"} start addOrError(10, 12);
    int|error result = wait f1 | f2;
    validateError(result, "err returned");
}

function waitTest17() {
    future<int|error> f1 = @strand {thread: "any"} start addOrError(10, 12);
    future<string> f2 = @strand {thread: "any"} start concat("moo");
    int|string|error result = wait f1 | f2;
    if (result is error) {
        test:assertTrue(result.message() == "err returned");
    } else {
        test:assertTrue(result == "hello moo");
    }
}

function waitTest18() {
    future<error> f1 = @strand {thread: "any"} start funcWithErr();
    future<()> f2 = @strand {thread: "any"} start funcWithPanic();
    error? result = trap wait f1 | f2;
    test:assertTrue(result is error);
    if (result is error) {
        test:assertTrue(result.message().startsWith("A hazardous error occurred!!!"));
    }
}

function waitTest19() {
    future<string> f3 = @strand {thread: "any"} start concat("foo");
    future<int|error> f1 = @strand {thread: "any"} start addOrError(10, 12);
    future<int> f2 = @strand {thread: "any"} start add_panic1(55, 88);
    int|string|error result = trap wait f3 | f2 | f1;
    if (result is string) {
        test:assertEquals(result, "hello foo");
    } else if (result is error) {
        test:assertTrue((result.message() == "err returned") || (result.message() == "err from panic"));
    } else {
        panic error("test failed");
    }
}

function waitTest20() {
    future<()> f1 = start sleep(50);
    future<int> f2 = @strand {thread: "any"} start add_1(5, 2);
    future<string> f3 = @strand {thread: "any"} start greet();
    int|string|() result = checkpanic wait f1 | f2 | f3;
    if (result is int) {
        test:assertEquals(result, 7);
    } else if (result is string) {
        test:assertEquals(result, "good morning");
    } else {
        test:assertEquals(result, ());
    }
}

function waitTest21() {
    future<int|error> f1 = @strand {thread: "any"} start fError();
    future<int|error> f2 = @strand {thread: "any"} start sError();
    int|error result = wait f2 | f1;
    if (result is error) {
        test:assertTrue((result.message() == "first error returned") ||
        (result.message() == "A hazardous error occurred!!! Abort immediately!!"));
    } else {
        panic error("test failed");
    }
}

function add_panic1(int i, int j) returns int {
    int k = i + j;
    if (0 < 1) {
        error err = error("err from panic");
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
        error err = error("err from panic");
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
        error err = error("err from panic");
        panic err;
    }
    return k;
}

function add_1(int i, int j) returns int {
    int k = i + j;
    // sleep for 2s
    sleep(2000);
    int l = 0;
    while (l < 999999) {
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
    future<int> i = @strand {thread: "any"} start add_3(11, 17);
    return i;
}

function getEmpMap() returns map<string> {
    map<string> empMap = {fname: "foo", lname: "bar"};
    sleep(2000);
    return empMap;
}

function getAddrMap() returns map<string> {
    map<string> addrMap = {line1: "No. 20", line2: "Palm Grove", city: "Colombo 03"};
    return addrMap;
}

function addOrError(int i, int j) returns int|error {
    int k = i + j;
    if (0 < 1) {
        error err = error("err returned");
        return err;
    }
    return k;
}

function fError() returns int|error {
    error err = error("first error returned");
    return err;
}

function sError() returns error {
    error err = error("A hazardous error occurred!!! Abort immediately!!");
    sleep(2000);
    return err;
}

function funcWithErr() returns error {
    error err = error("A hazardous error occurred!!!");
    return err;
}

function funcWithPanic() {
    sleep(1500);
    if (true) {
        error err = error("A hazardous error occurred!!! Panic!!");
        panic err;
    }
}

function sleep(int millis) {
    runtime:sleep((<decimal>millis) / 1000);
}

function assertIsOneOfTheAnydataValues(anydata result, anydata[] values) {
    boolean isOneValue = false;
    foreach var item in values {
        if (item == result) {
            isOneValue = true;
            break;
        }
    }
    if (!isOneValue) {
        panic error("Not one of the expected values");
    }
}

function validateError(any|error value, string message) {
    if (value is error) {
        if (value.message() == message) {
            return;
        }
        panic error("Expected error message: " + message + ", found: " + value.message());
    }
    panic error("Expected error, found: " + (typeof value).toString());
}
