// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

type E1 distinct error;
type E2 distinct error;

function foo(int state) returns int|E1|E2 {
    if (state == 0) {
        return error("Custom Error E1");
    } else if (state == 1) {
        return error E2("Custom Error E2");
    } else {
        return state;
    }
}

function bar() returns error? {
    return error("Dummy error");
}

function checkedExprErrorReturnType(int state) returns int|error {
    return check foo(state);
}

function testCheckedExprErrorReturnType() {
    test:assertTrue(checkedExprErrorReturnType(0) is E1);
    test:assertTrue(checkedExprErrorReturnType(1) is E2);
    test:assertTrue(checkedExprErrorReturnType(5) is int);
    test:assertFalse(checkedExprErrorReturnType(0) is E2);
    test:assertFalse(checkedExprErrorReturnType(1) is E1);
}

function checkedExprErrorReturnTypeWithOnFail(int state) returns int|error {
    do {
        if (foo(state) == state) {
            return check foo(state - 2);
        } else {
            return check foo(state);
        }
    } on fail var e {
        return e;
    }
}

function testCheckedExprErrorReturnTypeWithOnFail() {
    test:assertTrue(checkedExprErrorReturnTypeWithOnFail(0) is E1);
    test:assertTrue(checkedExprErrorReturnTypeWithOnFail(1) is E2);
    test:assertTrue(checkedExprErrorReturnTypeWithOnFail(2) is E1);
    test:assertTrue(checkedExprErrorReturnTypeWithOnFail(3) is E2);
    test:assertTrue(checkedExprErrorReturnTypeWithOnFail(5) is int);
    test:assertFalse(checkedExprErrorReturnTypeWithOnFail(0) is E2);
    test:assertFalse(checkedExprErrorReturnTypeWithOnFail(1) is E1);
}

function checkedExprErrorReturnTypeWithNestedFailStmt(int state) returns int|error {
    do {
        if (foo(state) == state) {
            return check foo(state - 2);
        } else {
            return check foo(state);
        }
    } on fail var e {
        check bar();
        return e;
    }
}

function testCheckedExprErrorReturnTypeWithNestedFailStmt() {
    test:assertTrue(checkedExprErrorReturnTypeWithNestedFailStmt(0) is error);
    test:assertTrue(checkedExprErrorReturnTypeWithNestedFailStmt(1) is error);
    test:assertTrue(checkedExprErrorReturnTypeWithNestedFailStmt(5) is int);
    test:assertFalse(checkedExprErrorReturnTypeWithNestedFailStmt(0) is E1);
    test:assertFalse(checkedExprErrorReturnTypeWithNestedFailStmt(0) is E2);
    test:assertFalse(checkedExprErrorReturnTypeWithNestedFailStmt(1) is E1);
    test:assertFalse(checkedExprErrorReturnTypeWithNestedFailStmt(1) is E2);
    test:assertFalse(checkedExprErrorReturnTypeWithNestedFailStmt(5) is error);
}
