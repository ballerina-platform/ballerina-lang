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

type Student record {
    readonly string name;
    int id;
};

function testAssignKeyedTableValueToAnydata() {
    table<Student> key(name) tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    anydata a1 = tbl1;
    anydata a2 = table key(name) [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(isEqual(table key(name) [{name: "Amy", id: 1234}, {name: "John", id: 4567}], a2), true);
}

function isEqual(anydata tabl1, anydata table2) returns boolean {
    return tabl1 == table2;
}

function testAssignKeyedTableValueToAny() {
    table<Student> key(name) tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    any a1 = tbl1;
    any a2 = table key(name) [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(table key(name) [{name: "Amy", id: 1234}, {name: "John", id: 4567}], a2);
}

function testAssignKeyedTableValueToVar() {
    table<Student> key(name) tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var a1 = tbl1;
    var a2 = table key(name) [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var ids = from var {id} in tbl1 select {id};
    table<record {| int id; |}> a3 = ids;

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(tbl1, a2);
    assertEqual(table [{"id": 1234}, {"id": 4567}], a3);
}

function testAssignKeyedTableValueToTableType() {
    table<Student> key(name) tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var a2 = checkpanic table key(id) from var {id} in tbl1 select {id};
    table<record {| readonly int id; |}> key(id) a3 = a2;
    any a4 = table key(id) [{"id": 1234}, {"id": 4567}];

    assertEqual(table key(id) [{"id": 1234}, {"id": 4567}], a3);
    assertEqual(a3, a4);
}

function testAssignKeylessTableValueToAnydata() {
    table<Student> tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    anydata a1 = tbl1;
    anydata a2 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(isEqual(table [{name: "Amy", id: 1234}, {name: "John", id: 4567}], a2), true);
}

function testAssignKeylessTableValueToAny() {
    table<Student> tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    any a1 = tbl1;
    any a2 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(table [{name: "Amy", id: 1234}, {name: "John", id: 4567}], a2);
}

function testAssignKeylessTableValueToVar() {
    table<Student> tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var a1 = tbl1;
    var a2 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var ids = from var {id} in tbl1 select {id};

    assertEqual(a1, a2);
    assertEqual(tbl1, a2);
    assertEqual(tbl1, a2);
    assertEqual(table [{"id": 1234}, {"id": 4567}], ids);
}

function testAssignKeylessTableValueToTableType() {
    table<Student> tbl1 = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var a2 = table key() from var {id} in tbl1 select {id};
    table<record {| int id; |}> a3 = a2;
    any a4 = table [{"id": 1234}, {"id": 4567}];

    assertEqual(table [{"id": 1234}, {"id": 4567}], a3);
    assertEqual(a3, a4);
}

function assertEqual(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected.toString();
    string actualValAsString = actual.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
