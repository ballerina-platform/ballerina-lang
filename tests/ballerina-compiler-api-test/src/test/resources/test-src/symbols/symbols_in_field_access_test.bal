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

function testFieldAccess() {
    Person p = {name: "John Doe", age: 20};
    string s = p.name;

    int age = getPerson().age;

    json j1 = {a: {b: i}};
    json|error f = j1.a.b;

    map<json> j2 = {a: {b: i}};
    f = j2.a.b;

    Person2 p2 = {name: "J. Doe", age: 30};
    var adr = p2.address.city;

    p2.address = {city: "Colombo", country: "Sri Lanka"};

    PersonObj p3 = new PersonClz("J. Doe");
    s = p3.name;

    PersonClz p4 = new("J. Doe");
    s = p4.name;
}

function testOptionalFieldAccess() {
    Employee emp = {name: "J. Doe"};
    var v = emp?.designation;
}

// utils

function getPerson() returns Person => {name: "Jane Doe", age: 25};

type Person record {
    string name;
    int age;
};

type Address record {
    string city;
    string country;
};

public type Person2 record {
    string name;
    int age;

    record {|
        string city;
        string country;
    |} address;
};

type PersonObj object{
    string name;
};

class PersonClz {
    string name;

    function init(string name) {
        self.name = name;
    }
}

type Employee record {
    string name;
    string designation?;
};
