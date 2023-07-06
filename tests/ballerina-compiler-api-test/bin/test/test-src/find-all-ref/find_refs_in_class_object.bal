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

class Animal {
    function walk() {
    }
}

type Person object {
    string name;

    function getName() returns string;
};

class Student {
    *Person;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }

    function doSomething() {
        Animal an = new();
        an.walk();
    }

    function () returns string greet = () => "Greetings";
}

client class Vehicle {
    function ride(string a, int b, int... ints) {
    }
}

function testClassObject() {
    Student st = new("CM");

    Person p = st;

    string firstName = st.getName();

    string lastName = st.name;

    string greetings = st.greet();

    Vehicle vehicle = new();

    _ = start vehicle->ride("john", 12);
}
