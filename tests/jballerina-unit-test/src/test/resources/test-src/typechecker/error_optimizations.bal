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

public type MyError error<record { int code; string message?; error cause?; }>;
public type YourError error<record { int value; string message?; error cause?; }>;

type FooError error;
type BarError error;

function testWithValue() {
    string|MyError someValue = "some";
    test:assertTrue(someValue is string);
    // Always evaluates to true
    //test:assertTrue(someValue is readonly);
    test:assertTrue(someValue is string|int);
    test:assertFalse(someValue is MyError|YourError);
    test:assertFalse(someValue is MyError);
    test:assertFalse(someValue is error);
}

type Person record {
    string name;
    string age;
};

type Student record {
    string name;
    string age;
    string school;
};

function testWithRecordAndError() {
    Person|MyError someValue = {name: "John", age: "30"};
    test:assertTrue(someValue is Person);
    // Always evaluates to false
    // test:assertFalse(someValue is readonly);
    test:assertFalse(someValue is Student);
    test:assertFalse(someValue is MyError|YourError);
    test:assertFalse(someValue is MyError);
    test:assertFalse(someValue is error);
}

function testWithError() {
    string|MyError someValue = error MyError("io error", code = 0);
    test:assertFalse(someValue is string);
    // Always evaluates to true
    // test:assertTrue(someValue is readonly);
    test:assertFalse(someValue is string|int);
    test:assertTrue(someValue is MyError|YourError);
    test:assertTrue(someValue is MyError);
    test:assertFalse(someValue is YourError);
    test:assertTrue(someValue is error);
}

function testWithOnlyError() {
    error err = error("hello");
    test:assertFalse(err is MyError);
    test:assertFalse(err is MyError|YourError);
    test:assertTrue(err is MyError|error);
    test:assertTrue(err is error);
}

function testWithMultipleErrors() {
    MyError|YourError|int someValue = 1;
    test:assertFalse(someValue is MyError);
    test:assertFalse(someValue is MyError|YourError);
    test:assertFalse(someValue is MyError|error);
    test:assertFalse(someValue is error);
    test:assertTrue(someValue is int);
}

function testWithMultipleErrorsAndError() {
    MyError|YourError|int someValue = error MyError("io error", code = 0);
    test:assertTrue(someValue is MyError);
    test:assertTrue(someValue is MyError|YourError);
    test:assertTrue(someValue is MyError|error);
    test:assertTrue(someValue is MyError|error);
    test:assertTrue(someValue is error);
    test:assertFalse(someValue is int);
    test:assertFalse(someValue is YourError);
}

function testMultipleErrorUnionWithError() {
    FooError|error foo = error("");
    test:assertTrue(foo is FooError);
    test:assertTrue(foo is BarError);
    test:assertTrue(foo is error);

    MyError|error errorValue = error("");
    test:assertTrue(errorValue is error);
    test:assertFalse(errorValue is MyError);
    test:assertFalse(errorValue is YourError);
    test:assertTrue(errorValue is FooError);
    test:assertTrue(errorValue is FooError|BarError);
}
