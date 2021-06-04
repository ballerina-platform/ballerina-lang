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
    table<record {| int id; |}> key(id) a3 = a2;
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

type Row record {
  readonly int k;
  readonly string[] v;
  string m;
};

int id1 = 15;

function testAssignVariableRefAsKey() {
    int id2 = 16;
    table<Row> key(k) tbl1 = table [
        {k: id1, v:["Foo", "Bar"], m: "A"},
        {k: id2, v:["Bam", "Boom"], m: "B"}
    ];
    int id3 = 17;
    tbl1.add({k: id3, v:["Foo", "Boom"], m: "C"});

    var tbl2 = table key(k) [
        {k: id1, v:["Foo", "Bar"], m: "A"},
        {k: id2, v:["Bam", "Boom"], m: "B"},
        {k: id3, v:["Foo", "Boom"], m: "C"}
    ];
    assertEqual(tbl1, tbl2);

    readonly & string[] s1 = ["foo", "bar"];
    table<Row> key(k, v) tbl3 = table [
        {k: id1, v:s1, m: "A"},
        {k: id2, v:s1, m: "B"}
    ];

    Row row = {k: 16, v:["foo", "bar"], m: "B"};
    assertEqual(row, tbl3.get([16, ["foo", "bar"]]));

    error? err = trap tbl3.add({k: id1, v:s1, m: "A"});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"A value found for key '15 [\"foo\",\"bar\"]'"};
        assertEqual(msg, err.detail());
    }

    table<Row> tbl4 = table [
        {k: id1, v:s1, m: "A"},
        {k: id2, v:s1, m: "B"}
    ];
    assertEqual(table [{k: 15, v:["foo", "bar"], m: "A"}, {k: 16, v:["foo", "bar"], m: "B"}], tbl4);
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
