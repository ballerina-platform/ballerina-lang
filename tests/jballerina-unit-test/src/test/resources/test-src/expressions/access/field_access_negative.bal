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
    int? id?;
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

type R1 record {|
    int x;
|};

type R2 record {|
    int y;
    R1 r1;
|};

function testUndeclared(R2 r2) {
    int a = r2.r1.a;
}

type R3 record {|
    int x;
    int? y?;
|};

function testOptionalInClosedRecord(R3 r) {
    int a = r.y;
}

type R4 record {
    int x;
    int? y?;
};

function testOptionalInOpenRecord(R4 r) {
    int a = r.y;
}

type R5 record {
    int x;
};

function testRestDeclaredInRuntime() {
    R5 r = {x:1, "y":2};
    anydata a = r.y;
}

type R6 record {
    int x;
};

function testUndeclaredInOpenRecord() {
    R6 r = {x:1};
    anydata a = r.y;
}

type R7 record {|
    int x;
    int|string...;
|};


function exclusiveRecordTypeWithRestField() {
    R7 r = {x:1};
    anydata a = r.y;
}

type RA record {
    int a;
    int b;
    int c;
    int x;
    int y;
    int z;
};

type SA record {
    int? a?;
    int? b?;
    int c;
    int z;
};

type TA record {|
    int a;
    int b;
    int? c?;
    int x;
    int y;
|};

type UA record {
    int? a?;
    int? b?;
    int c;
    int z;
};

type VA record {|
    int? a?;
    int b;
    int c;
    int y;
    int z;
|};

function testUndeclaredFieldInUnion() {
    RA r = {a: 1, b: 2, c: 3, x: 4, y: 5, z: 6};
    (RA|SA|TA|UA|VA) rstuv = r;
    anydata a = rstuv.a;
    anydata b = rstuv.b;
    anydata c = rstuv.c;

    anydata x = rstuv.x;
    anydata y = rstuv.y;
    anydata z = rstuv.z;
}

type PB record {
    int x;
    int y;
    string z;
};

type QB record {
    int x?;
    string z?;
};

type RB record {
    int y?;
};

type SB record {
    int x?;
    int y;
};

type TB record {
    string z?;
};

type UB record {
    int y?;
    string z;
};

type VB record {
    int x?;
    int y?;
};

function testUndeclaredAndOptional() {
    PB r = {x: 5, y: 6, z: "test"};
    (PB|QB|RB|SB|TB|UB|VB) pqrstuv = r;
    anydata x = pqrstuv.x;
    anydata y = pqrstuv.y;
    anydata z = pqrstuv.z;
}

type FooOne record {|
    int i?;
|};

type BarOne record {|
    int? i;
|};

function testNilableAndOptional() {
    FooOne|BarOne val = <BarOne> {i: 5};
    int? i = val.i;
}

type AB record {
    int x;
    int? y?;
    string z;
};

type BC record {
    int? x?;
    string? z?;
};

type CD record {
    int y?;
};

function testUndeclaredAndOptionalAndNilable() {
    AB r = {x: 5, y: 6, z: "test"};
    (AB|BC|CD) abcd = r;
    anydata x = abcd.x;
    anydata y = abcd.y;
    anydata z = abcd.z;
}
