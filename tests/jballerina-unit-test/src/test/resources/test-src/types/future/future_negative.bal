// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function testWaitTwiceOnSingleFuture() {
    future<int|error> f4 = start calculate(9,8);
    int|error r = wait f4;
    int|error r1 = wait f4;
    assertError(r1);
}

public function testMultipleWaitTwiceOnTwoFutures() {
    future<int|error> f4 = start calculate(9, 8);
    future<int|error> f5 = start calculate(5, 4);
    record { int|error r1; int|error r2; } result = wait {r1: f4, r2: f5};
    record { int|error r1; int|error r2; } result1 = wait {r1: f4, r2: f5};
    assertError(result1.r1);
    assertError(result1.r2);
}

public function testMultipleWaitAndAlternateWait() {
    future<int|error> f4 = start calculate(9, 8);
    future<int|error> f5 = start calculate(5, 4);
    record { int|error r1; int|error r2; } result = wait {r1: f4, r2: f5};
    int|error r1 = wait f4;
    assertError(r1);
}

public function testMultipleWaitTwiceOnDifferentFutures() {
    future<int> f1 = start calculate(3, 4);
    future<int|error> f4 = start calculate(9, 8);
    future<int|error> f5 = start calculate(5, 4);
    record { int|error r1; int|error r2; } result = wait {r1: f4, r2: f5};
    record { int|error r1; int|error r2; } result1 = wait {r1: f4, r2: f1};
    int|error r1 = result1.r1;
    assertError(result1.r1);
    test:assertEquals(result1.r2, 12);
}

public function testAlternateWaitTwiceOnTwoFutures() {
    future<int|error> f4 = start calculate(9, 8);
    future<int|error> f5 = start calculate(5, 4);
    int|error result = wait f4|f5; // waits on one of the futures
    int|error result1 = wait f4|f5; // waits on the other future
    int|error r1 = wait f4|f5; // both futures are now waited on so returns an error
    assertError(r1);
}

function calculate(int a, int b) returns int {
    return a*b;
}

function testWaitError() {
    future<error> f1 = @strand{thread:"any"} start getError();
    future<error> f2 = @strand{thread:"any"} start getError();
    error result = wait f1|f2;
    error r1 = wait f1;
    assertError(r1);
}

function getError() returns error {
    return error("This is an error");
}
public function assertError(int|error r1) {
    if(r1 is error) {
        test:assertEquals(r1.message(), "multiple waits on the same future is not allowed");
    } else {
        test:assertFail();
    }
}
