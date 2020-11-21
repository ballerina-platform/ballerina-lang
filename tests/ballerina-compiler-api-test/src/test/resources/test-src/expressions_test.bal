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

function testLiterals() {
    anydata[] arr = [5, 12.34, 34.5d, true, (), "foo"];
    json j = null;
    byte[] b = base64 `SGVsbG8gQmFsbGVyaW5h`;
}

function testTemplateExprs() {
    string s = string `Hello World!`;
    xml x = xml `<Greeting>Hello!</Greeting>`;
    'object:RawTemplate rt = `${s} from Ballerina`;
}

function testStructuralConstructors() {
    string[] arr1 = ["foo", "bar"];
    var arr2 = [10, 20, 30];

    [int, string, float] tup = [10, "foo", 12.34];

    map<string> m = {"foo": "bar"};
    var m2 = {"City": "Colombo", "Code": 1};

    record {|
        string name;
        int age;
    |} person = {name: "John Doe", age: 20};

    json j = {name: "Jane Doe", age: 25};
}

function testAccessExprs() {
    record {|
        string name;
        int age?;
    |} person = {name: "John", age: 20};

    string name = person.name;
    int? age = person?.age;
    string optName = person["name"];
}

function testObjectConstructor() {
    PersonObj p1 = new ("Pubudu");
    PersonObj p2 = new PersonObj("Pubudu");

    object {
        string name;

        function getName() returns string;
    } person = object {
        string name = "Anon";

        function getName() returns string => self.name;
    };
}

function testMiscExprs() {
    int x = -20;
    int y = 10 * 20 / 5;
    int z = 10 + 20 - 5;
    boolean b = y >= z;
    anydata ad = "foo";

    if (ad is string && y != 0) {
        string s = ad;
    }

    string greeting = ad == "" ? "Hello" : "Hello " + ad.toString();
}

function testCheckingExprs() returns error? {
    string s1 = check foo();
    string s2 = checkpanic foo();
}

function testCastingExprs() {
    anydata ad = 10;
    string s = <string>ad;
    int x = <@untainted int>ad;
}

function testIneferredRecordType() {
    {"name" : "foo"};
}

function testStartAction() {
    start testAsync();
}

public function testAsync() {
    // do something
}

// utils

class PersonObj {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}

function foo() returns string|error => "foo";
