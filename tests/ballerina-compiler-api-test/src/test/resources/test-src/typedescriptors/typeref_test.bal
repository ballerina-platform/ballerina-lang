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

# Represents a person.
type Person record {
    string name;
    int age;
};

# Represents an employee.
class Employee {
    string name;
    string designation;
}

# An enumeration of colours.
enum Colour {
    RED, GREEN, BLUE
}

# Record with enum field to test the enum type reference issue
type Department record {
    string name;
    Colour code;
};

function test() {
    Person p;
    Employee e;
    Colour c;
    Department d;
}

type Age int;

function testAge() {
    record {|
        Age age;
    |} p;

    Foo f;
    Bar b;
    Baz z;
}

function test2() {
    readonly & Person|int person = {name: "A", age: 0};
    if person is int {
        return;
    }
    _ = person.entries();
}

function test3() {
    readonly & Person|int person = {name: "A", age: 0};
    if person is readonly & Person {
        _ = person.entries();
    }
}

function test4() returns error? {
    var person = check fn();
    _ = person.entries();
}

function test5() {
    Person & readonly|xml & readonly|error val = error("");
    if val is xml & readonly|error {
        return;
    }
    _ = val.entries();
}

// utils
type Foo Person;

type Bar Foo;

type Baz decimal;

function fn() returns readonly & Person|error => error("");
