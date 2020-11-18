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

const ASSERTION_ERROR_REASON = "AssertionError";

type Employee record {
    string name;
    Employee? employer = ();
    int id;
};

type Person record {
    string name;
    Person? employer = ();
};

function testRecordFieldAccess1() returns boolean {
    string s = "Anne";
    Employee e = { name: s, id: 100 };
    return s == e.name;
}

function testRecordFieldAccess2() returns boolean {
    string s = "Anne";
    Employee e = { name: s, id: 1001 };
    Employee|Person ep = e;
    string name = ep.name;
    Employee|Person? employer = ep.employer;
    return s == name && employer == ();
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

function testRecordFieldAccess3() returns boolean {
    string s1 = "John";
    string s2 = "ASD123";
    PersonTwo e = { name: s1, id: s2, salary: 100.0 };
    EmployeeTwo|PersonTwo ep = e;
    string name = ep.name;
    string|int id = ep.id;
    return name == s1 && id == s2;
}

int i = 12;
json j1 = { a: { b: i } };
map<json> j2 = { a: { b: i } };

function testJsonFieldAccessPositive() returns boolean[2] {
    return [testJsonFieldAccessPositive1(j1), testJsonFieldAccessPositive2(j1)];
}

function testJsonFieldAccessNegative() returns boolean[5] {
    return [testNonMappingJsonFieldAccessNegative1(j1), testNonMappingJsonFieldAccessNegative2(j1),
            testJsonFieldAccessNegativeMissingKey1(j1), testJsonFieldAccessNegativeMissingKey2(j1),
            testJsonFieldAccessNegativeMissingKey3(j1)];
}

function testJsonFieldAccessPositive1(json j) returns boolean {
    json be = { b: i };
    json|error a = j.a;
    return a is json && a == be;
}

function testJsonFieldAccessPositive2(json j) returns boolean {
    json|error b = j.a.b;
    return b is json && b == i;
}

function testNonMappingJsonFieldAccessNegative1(json j) returns boolean {
    json|error a = j.a.b.c;
    return assertNonMappingJsonError(a);
}

function testNonMappingJsonFieldAccessNegative2(json j) returns boolean {
    json|error a = j.a.b.c.d;
    return assertNonMappingJsonError(a);
}

function testJsonFieldAccessNegativeMissingKey1(json j) returns boolean {
    json|error a = j.a.d;
    return assertKeyNotFoundError(a, "d");
}

function testJsonFieldAccessNegativeMissingKey2(json j) returns boolean {
    json|error a = j.e;
    return assertKeyNotFoundError(a, "e");
}

function testJsonFieldAccessNegativeMissingKey3(json j) returns boolean {
    json|error a = j.e.f;
    return assertKeyNotFoundError(a, "e");
}

function testMapJsonFieldAccessPositive() returns boolean[2] {
    return [testMapJsonFieldAccessPositive1(j2), testMapJsonFieldAccessPositive2(j2)];
}

function testMapJsonFieldAccessNegative() returns boolean[5] {
    return [testNonMappingMapJsonFieldAccessNegative1(j2), testNonMappingMapJsonFieldAccessNegative2(j2),
            testMapJsonFieldAccessNegativeMissingKey1(j2), testMapJsonFieldAccessNegativeMissingKey2(j2),
            testMapJsonFieldAccessNegativeMissingKey3(j2)];
}

function testMapJsonFieldAccessPositive1(map<json> j) returns boolean {
    json be = { b: i };
    json|error a = j.a;
    return a is json && a == be;
}

function testMapJsonFieldAccessPositive2(map<json> j) returns boolean {
    json|error b = j.a.b;
    return b is json && b == i;
}

function testNonMappingMapJsonFieldAccessNegative1(map<json> j) returns boolean {
    json|error a = j.a.b.c;
    return assertNonMappingJsonError(a);
}

function testNonMappingMapJsonFieldAccessNegative2(map<json> j) returns boolean {
    json|error a = j.a.b.c.d;
    return assertNonMappingJsonError(a);
}

function testMapJsonFieldAccessNegativeMissingKey1(map<json> j) returns boolean {
    json|error a = j.a.d;
    return assertKeyNotFoundError(a, "d");
}

function testMapJsonFieldAccessNegativeMissingKey2(map<json> j) returns boolean {
    json|error a = j.e;
    return assertKeyNotFoundError(a, "e");
}

function testMapJsonFieldAccessNegativeMissingKey3(map<json> j) returns boolean {
    json|error a = j.e.f;
    return assertKeyNotFoundError(a, "e");
}

function testNonNilLiftingJsonAccess1() returns boolean {
    json a = ();
    json|error b = a.one;
    return assertNonMappingJsonError(b);
}

function testNonNilLiftingJsonAccess2() returns boolean {
    json a = ();
    json|error b = a.one.two;
    return assertNonMappingJsonError(b);
}

function testNonNilLiftingJsonAccess3() returns boolean {
    map<json> a = { one: (), two: "two", three: { j: "mapping" } };
    json|error b = a.one.two;
    return assertNonMappingJsonError(b);
}

function testLaxUnionFieldAccessPositive() returns boolean {
    map<map<json>> m = { a: { b: i }, c: { d: "string value" } };
    map<map<json>>|json mj = m;
    json|error jv = mj.a.b;
    return jv == i;
}

function testLaxUnionFieldAccessNegative1() returns boolean {
    map<map<json>> m = { a: { b: i }, c: { d: "string value" } };
    map<map<json>>|json mj = m;
    json|error jv = mj.a.b.c;
    return assertNonMappingJsonError(jv);
}

function testLaxUnionFieldAccessNegative2() returns boolean {
    map<map<json>> m = { a: { b: i }, c: { d: "string value" } };
    map<map<json>>|json mj = m;
    json|error jv = mj.a.e;
    return assertKeyNotFoundError(jv, "e");
}

function testLaxUnionFieldAccessNegative3() returns boolean {
    map<map<json>> m = { a: { b: i }, c: { d: "string value" } };
    map<map<json>>|json mj = m;
    json|error jv = mj.e;
    return assertKeyNotFoundError(jv, "e");
}

function assertNonMappingJsonError(json|error je) returns boolean {
    if (je is error) {
        return je.message() == "{ballerina}JSONOperationError" && je.detail()["message"].toString() == "JSON value is not a mapping";
    }
    return false;
}

function assertKeyNotFoundError(json|error je, string key) returns boolean {
    if (je is error) {
        return je.message() == "{ballerina/lang.map}KeyNotFound" &&
                                je.detail()["message"].toString() == "Key '" + key + "' not found in JSON mapping";
    }
    return false;
}

function testMapJsonFieldAccessTypePositive1() returns boolean {
    map<map<json>> m1 = { a: { b: 1 }, c: { d: 2, e: "hello" } };
    map<map<json>>|map<map<map<json>>> m2 = m1;
    map<json>|error jv = m2.a;
    return jv is map<json> && jv == <map<int>> { b: 1 };
}

function testMapJsonFieldAccessTypePositive2() returns boolean {
    map<map<json>> m1 = { a: { b: 1 }, c: { d: 2, e: "hello" } };
    map<map<json>>|map<map<map<json>>> m2 = m1;
    json|error jv = m2.a.b;
    return jv is json && jv == 1;
}

type Foo record {
    Bar bar;
    function() returns Baz bazFunc = getBaz;
};

class Bar {
    int i;

    public function init(int i) {
        self.i = i;
    }
}

type Baz record {
    float f = 2.0;
};

function getFoo() returns Foo {
    return { bar: new(10) };
}

function getBaz() returns Baz {
    return { f: 100.0 };
}

function testFieldAccessOnInvocation() returns boolean {
    int ri = getFoo().bar.i;
    float rf = getFoo().bazFunc().f;

    return ri == 10 && rf == 100.0;
}

function testJsonFieldAccessOnInvocation() returns boolean {
    json|error v = getJson().x.y;
    return v == 1;
}

function getJson() returns json {
    return { x: { y: 1 } };
}
