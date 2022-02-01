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

function testOrdinaryAssignment() {
    int intVal;
    Person person;
    map<int> numbers;

    intVal = func1();
    person = {name: "John", age: 20};
    person.name = "Doe";
    person["dept"] = "sales";

    Author author = new();
    author.books = 2;

    Human human = new Author();
    human.age = 20;
}

class Author {
    int books = 0;
    int age;
    
    function init() {
        self.age = 0;
    }
}

type Human object {
    int age;
};

function testCompoundAssignment() {
    int intVal = 0;
    Person person = {name: "Carl", age: 18};
    Author author = new();

    intVal += 1;
    person.age << = 2;
    person["age"] ^= 2;
}

function testDestructuringAssignment() {
    Person person = {name: "John", age: 0};

    _ = func1();
    [int, string, int...] [intVal, stringVal, ...otherVal] = [5, "myString", 3, 1];
    Person {name: valName, age: valAge} = person;

}

function func1() returns int {
    return 10;
}

type Person record {|
    string name;
    int age;
    string dept?;
|};
