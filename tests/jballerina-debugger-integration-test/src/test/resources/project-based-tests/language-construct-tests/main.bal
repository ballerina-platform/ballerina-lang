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

import ballerina/test;

public client class Person {
    public int age;
    public string firstName;
    public string lastName;

    function init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    remote function getName() returns string {
        string name = self.firstName;
        return name;
    }
}

public client class MockPerson {
    public int age;
    public string firstName;
    public string lastName;

    function init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    remote function getName() returns string {
        string name = self.firstName;
        return name;
    }
}

public function main() {
    Person person = new Person(25, "John", "Doe");
    string fullName = person.getFullName();
    string name = person -> getName();

    Person person2 = new Person(30, "Jack", "Sparrow");
    person2 = test:mock(Person, new MockPerson(27, "Praveen", "Nada"));
    string person2FullName = person2.getFullName();
    string person2Name = person2 -> getName();
}
