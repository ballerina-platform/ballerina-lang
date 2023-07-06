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

function mappingMatchPattern1(any v) returns any|error {
    match v {
        {w: 1, x: 2, y: 3,  ...var a} => {
            assertEquals(true, a is record {| never w?; never x?; never y?; (any|error)...; |});
            return a["z"];
        }
        {x: 2, y: 3, ...var a} => {
            assertEquals(true, a is record {| never x?; never y?; (any|error)...; |});
            return a["z"];
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern1() {
    assertEquals(3, <anydata> checkpanic mappingMatchPattern1({x: 2, y: 3, "z": 3, w: 4}));
    assertEquals("3", <anydata> checkpanic mappingMatchPattern1({w: 1, x: 2, y: 3, "z": "3"}));
    assertEquals("No match", <anydata> checkpanic mappingMatchPattern1({x: 3, y: 3, "z": 3, w: 4}));
}

function mappingMatchPattern2(record { int x; int y; int z1; int z2; } v) returns anydata {
    match v {
        {x: 2, y: 3, z1: 5, ...var a} => {
            assertEquals(true, a is record {| never x?; never y?; never z1?; (any|error)...; |});
            return a["z2"];
        }
        {x: 2, y: 3, ...var a} => {
            assertEquals(true, a is record {| never x?; never y?; (any|error)...; |});
            return a["z2"];
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern2() {
    assertEquals(22, mappingMatchPattern2({x: 2, y: 3, z1: 5, z2: 22}));
    assertEquals(22, mappingMatchPattern2({x: 2, y: 3, z1: 6, z2: 22}));
    assertEquals("No match", mappingMatchPattern2({x: 2, y: 2, z1: 6, z2: 22}));
}

function mappingMatchPattern3(map<int> v) returns anydata {
    match v {
        {x: 2, y: 3, z1: 5, ...var a} => {
            return a["z2"];
        }
        {x: 2, y: 3, ...var a} => {
            return a["z2"];
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern3() {
    assertEquals(22, mappingMatchPattern3({x: 2, y: 3, z1: 5, z2: 22}));
    assertEquals(22, mappingMatchPattern3({x: 2, y: 3, z1: 6, z2: 22}));
    assertEquals("No match", mappingMatchPattern3({x: 2, y: 2, z1: 6, z2: 22}));
}

function mappingMatchPattern4(record {|int a; int b; string...;|} v) returns (string|int)? {
    match v {
        {a: 2, ...var rst} => {
            assertEquals(true, rst is record {| never a?; int b; string...; |});
            return rst["c"];
        }
    }
    return "No match";
}

function testMappingMatchPattern4() {
    assertEquals("34", mappingMatchPattern4({a: 2, b: 3, "c": "34"}));
}

map<int> globalVar = {};
function mappingMatchPattern5() returns int? {
    map<int> m = {a: 1, b: 2, c: 3};
    match m {
        {a: 1, ...var globalVar} => {
            return globalVar["b"];
        }
        _ => {
            return -1;
        }
    }
}

function testMappingMatchPattern5() {
    assertEquals(2, mappingMatchPattern5());
}


function mappingMatchPattern6(map<int>|map<json> m) returns json|int {
    match m {
        {a: "foo", ...var x}|{a: 1, ...var x} => {
            return x["b"];
        }
        _ => {
            return -1;
        }
    }
}

function testMappingMatchPattern6() {
    assertEquals(2, mappingMatchPattern6({a: "foo", b: 2, c: "3"}));
    assertEquals("20", mappingMatchPattern6({a: 1, b: "20", c: "3"}));
    assertEquals(-1, mappingMatchPattern6({a: 10, b: "20", c: "3"}));
}

function mappingMatchPattern7(map<int>|map<json> m) returns json|int {
    match m {
        {a: "foo", b: 2, ...var x}|{a: 1, ...var x} => {
            return x["c"];
        }
        _ => {
            return -1;
        }
    }
}

function testMappingMatchPattern7() {
    assertEquals("3", mappingMatchPattern7({a: "foo", b: 2, c: "3"}));
    assertEquals(30, mappingMatchPattern7({a: 1, b: "2", c: 30}));
    assertEquals(-1, mappingMatchPattern7({a: 10, b: "20", c: "3"}));
}

function mappingMatchPattern8(any v) returns anydata {
    match v {
        {x: {y: 1, ...var a}} => {
            return <anydata> checkpanic a["z"];
        }
    }
    return "";
}

function testMappingMatchPattern8() {
    assertEquals("z", mappingMatchPattern8({x: {y: 1, z: "z"}}));
}

function mappingMatchPattern9(anydata a) returns anydata {
    match a {
        {x: var p, ...var oth} if p is map<any> => {
            map<anydata> m = p;
            return m;
        }
        _ => {
            return "other";
        }
    }
}

function mappingMatchPattern10(map<map<int|error>> a) returns [map<int>, map<map<int|error>>]|string {
    match a {
        {x: var p, ...var oth} if p is anydata => {
            map<int> m = p;
            map<map<int|error>> n = oth;
            return [m, n];
        }
        _ => {
            return "other";
        }
    }
}

function testMappingMatchPatternWithMapAndAnydataIntersection() {
    map<int> m1 = {a: 1, b: 2};
    map<anydata> m2 = {x: m1, y: "hello"};
    assertEquals(m1, mappingMatchPattern9(m2));
    map<anydata> m3 = {a: 1, x: "foo"};
    assertEquals("other", mappingMatchPattern9(m3));
    assertEquals("other", mappingMatchPattern9("foo"));

    map<int|error> m4 = {a: 1, b: error("error!")};
    [map<int>, map<map<int|error>>] res = <[map<int>, map<map<int|error>>]> mappingMatchPattern10({x: m1, y: m4});
    assertEquals(m1, res[0]);
    assertEquals(true, m4 === res[1]["y"]);
    assertEquals(1, res[1].length());
    assertEquals("other", <string> mappingMatchPattern10({x: {a: error("error!")}}));
    assertEquals("other", <string> mappingMatchPattern10({}));
}

function mappingMatchPatternWithRestPattern11(json j) returns map<json> {
    match j {
        {x: var x, ...var y} => {
            y["val"] = x;
            return y;
        }
    }
    return {};
}

function testMappingMatchPatternWithRestPattern11() {
    assertEquals({val: "hello"}, mappingMatchPatternWithRestPattern11({x: "hello"}));
    assertEquals({val: 1, y: "hello world"}, mappingMatchPatternWithRestPattern11({y: "hello world", x: 1}));
    assertEquals({y: "hello world", val: ()}, mappingMatchPatternWithRestPattern11({y: "hello world", x: ()}));
    assertEquals({}, mappingMatchPatternWithRestPattern11({a: "hello world", x1: 1}));
    assertEquals({}, mappingMatchPatternWithRestPattern11({}));
    assertEquals({}, mappingMatchPatternWithRestPattern11(1));
}

type Person record {|
    int id;
    string name;
    boolean employed;
|};

function mappingMatchPattern12(Person person) returns anydata {
    match person {
        {id: var x, ...var rest} => {
            return [x, rest];
        }
    }
    return "";
}

function testMappingMatchPatternWithClosedRecord() {
    anydata a = mappingMatchPattern12({id: 12, name: "May", employed: true});
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

function mappingMatchPattern13(ClosedRecordWithOneField|EmptyClosedRecord rec) returns anydata {
    match rec {
        {i: var x, ...var rest} => {
            int m = x;
            map<never> n = rest;
            return <int[2]> [m, n.length()];
        }
        {...var rest} => {
            assertEquals(true, rest is record {|int i?; never...;|});
            map<int|never> n = rest;
            return n.length();
        }
    }
}

function testMappingMatchPatternWithClosedRecordUnion() {
    anydata a = mappingMatchPattern13({i: 12});
    assertEquals(true, a is int[2]);
    int[2] b = <int[2]> a;
    assertEquals(12, b[0]);
    assertEquals(0, b[1]);

    anydata c = mappingMatchPattern13({});
    assertEquals(0, c);
}

type RecTwo record {| int m; string...; |};

function mappingMatchPattern14(RecTwo rec)
        returns [string, map<int|string>]|[string, string, map<int|string>]|[int, map<string>]? {
    match rec {
        {p: var p, ...var q} => {
            return [p, q];
        }
        {a: var a, b: var b, ...var c} => {
         return [a, b, c];
        }
        {m: var m, ...var n} => {
            return [m, n];
        }
    }
}

public function testRestMappingAtRuntime() {
    RecTwo rec = {"p": "hello", m: 101, "q": "world"};
    var r1 = mappingMatchPattern14(rec);
    assertEquals(true, r1 is [string, map<int|string>]);
    var v1 = <[string, map<int|string>]> r1;
    assertEquals("hello", v1[0]);
    var m1 = v1[1];
    assertEquals(true, m1 is record {| never p?; int m; string...;|});
    var rec1 = <record {| never p?; int m; string...;|}>m1;
    assertEquals(101, rec1.m);
    assertEquals("world", m1["q"]);
    assertEquals(2, m1.length());
    assertEquals(<RecTwo> {m: 101, "p": "hello", "q": "world"}, rec);

    RecTwo rec2 = {m: 202, "a": "hello", "b": "world", "c": "ballerina"};
    var r2 = mappingMatchPattern14(rec2);
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
    var r3 = mappingMatchPattern14(rec3);
    assertEquals(true, r3 is [int, map<string>]);
    var v3 = <[int, map<string>]> r3;
    assertEquals(303, v3[0]);
    var m3 = v3[1];
    assertEquals(true, m3 is record {| never m?; string...;|});
    assertEquals("ballerina", m3["b"]);
    assertEquals(1, m3.length());
    assertEquals(<RecTwo> {m: 303, "b": "ballerina"}, rec3);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
