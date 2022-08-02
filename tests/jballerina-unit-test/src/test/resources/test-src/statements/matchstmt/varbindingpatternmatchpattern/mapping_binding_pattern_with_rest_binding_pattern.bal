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

import ballerina/test;

function mappingBindingPatternRest1(any v) returns any|error {
    match v {
        var {w: a, x: b, y: c,  ...r} => {
            return <anydata> checkpanic r["z"];
        }
        var {x: a, y: b, ...r} => {
            return <anydata> checkpanic r["d"];
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest1() {
    assertEquals(3, <anydata> checkpanic mappingBindingPatternRest1({x: 2, y: 3, z: 3, w: 4}));
    assertEquals(1, <anydata> checkpanic mappingBindingPatternRest1({d: 1, x: 2, y: 3, z: "3"}));
    assertEquals("No match", <anydata> checkpanic mappingBindingPatternRest1({d: 3, y: 3, z: 3, w: 4}));
}

function mappingBindingPatternRest2(record { int x; int y; int z1; int z2; } v) returns anydata {
    match v {
        var {x: a, y: b, z1: c, ...r} => {
            return r.z2;
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest2() {
    assertEquals(22, mappingBindingPatternRest2({x: 2, y: 3, z1: 5, z2: 22}));
}

function mappingBindingPatternRest3(map<int> v) returns anydata {
    match v {
        var {x: a, y: b, z1: c, ...r} => {
            return r["z2"];
        }
        var {x: a, y: b, ...r} => {
            return r["z2"];
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest3() {
    assertEquals(22, mappingBindingPatternRest3({x: 2, y: 3, z1: 5, z2: 22}));
    assertEquals(25, mappingBindingPatternRest3({x : 2, y: 3, z3: 6, z2: 25}));
    assertEquals("No match", mappingBindingPatternRest3({w: 2, y: 2, z1: 6, z2: 22}));
}

function mappingBindingPatternRest4(anydata v) returns anydata {
    match v {
        var {x: {y: a, ...r}} => {
            return r["z"];
        }
    }
    return "";
}

function testMappingBindingPatternWithRest4() {
    assertEquals("z", mappingBindingPatternRest4({x: {y: 1, z: "z" }}));
}

function mappingBindingPatternRest5(json j) returns map<json> {
    match j {
        var {x: x, ...y} => {
            y["val"] = x;
            return y;
        }
    }
    return {};
}

function testMappingBindingPatternWithRest5() {
    assertEquals({val: "hello"}, mappingBindingPatternRest5({x: "hello"}));
    assertEquals({val: 1, y: "hello world"}, mappingBindingPatternRest5({y: "hello world", x: 1}));
    assertEquals({y: "hello world", val: ()}, mappingBindingPatternRest5({y: "hello world", x: ()}));
    assertEquals({}, mappingBindingPatternRest5({a: "hello world", x1: 1}));
    assertEquals({}, mappingBindingPatternRest5({}));
    assertEquals({}, mappingBindingPatternRest5(1));
}

type RecOne record {| int a; boolean b; string c; |};
type RecTwo record {| int m; string...; |};
type RecThree record { int n?; };

function mappingBindingPatternRest6(RecOne rec) returns [int, boolean, string, map<never>]? {
    match rec {
        var {a, b, c, ...d} => {
            int a2 = a;
            boolean b2 = b;
            string c2 = c;
            map<never> d2 = d;
            return [a2, b2, c2, d2];
        }
    }
}

function mappingBindingPatternRest7(RecOne rec) returns [int, map<boolean|string>]? {
    match rec {
        var {a, ...d} => {
            return [a, d];
        }
    }
}

function mappingBindingPatternRest8(RecTwo rec) returns [string, map<int|string>]? {
    match rec {
        var {p, ...q} => {
            return [p, q];
        }
    }
}

function mappingBindingPatternRest9(RecThree rec) returns int|[anydata, map<anydata>]? {
    match rec {
        var {p, ...q} => {
            return [p, q];
        }

        var {n, ...q} => {
            map<anydata> mp = q;
            return n + q.length();
        }
    }
}

function testMappingBindingPatternWithRest6() {
    RecOne rec1 = {a: 123, b: true, c: "hello"};
    [int, boolean, string, map<never>]? r1 = mappingBindingPatternRest6(rec1);
    assertEquals(true, r1 is [int, boolean, string, map<never>]);
    var v1 = <[int, boolean, string, map<never>]> r1;
    assertEquals(123, v1[0]);
    assertEquals(true, v1[1]);
    assertEquals("hello", v1[2]);
    assertEquals(0, v1[3].length());

    [int, map<boolean|string>]? r2 = mappingBindingPatternRest7(rec1);
    assertEquals(true, r2 is [int, map<boolean|string>]);
    var v2 = <[int, map<boolean|string>]> r2;
    assertEquals(123, v2[0]);
    map<boolean|string> m2 = v2[1];
    assertEquals(true, m2 is record {|never a?; boolean b; string c;|});
    assertEquals(true, m2["b"]);
    assertEquals("hello", m2["c"]);
    assertEquals(2, m2.length());

    [string, map<int|string>]? r3 = mappingBindingPatternRest8({m: 0});
    assertEquals((), r3);

    [string, map<int|string>]? r4 = mappingBindingPatternRest8({m: 0, "p": "hello", "q": "world"});
    assertEquals(true, r4 is [string, map<int|string>]);
    var v4 = <[string, map<int|string>]> r4;
    assertEquals("hello", v4[0]);
    map<int|string> m4 = v4[1];
    assertEquals(true, m4 is record {|never p?; int m; string...;|});
    record {|never p?; int m; string...;|} resRec = <record {|never p?; int m; string...;|}>m4;
    assertEquals(0, resRec.m);
    assertEquals("world", m4["q"]);
    assertEquals(2, m4.length());

    int|[anydata, map<anydata>]? r5 = mappingBindingPatternRest9({"m": 0});
    assertEquals((), r5);

    int|[anydata, map<anydata>]? r6 = mappingBindingPatternRest9({"m": 10, "p": "hello", "q": "world"});
    assertEquals(true, r6 is [anydata, map<anydata>]);
    var v6 = <[anydata, map<anydata>]> r6;
    assertEquals("hello", v6[0]);
    map<anydata> m6 = v6[1];
    assertEquals(true, m6 is record {|never p?; int n?; anydata...;|});
    assertEquals(10, m6["m"]);
    assertEquals("world", m6["q"]);
    assertEquals(2, m6.length());

    int|[anydata, map<anydata>]? r7 = mappingBindingPatternRest9({n: 120, "q": "world"});
    assertEquals(121, r7);
}

type Person record {|
    int id;
    string name;
    boolean employed;
|};

function mappingBindingPatternRest10(Person person) returns anydata {
    match person {
        var {id, ...rest} => {
            return [id, rest];
        }
    }
    return "";
}

function testMappingBindingPatternWithRest7() {
    anydata a = mappingBindingPatternRest10({id: 12, name: "May", employed: true});
    assertEquals(true, a is anydata[]);
    anydata[] b = <anydata[]> a;
    assertEquals(12, b[0]);
    map<anydata> mp = <map<anydata>> b[1];
    assertEquals(2, mp.length());
    assertEquals(true, mp["employed"]);
    assertEquals("May", mp["name"]);
}

type ClosedRecordWithOneField record {|
    int i;
|};

type EmptyClosedRecord record {|
|};

function mappingBindingPatternRest11(ClosedRecordWithOneField|EmptyClosedRecord rec) returns anydata {
    match rec {
        var {i, ...rest} => {
            int m = i;
            assertEquals(true, rest is record {| never i?; never...;|});
            map<never> n = rest;
            return <int[2]> [m, n.length()];
        }
        var {...rest} => {
            assertEquals(true, rest is record {| int i?; never...;|});
            map<int|never> n = rest;
            return n.length();
        }
    }
}

function testMappingBindingPatternWithRest8() {
    anydata a = mappingBindingPatternRest11({i: 12});
    assertEquals(true, a is int[2]);
    int[2] b = <int[2]> a;
    assertEquals(12, b[0]);
    assertEquals(0, b[1]);

    anydata c = mappingBindingPatternRest11({});
    assertEquals(0, c);
}

function mappingBindingPatternRest12(RecTwo rec)
        returns [string, map<int|string>]|[string, string, map<int|string>]|[int, map<string>]? {
    match rec {
        var {p, ...q} => {
            return [p, q];
        }
        var {a, b, ...c} => {
         return [a, b, c];
        }
        var {m, ...n} => {
            return [m, n];
        }
    }
}

public function testRestMappingAtRuntime() {
    RecTwo rec = {"p": "hello", m: 101, "q": "world"};
    var r1 = mappingBindingPatternRest12(rec);
    assertEquals(true, r1 is [string, map<int|string>]);
    var v1 = <[string, map<int|string>]> r1;
    assertEquals("hello", v1[0]);
    var m1 = v1[1];
    assertEquals(true, m1 is record{|never p?; int|string...;|});
    assertEquals(101, m1["m"]);
    assertEquals("world", m1["q"]);
    assertEquals(2, m1.length());
    assertEquals(<RecTwo> {m: 101, "p": "hello", "q": "world"}, rec);

    RecTwo rec2 = {m: 202, "a": "hello", "b": "world", "c": "ballerina"};
    var r2 = mappingBindingPatternRest12(rec2);
    assertEquals(true, r2 is [string, string, map<int|string>]);
    var v2 = <[string, string, map<int|string>]> r2;
    assertEquals("hello", v2[0]);
    assertEquals("world", v2[1]);
    var m2 = v2[2];
    assertEquals("ballerina", m2["c"]);
    assertEquals(202, m2["m"]);
    assertEquals(2, m2.length());
    assertEquals(<RecTwo> {m: 202, "a": "hello", "b": "world", "c": "ballerina"}, rec2);

    RecTwo rec3 = {m: 303, "b": "ballerina"};
    var r3 = mappingBindingPatternRest12(rec3);
    assertEquals(true, r3 is [int, map<string>]);
    var v3 = <[int, map<string>]> r3;
    assertEquals(303, v3[0]);
    var m3 = v3[1];
    assertEquals(true, m3 is record{|never m?; string...;|});
    assertEquals("ballerina", m3["b"]);
    assertEquals(1, m3.length());
    assertEquals(<RecTwo> {m: 303, "b": "ballerina"}, rec3);
}

type PersonA record {|
    int id;
    string name;
    boolean employed;
|};

type PersonB record {|
    string...;
|};

function testRestRecordPattern() {
    Person p1 = {id: 20, name: "Jane Doe", employed: false};

    match p1 {
        {name: var s, ...var rest} => {
            assertEquals(true, rest is record{|never name?; int id; boolean employed; |});
            assertEquals(20, rest.id);
            assertEquals(false, rest.employed);
        }
    }

    match p1 {
         {...var rest} => {
             assertEquals(true, rest is record{|int id; string name; boolean employed;|});
             assertEquals("Jane Doe", rest.name);
             assertEquals(20, rest.id);
             assertEquals(false, rest.employed);
         }
    }

    PersonA|PersonB p2 = {id: 10, name: "Jone Doe", employed: true};

    match p2 {
         var {...rest} => {
             assertEquals(true, rest is record{|int id?; string name?; boolean employed?; (never|string)...;|});
             assertEquals("Jone Doe", rest?.name);
             assertEquals(10, rest?.id);
             assertEquals(true, rest?.employed);
         }
     }
}

type PersonClosed record {|
    int id;
    string name;
    boolean aged;
|};

type PersonOpen record {
    int id;
    string name;
    boolean aged;
};

function testRestBindingPatternWithRecords() {
    PersonOpen person1 = {id: 456, name: "yourName", aged: false, "address": "yourAddress"};
    PersonOpen {id: personId1, ...otherDetails1} = person1;
    test:assertEquals(personId1, 456);
    test:assertEquals(otherDetails1, {"name":"yourName","aged":false,"address":"yourAddress"});
    test:assertTrue(otherDetails1 is record {| never id?; string name; boolean aged; anydata...; |});

    PersonClosed person2 = {id: 123, name: "myName", aged: true};
    PersonClosed {id: personId2, ...otherDetails2} = person2;
    test:assertEquals(personId2, 123);
    test:assertEquals(otherDetails2, {"name":"myName","aged":true});
    test:assertTrue(otherDetails2 is record {| never id?; string name; boolean aged; |});
}

type Record record {| int m; string...; |};

function testReachableMappingBinding() {
    Record rec = {m: 1, "a": "foo", "b": "bar"};
    string result = "";
    match rec {
        var {p, ...q} => {
            result = "Match1";
        }
        var {a, b, ...c} => {
            result = "Match2";
        }
    }
    assertEquals("Match2", result);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
