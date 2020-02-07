// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testEntries() returns map<[string, anydata]> {
    return person.entries();
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

function testHasKey2() returns [boolean, boolean] {
    Person anotherPerson = {name: "Jane Doe", age: 30};
    boolean b1 = anotherPerson.hasKey("gender");
    anotherPerson["gender"] = "female";
    return [b1, anotherPerson.hasKey("gender")];
}

function testKeys() returns string[] {
    return person.keys();
}

function testMap() returns map<int> {
    return person.'map(function (anydata v) returns int {
        if (v is string) {
            return v.length();
        } else if (v is int) {
            return v;
        }
        return 0;
    });
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

function testFilter() returns map<int> {
    map<int> filteredGrades = grades.filter(function (int d) returns boolean { return d > 50; });
    return filteredGrades;
}

function testReduce() returns float {
    float avg = grades.reduce(function (float accum, int val) returns float {
        return accum + <float>val / grades.length();
    }, 0.0);
    return avg;
}
