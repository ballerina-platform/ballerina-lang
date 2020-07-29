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

import ballerina/lang.test as test;

type Person record {
    string name;
    int age;
};

Person person = {name: "John Doe", age: 25};

function testLength() returns int {
    return person.length();
}

function testGet(string key) returns anydata {
    return person.get(key);
}

function testEntries() {
    map<[string, anydata]> mp = person.entries();
    test:assertEquals(mp.length(), 2);
    test:assertEquals(mp.get("name"), ["name", "John Doe"]);
    test:assertEquals(mp.get("age"), ["age", 25]);
}

function testRemove(string key) returns anydata {
    Person anotherPerson = {name: "Jane Doe", age: 30};
    return anotherPerson.remove(key);
}

function testRemoveAll() {
    Person anotherPerson = {name: "Jane Doe", age: 30};
    anotherPerson.removeAll();
}

function testHasKey(string key) returns boolean {
    return person.hasKey(key);
}

function testHasKey2() {
    Person anotherPerson = {name: "Jane Doe", age: 30};
    boolean b1 = anotherPerson.hasKey("gender");
    anotherPerson["gender"] = "female";
    test:assertFalse(b1);
    test:assertTrue(anotherPerson.hasKey("gender"));
}

function testKeys() {
    string[] keys = person.keys();
    test:assertEquals(keys.length(), 2);
    test:assertEquals(keys[0], "name");
    test:assertEquals(keys[1], "age");
}

function testMap() {
    map<int> mp = person.'map(function (anydata v) returns int {
        if (v is string) {
            return v.length();
        } else if (v is int) {
            return v;
        }
        return 0;
    });
    test:assertEquals(mp.length(), 2);
    test:assertEquals(mp.get("name"), 8);
    test:assertEquals(mp.get("age"), 25);
}

function testForEach() returns string {
    string result = "";
    person.forEach(function (anydata val) {
        if (val is string) {
            result += val;
        } else {
            result += val.toString();
        }
    });
    return result;
}

type Grade record {|
    int maths;
    int physics;
    int chemistry;
    int ict;
|};

Grade grades = {maths: 45, physics: 75, chemistry: 40, ict: 85};

function testFilter() {
    map<int> filteredGrades = grades.filter(function (int d) returns boolean { return d > 50; });
    test:assertEquals(filteredGrades.length(), 2);
    test:assertEquals(filteredGrades.get("physics"), 75);
    test:assertEquals(filteredGrades.get("ict"), 85);
}

function testReduce() returns float {
    float avg = grades.reduce(function (float accum, int val) returns float {
        return accum + <float>val / grades.length();
    }, 0.0);
    return avg;
}
