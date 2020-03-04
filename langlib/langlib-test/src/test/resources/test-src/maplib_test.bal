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

map<string> countries = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};

function testLength() returns int {
    return countries.length();
}

function testGet(string key) returns string {
    return countries.get(key);
}

function testEntries() returns map<[string, string]> {
    return countries.entries();
}

function testRemove(string key) returns [string, map<string>] {
    map<string> countriesDup = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};
    return [countriesDup.remove(key), countriesDup];
}

function testRemoveAll() returns map<string> {
    map<string> countriesDup = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};
    countriesDup.removeAll();
    return countriesDup;
}

function testRemoveIfHasKey() {
    map<string> student = {id:"1", name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    string? s = student.removeIfHasKey("name");
    if (s is ()) {
         error err = error("Returned value should be an string.");
         panic err;
    }
    if (<string> s != "Andrew") {
         error err = error("Returned value should equals 'Andrew'.");
         panic err;
    }

    string? age = student.removeIfHasKey("age");
    if !(age is ()) {
        error err = error("Returned value should be nil.");
        panic err;
    }
}

function testHasKey(string key) returns boolean {
    return countries.hasKey(key);
}

function testKeys() returns string[] {
    return countries.keys();
}

function testMap() returns map<float> {
    map<int> m = {"1":1, "2":2, "3":3};
    return m.'map(function (int v) returns float { return v * 5.5; });
}

function testForEach() returns string {
    string result = "";
    countries.forEach(function (string val) {
        result += val;
    });
    return result;
}

function testFilter() returns map<decimal> {
    map<decimal> m = {"1":12.34, "2":3.45, "3":7.89, "4":21.2};
    map<decimal> filteredM = m.filter(function (decimal d) returns boolean { return d > 10; });
    return filteredM;
}

function testReduce() returns float {
    map<int> grades = {"maths":79, "physics":84, "chemistry":72, "ict":87};
    float avg = grades.reduce(function (float accum, int val) returns float {
        return accum + <float>val / grades.length();
    }, 0.0);
    return avg;
}
