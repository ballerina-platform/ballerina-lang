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

function mappingBindingPattern1(any v) returns string {
    match v {
        var {a: x} if x is string => {
            return "match1";
        }
        var {a: x} if x is int  => {
            return "match2";
        }
        var {a: x} if x is boolean => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingBindingPattern1() {
    assertEquals("match1", mappingBindingPattern1({a: "str"}));
    assertEquals("match1", mappingBindingPattern1({a: "str", b : "D"}));
    assertEquals("match2", mappingBindingPattern1({a: 1}));
    assertEquals("match3", mappingBindingPattern1({a: true}));
    assertEquals("No match", mappingBindingPattern1({b: true}));
}

function mappingBindingPattern2(any v) returns string|int|boolean {
    match v {
        var {a: x} if x is string => {
            return x;
        }
        var {a: x} if x is int  => {
            return x;
        }
        var {a: x} if x is boolean => {
            return x;
        }
    }
    return "No match";
}

function testMappingBindingPattern2() {
    assertEquals("str", mappingBindingPattern2({a: "str"}));
    assertEquals("str", mappingBindingPattern2({a: "str", b : "D"}));
    assertEquals(1, mappingBindingPattern2({a: 1}));
    assertEquals(true, mappingBindingPattern2({a: true}));
    assertEquals("No match", mappingBindingPattern2({b: true}));
}

function mappingBindingPattern3(any v) returns string {
    match v {
        var {a: x, b: y} if x is string => {
            return "match1";
        }
        var {a: x, b: y} if x is int => {
            return "match2";
        }
        var {a: x, b: y} if x is boolean => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingBindingPattern3() {
    assertEquals("match1", mappingBindingPattern3({a: "str1", b:"str2"}));
    assertEquals("match2", mappingBindingPattern3({a: 1, b: 2}));
    assertEquals("match3", mappingBindingPattern3({a: true, b: false, c : 3}));
    assertEquals("match2", mappingBindingPattern3({a: 1, b: 2, c: 4}));
    assertEquals("No match", mappingBindingPattern3({b: true}));
}

function mappingBindingPattern4(any v) returns string {
    match v {
        var {a: x, b: y} | var {c: x, d: y} | var {a: x, c: y}  => {
            return "match1";
        }
        var {x: a, y: b} | var {z: a, w: b} | var {x: a, z: b}  => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingBindingPattern4() {
    assertEquals("match1", mappingBindingPattern4({a: "str1", b: "str2"}));
    assertEquals("match1", mappingBindingPattern4({c: "str3", d: "str4"}));
    assertEquals("match1", mappingBindingPattern4({a: "str3", c: "str4", e: true}));
    assertEquals("match2", mappingBindingPattern4({x: "str1", y: "str2"}));
    assertEquals("match2", mappingBindingPattern4({z: "str3", w: "str4"}));
    assertEquals("match2", mappingBindingPattern4({x: "str3", z: "str4", v: true}));
    assertEquals("No match", mappingBindingPattern4({s: true}));
}

function mappingBindingPattern5(map<int> v) returns int {
    match v {
        var {a: x, b: y} => {
            return x;
        }
        var {x: a, y: b} => {
            return b;
        }
    }

    return -1;
}

function testMappingBindingPattern5() {
    map<int> m1 = {a: 2, b: 3};
    assertEquals(2, mappingBindingPattern5(m1));
    map<int> m2 = {x: 3, y: 3};
    assertEquals(3, mappingBindingPattern5(m2));
    map<int> m3 = {a: 4, d: 3};
    assertEquals(-1, mappingBindingPattern5(m3));
}

function mappingBindingPattern6(map<int|string> v) returns string|int {
    match v {
        var {a: x, b: y} => {
            return x;
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPattern6() {
    map<string> m1 = {a: "2", b: "3"};
    assertEquals("2", mappingBindingPattern6(m1));
    map<int> m2 = {a: 2, b: 3};
    assertEquals(2, mappingBindingPattern6(m2));
    map<int|string> m3 = {a: 4, b: "3"};
    assertEquals(4, mappingBindingPattern6(m3));
    map<int|string> m4 = {a: "4", b: 3};
    assertEquals("4", mappingBindingPattern6(m4));
    map<int|string> m5 = {a: "4", c: 3};
    assertEquals("No match", mappingBindingPattern6(m5));
}

function mappingBindingPattern7(any v) returns int|string {
    match v {
        var {a: x, b: y, c: z} if x is string && y is string && z is string => {
            return x + y + z;
        }
        var {a: x, b: y, c: z} if x is int && y is int && z is int => {
            return x + y + z;
        }
    }
    return "No match";
}

function testMappingBindingPattern7() {
    assertEquals("Hello Ballerina World!", mappingBindingPattern7({ a : "Hello ", b : "Ballerina ", c : "World!" }));
    assertEquals(8, mappingBindingPattern7({a: 5, b: 2, c: 1}));
    assertEquals("No match", mappingBindingPattern7({a: 5, b: 2}));
}

function mappingBindingPattern8(any v) returns string {
    match v {
        var {a: {x: a1, y: a2}, b:{x: b1, y:b2}} => {
            return "match1";
        }
        var {a: {x: a1, y: a2}} => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingBindingPattern8() {
    assertEquals("match1", mappingBindingPattern8({a: {x: 20, y: 30}, b: {x: 4, y: 5}}));
    assertEquals("match2", mappingBindingPattern8({a: {x: 2, y: 3}}));
    assertEquals("No match", mappingBindingPattern8({a: {w: 11, y: 13}, b: {x: 14, y: 15}}));
}

function mappingBindingPattern9(any v) returns string {
    match v {
        var {a: {x: a1, y: a2}, b: {x: b1, y: b2}} | var {c: {x: a1, y: a2}, d: {x: b1, y: b2}} => {
            return "match1";
        }
    }
    return "No match";
}

function testMappingBindingPattern9() {
    assertEquals("match1", mappingBindingPattern9({a: {x: 20, y: 30}, b: {x: 4, y: 5}}));
    assertEquals("match1", mappingBindingPattern9({c: {x: 20, y: 30}, d: {x: 4, y: 5}}));
    assertEquals("No match", mappingBindingPattern9({s: {x: 2, y: 3}}));
}

function mappingBindingPattern10(any v) returns string {
    match v {
        var {a: {x: a1, y: {m: a2, n: {p: a3, q: a4}}}} => {
            return "match1";
        }
        var {a: {x: a1, y: {m: a2, n: a3}}} => {
            return "match2";
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPattern10() {
    assertEquals("match1", mappingBindingPattern10({a: {x: 2, y: {m: 4, n: {p: 2, q: 3}}}}));
    assertEquals("match2", mappingBindingPattern10({a: {x: 2, y: {m: 4, n: 5}}}));
    assertEquals("No match", mappingBindingPattern10("No match"));
}

function mappingBindingPattern11(any v) returns string {
    match v {
        var {a: {x: a1, y: a2 } } if a2 is int => {
            return "match1";
        }
        var {a: {x: a1, y: a2}} if a2 is string => {
            return "match2";
        }
        var {a: {x: a1, y: a2}} if a2 is boolean => {
            return "match3";
        }
        var {a: {x: a1, y: a2}} if a2 is record { string z; } => {
            return a2.z;
        }
        var {a: {x: a1, y: a2}} if a2 is map<string> => {
            return "match4";
        }
        _  => {
            return "No match";
        }
    }
}

function testMappingBindingPattern11() {
    assertEquals("match1", mappingBindingPattern11({a: {x: 2, y: 3}}));
    assertEquals("match2", mappingBindingPattern11({a: {x: 2, y: "3"}}));
    assertEquals("match3", mappingBindingPattern11({a: {x: 2, y: true}}));
    map<string> m = {a1: "str" };
    assertEquals("match4", mappingBindingPattern11({a: {x: 2, y: m}}));
    var r = {z: "str" };
    assertEquals("str", mappingBindingPattern11({a: {x: 2, y: r}}));
    assertEquals("No match", mappingBindingPattern11("No match"));
}

function mappingBindingPattern12(map<string|int> v) returns string|int {
    match v {
        var {x: a, y: b} if b is string => {
            return b;
        }
        var {x: a, y: b} if a is string && b is int => {
            return b;
        }
        var {x: a, y: b} if a is int && b is int => {
            return a + b;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingBindingPattern12() {
    assertEquals("str", mappingBindingPattern12({x: "s", y: "str"}));
    assertEquals(3, mappingBindingPattern12({x: "2", y: 3}));
    assertEquals(11, mappingBindingPattern12({x: 5, y: 6, z: "world"}));
    assertEquals("No match", mappingBindingPattern12({w: 2, y: "Hello", z: "world"}));
}

function mappingBindingPattern13(map<string|int>|map<boolean|int> v) returns string|int {
    match v {
        var {x: b, y: a} if a is string => {
            return a;
        }
        var {x: b, y: a, z: c} if a is int && c is boolean => {
            return a;
        }
        var {x: a, y: b} if a is int && b is int => {
            return a + b;
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPattern13() {
    assertEquals("str", mappingBindingPattern13({x : "s", y: "str"}));
    assertEquals(3, mappingBindingPattern13({x: 2, y: 3, z: true}));
    assertEquals(11, mappingBindingPattern13({x: 5, y: 6, z: "world"}));
    assertEquals("No match", mappingBindingPattern13({w: 2, y: "Hello", z: "world"}));
}

function mappingBindingPattern14(record {int x; int y;} v) returns string|int {
    match v {
        var {x: a, y: b} => {
            return a;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingBindingPattern14() {
    assertEquals(2, mappingBindingPattern14({x: 2, y: 3}));
    assertEquals(3, mappingBindingPattern14({x: 3, y: 3}));
}

function mappingBindingPattern15(record { int i; string? s; } v) returns string? {
    match v {
        var {i, s} => {
            return s;
        }
    }
    return "No match";
}

function testMappingBindingPattern15() {
    assertEquals("str", mappingBindingPattern15({i: 2, s: "str"}));
}


function mappingBindingPattern16(any v) returns string? {
    match v {
        var {} => {
            return "Matches";
        }
    }
    return "No match";
}

function testMappingBindingPattern16() {
    assertEquals("Matches", mappingBindingPattern16({i: 2, s: "str"}));
}

function mappingBindingPattern17(json j) returns json {
    match j {
        var {x: x} => {
            return x;
        }
    }
    return ();
}

function testMappingBindingPattern17() {
    assertEquals("hello", mappingBindingPattern17({x: "hello"}));
    assertEquals(1, mappingBindingPattern17({y: "hello world", x: 1}));
    assertEquals((), mappingBindingPattern17({y: "hello world", x: ()}));
    assertEquals((), mappingBindingPattern17({a: "hello world", x1: 1}));
    assertEquals((), mappingBindingPattern17({}));
    assertEquals((), mappingBindingPattern17(1));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
