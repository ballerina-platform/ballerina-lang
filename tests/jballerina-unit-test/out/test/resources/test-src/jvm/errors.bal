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

function testPanic(int y) returns int|error {
    int|error x = foo(y);
    return x;
}

function testTrap(int y) returns int|error {
    int|error x = trap foo(y);
    return x;
}

function testNestedCallsWithSingleTrap(int y) returns int|error {
    int|error x = trap bar(foo(bar(y)));
    return x;
}

function testNestedCallsWithAllTraps(int y) returns int|error {
    int|error x = trap bar(trap foo(trap bar(trap <int>y)));
    return x;
}

function testNestedCallsWithSomeTraps(int y) returns int|error {
    int|error x = trap bar(foo(trap bar(<int>y)));
    return x;
}

public function foo(int|error x) returns int|error {
    if (x is error) {
        panic error("reason foo 1", message = "foo");
    } else {
        panic error("reason foo 2", message = "int value");
    }
}

public function bar(int|error x) returns int|error {
    if (x is int) {
        if (x == 0) {
            panic error("reason bar 1", message = "bar");
        }
        return x;
    }
    return x;
}

public function testSelfReferencingError() returns error {
    MyError cause = error("root cause", message = "root cause msg");
    MyError e = error("actual error", message = "actual error msg", cause = cause);
    return e;
}

type MyError error<string, MyErrorData>;

type MyErrorData record {|
    *lang:Detail;
|};
