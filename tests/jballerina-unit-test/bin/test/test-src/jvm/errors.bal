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

import ballerina/lang.'error as lang;

function testPanic(int y) {
    int|error x = foo(y);
    validateError(y, x);
}

function testTrap(int y) {
    int|error x = trap foo(y);
    if (x is error) {
        string msg = x.message();
        if (msg == "reason foo 1") {
            return;
        }
        panic error("Expected error message: reason foo 1, found: " + msg);
    }
    panic error("Expected an error found: " + (typeof x).toString());
}

function testNestedCallsWithSingleTrap(int y) {
    int|error x = trap bar(foo(bar(y)));
    if x is error {
        if (y == 0) {
            if (x.message() == "reason bar 0") {
                return;
            }
            panic error("Expected error message: reason bar 0, found: " + x.message());
        }

        if (y == 1) {
            if (x.message() == "reason foo 1") {
                return;
            }
            panic error("Expected error message: reason foo 1: found: "+ x.message());
        }
    }
    panic error("Assertion error");
}

function testNestedCallsWithAllTraps(int y) {
    int|error x = trap bar(trap foo(trap bar(trap <int>y)));

    validateError(y, x);
}

function validateError(int y, int|error x) {
    if (x is error) {
        var expMessage = "reason foo " + y.toString();
        if (!(x.message() == expMessage)) {
            panic error("Expected:" + expMessage + ", found: " + x.message());
        }
        var detailMessage = x.detail()["message"];
        if (detailMessage is string) {
            if y == 0 && detailMessage != "foo" {
                panic error("Expected: foo, found: " + detailMessage);
            }
            if y != 0 && detailMessage != "int value" {
                panic error("Expected: int value, found: " + detailMessage);
            }
        }
        return;
    }
    panic error("Expected an error found: " + (typeof x).toString());
}

function testNestedCallsWithSomeTraps(int y) returns int|error {
    int|error x = trap bar(foo(trap bar(<int>y)));
    return x;
}

public function foo(int|error x) returns int|error {
    if (x is error) {
        panic error("reason foo 0", message = "foo");
    } else {
        panic error("reason foo 1", message = "int value");
    }
}

public function bar(int|error x) returns int|error {
    if (x is int) {
        if (x == 0) {
            panic error("reason bar 0", message = "bar");
        }
        return x;
    }
    return x;
}

public function testSelfReferencingError() returns error {
    MyError cause = error MyError("root cause msg");
    MyError e = error MyError("actual error msg", cause);
    return e;
}

type MyError error<MyErrorData>;

type MyErrorData record {|
    *lang:Detail;
|};
