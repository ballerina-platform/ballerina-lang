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

function testWithValue() {
    string|MyError someValue = "some";
    if (someValue is string) {
    } else {
        test:assertFail();
    }

    if (someValue is readonly) { // always true
    }

    if (someValue is string|int) {
    } else {
        test:assertFail();
    }

    if (someValue is MyError|YourError) {
        test:assertFail();
    }

    if (someValue is MyError) {
        test:assertFail();
    }

    if (someValue is error) {
        test:assertFail();
    }
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
    if (someValue is Person) {
    } else {
        test:assertFail();
    }

    if (someValue is readonly) {
        test:assertFail();
    }

    if (someValue is Student) {
        test:assertFail();
    }

    if (someValue is MyError|YourError) {
        test:assertFail();
    }

    if (someValue is MyError) {
        test:assertFail();
    }

    if (someValue is error) {
        test:assertFail();
    }
}

function testWithError() {
    string|MyError someValue = error MyError("io error", code = 0);
    if (someValue is string) {
        test:assertFail();
    }

    if (someValue is readonly) { // always true
    }

    if (someValue is string|int) {
        test:assertFail();
    }

    if (someValue is MyError|YourError) {
    } else {
        test:assertFail();
    }

    if (someValue is MyError) {
    } else {
        test:assertFail();
    }

    if (someValue is YourError) {
        test:assertFail();
    }

    if (someValue is error) {
    } else {
        test:assertFail();
    }
}

function testWithOnlyError() {
    error err = error("hello");

    if(err is MyError) {
        test:assertFail();
    }

    if (err is MyError|YourError) {
        test:assertFail();
    }

    if (err is MyError|error) { // always true
    }

    if (err is error) { // always true
    }
}

function testWithMultipleErrors() {
    MyError|YourError|int someValue = 1;
    if(someValue is MyError) {
        test:assertFail();
    }

    if (someValue is MyError|YourError) {
        test:assertFail();
    }

    if (someValue is MyError|error) {
        test:assertFail();
    }

    if (someValue is error) {
        test:assertFail();
    }

    if (someValue is int) {
    } else {
        test:assertFail();
    }
}

function testWithMultipleErrorsAndError() {
    MyError|YourError|int someValue = error MyError("io error", code = 0);
    if(someValue is MyError) {
    } else {
        test:assertFail();
    }

    if (someValue is MyError|YourError) {
    } else {
        test:assertFail();
    }

    if (someValue is MyError|error) {
    } else {
        test:assertFail();
    }

    if (someValue is error|MyError) {
    } else {
        test:assertFail();
    }

    if (someValue is error) {
    } else {
        test:assertFail();
    }

    if (someValue is int) {
        test:assertFail();
    }

    if (someValue is YourError) {
        test:assertFail();
    }
}
