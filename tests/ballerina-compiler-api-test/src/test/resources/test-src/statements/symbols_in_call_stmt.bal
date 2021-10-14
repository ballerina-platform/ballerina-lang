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

function test() returns error? {
    Dog dog = new ();
    string name = "john";
    int val = 10;
    int[] ints = [1, 2, 3, 4];

    foo("John", val);
    foo(name, age = val);
    foo(name, val, ...ints);
    check bar(name, val);
    checkpanic bar(name, val);

    dog.eat();
    dog.bark(val, j = name);
    dog.bark(val, name, ...ints);
    check dog.walk();
    checkpanic dog.walk();
}

function foo(string name, int age, any... other) {
}

function bar(string name, int age) returns error {
    return error("error");
}

class Dog {
    function eat() {
    }

    function bark(int i, string j, int... ints) {
    }

    function walk() returns error? {
    }
}

type Person record {|
    string name;
    int age;
|};
