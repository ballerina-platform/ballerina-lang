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

class Foo {
    string name = "default";
}

function testInvalidOptionalFieldAccessOnObject() {
    Foo f = new;
    string name = f?.name;

    Foo? f2 = new;
    string? name2 = f2?.name;
}

type Employee record {
    string name;
    int id?;
};

type Person record {
    string name;
    float id;
};

function testInvalidOptionalFieldAccessOnRecord() {
    Employee e = { name: "Anne", id: 100 };
    int id = e?.id;
    _ = e?.salary;
}

function testInvalidOptionalFieldAccessTypeForJson() {
    json j = { hello: "world" };
    json? j2 = j?.hello;
}

function testInvalidOptionalFieldAccessOnRecordUnion() {
    Employee e = { name: "Anne", id: 100 };
    Employee|Person ep = e;
    int id = ep?.id;
}

function testInvalidOptionalFieldAccessOnMap() {
    map<int> m = { id: 100 };
    int id = m?.id;

    map<string>? m2 = {};
    string? id2 = m2?.id2;

    map<xml> m3 = {};
    map<xml>|map<json> m4 = m3;
    xml|json id3 = m4?.id;
}

function testInvalidOptionalFieldAccessTypeForLaxType() {
    json j = { a: 1, b: { c: "foo" } };
    map<json>|json j1 = j;
    json j2 = j1?.a;
}

class Qux {
    int? i = 1;
}

function getQux() returns Qux {
    return new;
}

function getNonOptionalFieldAccessibleValue() returns string[] {
    return [];
}

function testInvalidOptionalFieldAccessOnInvocation1() {
    _ = getQux()?.i;
}

function testInvalidOptionalFieldAccessOnInvocation2() {
    _ = getNonOptionalFieldAccessibleValue()?.x;
}

class Student {
    public Details? details = ();

    function init(Details? details) {
        self.details = details;
    }

    public function getDetails() returns Details? {
        return self.details;
    }
}

public type Details record {
    Address addr?;
};

public type Address record {
    string street;
};

function testOptionalFieldAccessOnClassDef() {
    Address addr = {street: "Colombo"};

    Details details = {addr: addr};

    Student person = new Student(details);
    string? c1 = person.getDetails()?.addr?.road;
}

public type Quux record {|
    int i;
    Baz|int[] baz?;
|};

public type Baz record {|
    string a;
    int i?;
|};

public type Corge record {|
    int i;
    Baz baz?;
|};

public function testNestedOptionalFieldAccessOnIntersectionTypesNegative() {
    Quux & readonly q1 = {i: 1, baz: {a: "hello", i: 2}};
    var v1 = q1?.baz?.i;
    any v2 = q1["baz"]?.i;

    Corge & readonly q2 = {i: 1, baz: {a: "hello", i: 2}};
    string v3 = q2?.baz?.i;
    string v4 = q2?.baz["i"];
}
