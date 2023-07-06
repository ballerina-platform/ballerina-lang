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

function testStartAction() {
    string nm = "john";
    int ag = 12;
    Person p = {name: "doe", age: 40};
    Animal animal = new ();
    Vehicle vehicle = new ();
    int[] ints = [1, 2, 3, 4];

    future<()> result = start foo();
    _ = start bar(nm, age = 12);
    _ = start bar(nm, 10, ...ints);

    _ = start animal.eat();
    _ = start animal.walk(ag, path = nm);
    _ = start animal.walk(ag, nm, ...ints);

    _ = start vehicle->ride(nm, b = ag);
    _ = start vehicle->ride(nm, ag, ...ints);
}

function foo() {
}

function bar(string name, int age, any... other) {
}

class Animal {
    function eat() {
    }

    function walk(int dist, string path, any... config) {
    }
}

client class Vehicle {
    function ride(string a, int b, int... ints) {
    }
}

type Person record {|
    string name;
    int age;
|};
