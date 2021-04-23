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
            return r["z2"];
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
    // https://github.com/ballerina-platform/ballerina-lang/issues/30140
    // assertEquals(0, m4["m"]);
    assertEquals("world", m4["q"]);
    assertEquals(2, m4.length());

    int|[anydata, map<anydata>]? r5 = mappingBindingPatternRest9({"m": 0});
    assertEquals((), r5);

    int|[anydata, map<anydata>]? r6 = mappingBindingPatternRest9({"m": 10, "p": "hello", "q": "world"});
    assertEquals(true, r6 is [anydata, map<anydata>]);
    var v6 = <[anydata, map<anydata>]> r6;
    assertEquals("hello", v6[0]);
    map<anydata> m6 = v6[1];
    assertEquals(10, m6["m"]);
    assertEquals("world", m6["q"]);
    // https://github.com/ballerina-platform/ballerina-lang/issues/30140
    // assertEquals(2, m6.length());

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
        {id: var x, ...var rest} => {
            return [x, rest];
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

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
