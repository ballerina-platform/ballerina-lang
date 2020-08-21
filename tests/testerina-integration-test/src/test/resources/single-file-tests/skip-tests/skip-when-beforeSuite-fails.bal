// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// This file is used to test the behavior when there is a failing BeforeSuite function.
import ballerina/test;
import ballerina/io;

string a = "before";

@test:BeforeSuite
public function beforeSuite() {
    io:println("Failing Before Suite");
    int i = 12/0; // This will throw an exception and fail the function
}

@test:AfterSuite {}
public function afterSuite() {
    a = a + "after";
    io:println("After Suite");
}

@test:AfterSuite { alwaysRun : true}
public function afterSuiteAlwaysRun() {
    io:println("After Suite AlwaysRun");
    io:println("Value of a is " + a);
}

@test:BeforeEach
public function beforeEach() {
    io:println("Before each");
    a = a + "beforeEach";
}

@test:AfterEach
public function afterEach() {
    io:println("After each");
    a = a + "afterEach";
}

public function beforeFunc() {
    io:println("Before");
    a = a + "before";
}

public function afterFunc() {
    io:println("After");
    a = a + "after";
}

@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
public function test1() {
    io:println("test1");
    a = a + "test1";
}

@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
    a = a + "test2";
}

@test:Config {}
public function test3() {
    io:println("test3");
    a = a + "test3";
}
