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

type Employee record {
    string name;
    Employee? employer = ();
    int id?;
};

type Person record {
    string name;
    Person? employer = ();
    float salary;
    int id;
};

function testInvalidOptionalFieldAccess1() {
    Employee e = { name: "Anne" };
    _ = e.id;
    _ = e.salary;
}

function testInvalidOptionalFieldAccess2() {
    Employee e = { name: "Anne" };
    Employee|Person ep = e;
    _ = ep.salary;
}

type EmployeeTwo record {
    string name;
    int id;
};

type PersonTwo record {
    string name;
    string id;
    float salary;
};

function testInvalidFieldAccessType() {
    PersonTwo e = { name: "s1", id: "s2", salary: 100.0 };
    EmployeeTwo|PersonTwo ep = e;
    string id = ep.id;
    int id2 = ep.id;
}

function testInvalidFieldAccessOnMap() {
    map<string> m = { one: "hello" };
    string s = m.one;
}

function testInvalidFieldAccessOnUnion1() {
    map<string> m = { name: "Anne" };
    map<string>|EmployeeTwo me = m;
    string s = me.name;
}

function testInvalidFieldAccessOnUnion2() {
    EmployeeTwo e = { name: "Anne", id: 1000 };
    EmployeeTwo? en = e;
    string? s = en.name;
}

function testInvalidFieldAccessOnUnion3() {
    map<string> m = { name: "Anne" };
    map<string>|map<int> m2 = m;
    string|int s = m2.name;
}

function testInvalidFieldAccessTypeOnJson() {
    json m = { one: "hello" };
    json s = m.one;
}

function testInvalidFieldAccessTypeOnJsonMap() {
    map<json> m = { one: "hello" };
    json s = m.one;
}

function testInvalidFieldAccessOnJsonInUnion() {
    json m = { one: { two: "hello" } };
    json|error one = m.one;
    json|error two = one.two;
}

function testJsonMapAccessInvalidType() {
    map<json> m0 = { x: { y: 1 } };
    map<map<json>>|map<json> m1 = m0;
    map<json>|error m2 = m1.x;

    map<map<json>> m3 = { a: { b: 1 }, c: { d: 2, e: "hello" } };
    map<map<json>>|map<map<map<json>>> m4 = m3;
    map<json> jv = m4.a.b;
}

type Foo record {
    Bar bar;
    function() returns Baz? bazFunc = getBaz;
};

class Bar {
    int i = 10;
}

type Baz record {
    float f = 2.0;
};

function getFoo() returns Foo? {
    return { bar: new };
}

function getBaz() returns Baz? {
    return { f: 100.0 };
}

function testInvalidFieldAccessOnInvocation1() {
    int ri = getFoo().bar.i;

    Foo fv = { bar: new };
    float rf = fv.bazFunc().f;
}

function testInvalidFieldAccessOnInvocation2() {
    _ = getNonFieldAccessibleValue().f;
}

function getNonFieldAccessibleValue() returns Foo[] {
    return [];
}
