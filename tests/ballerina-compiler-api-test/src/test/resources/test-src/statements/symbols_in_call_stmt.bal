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

function test() {
    Person person = {name: "John", age: 0};
    Dog dog = new();
    int val = 10;

    foo("John", 12);
    foo("John", val);
    foo("John", age = val);
    foo(...person);

     dog.eat();
     dog.bark(val);
     dog.bark(i = val);
}

function foo(string name, int age) {
}

class Dog {
    function eat() {
    }

    function bark(int i) {
    }
}

type Person record {|
    string name;
    int age;
|};
