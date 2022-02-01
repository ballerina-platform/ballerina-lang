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

function mappingMatchPattern1(any v) returns string {
    match v {
        {a: "str"} => {
            return "match1";
        }
        {a: 1} => {
            return "match2";
        }
        {a: true} => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingMatchPattern1() {
    assertEquals("match1", mappingMatchPattern1({a: "str"}));
    assertEquals("match1", mappingMatchPattern1({a: "str", b: "D"}));
    assertEquals("match2", mappingMatchPattern1({a: 1}));
    assertEquals("match3", mappingMatchPattern1({a: true}));
    assertEquals("No match", mappingMatchPattern1({b: true}));
}

function mappingMatchPattern2(any v) returns string {
    match v {
        {a: "str1", b: "str2"} => {
            return "match1";
        }
        {a: 1, b: 2} => {
            return "match2";
        }
        {a: true, b: false, c: 3} => {
            return "match3";
        }
    }

    return "No match";
}

function testMappingMatchPattern2() {
    assertEquals("match1", mappingMatchPattern2({a: "str1", b: "str2"}));
    assertEquals("match2", mappingMatchPattern2({a: 1, b: 2}));
    assertEquals("match3", mappingMatchPattern2({a: true, b: false, c: 3}));
    assertEquals("match2", mappingMatchPattern2({a: 1, b: 2, c: 4}));
    assertEquals("No match", mappingMatchPattern2({b: true}));
}

function mappingMatchPattern3(any v) returns string {
    match v {
        {a: "str1", b: "str2"} | {a: "str3", b: "str4"} | {a: "str5", b: "str6", c: "str7"}  => {
            return "match1";
        }
        {a: 1, b: 2} | {a: 3, b: 4} | {a: 5, b: 6, c: 7} => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern3() {
    assertEquals("match1", mappingMatchPattern3({a: "str1", b: "str2"}));
    assertEquals("match1", mappingMatchPattern3({a: "str3", b: "str4"}));
    assertEquals("match1", mappingMatchPattern3({a: "str5", b: "str6", c: "str7"}));
    assertEquals("match2", mappingMatchPattern3({a: 1, b: 2}));
    assertEquals("match2", mappingMatchPattern3({a: 3, b: 4}));
    assertEquals("match2", mappingMatchPattern3({a: 5, b: 6, c: 7}));
    assertEquals("No match", mappingMatchPattern3({b: true}));
}

function mappingMatchPattern4(any v) returns string {
    match v {
        {a: var b} if b is int => {
            return "match1";
        }
        {a: var b} if b is string => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern4() {
    assertEquals("match1", mappingMatchPattern4({a: 2}));
    assertEquals("match2", mappingMatchPattern4({a: "str"}));
    assertEquals("No match", mappingMatchPattern4({a: 1.1}));
}

function mappingMatchPattern5(any v) returns string {
    match v {
        {a: var b, x: 2} if b is int => {
            return "match1";
        }
        {a: var b, x: "str"} if b is string => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern5() {
    map<int> m1 = {a: 2, x: 2};
    assertEquals("match1", mappingMatchPattern5(m1));
    map<string> m2 = {a: "a", x: "str"};
    assertEquals("match2", mappingMatchPattern5(m2));
    map<string> m3 = {a: "a", y: "str"};
    assertEquals("No match", mappingMatchPattern5(m3));
}

function mappingMatchPattern6(map<int> v) returns string {
    match v {
        {a: 2, b: 3} => {
            return "match1";
        }
        {a: 3, b: 3} => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern6() {
    map<int> m1 = {a: 2, b: 3};
    assertEquals("match1", mappingMatchPattern6(m1));
    map<int> m2 = {a: 3, b: 3};
    assertEquals("match2", mappingMatchPattern6(m2));
    map<int> m3 = {a: 4, b: 3};
    assertEquals("No match", mappingMatchPattern6(m3));
}

function mappingMatchPattern7(map<int|string> v) returns string {
    match v {
        {a: "2", b: "3"} => {
            return "match1";
        }
        {a: 2, b: 3} => {
            return "match2";
        }
        {a: 2, b: "3"} => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingMatchPattern7() {
    map<string> m1 = {a: "2", b: "3"};
    assertEquals("match1", mappingMatchPattern7(m1));
    map<int> m2 = {a: 2, b: 3};
    assertEquals("match2", mappingMatchPattern7(m2));
    map<int|string> m3 = {a: 2, b: "3"};
    assertEquals("match3", mappingMatchPattern7(m3));
    map<int|string> m4 = {a: 4, b: "3"};
    assertEquals("No match", mappingMatchPattern7(m4));
}

function mappingMatchPattern8(any v) returns string {
    match v {
        {a: var x} => {
            return "match1";
        }
    }
    return "No match";
}

function testMappingMatchPattern8() {
    assertEquals("match1", mappingMatchPattern8({a: 2}));
    assertEquals("No match", mappingMatchPattern8({b: 2}));
}

function mappingMatchPattern9(any v) returns string {
    match v {
        {a: var x} if x is int => {
            return "match1";
        }
        {a: var x} if x is string => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern9() {
    assertEquals("match1", mappingMatchPattern9({a: 2}));
    assertEquals("match2", mappingMatchPattern9({a: "2"}));
    assertEquals("No match", mappingMatchPattern9({a: false}));
}

function mappingMatchPattern10(any v) returns string {
    match v {
        {a: var x, b: 2} | {a: 2, b: var x} => {
            return "match1";
        }
        {a:  var x, b: "str"} | {a: "str", b: var x} => {
            return "match2";
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern10() {
    assertEquals("match1", mappingMatchPattern10({a: 5, b: 2}));
    assertEquals("match1", mappingMatchPattern10({a: "5", b: 2}));
    assertEquals("match1", mappingMatchPattern10({a: 2, b: 3}));
    assertEquals("match1", mappingMatchPattern10({a: 2, b: "str"}));
    assertEquals("match2", mappingMatchPattern10({a: 3, b: "str"}));
    assertEquals("match2", mappingMatchPattern10({a: "str", b: "s"}));
    assertEquals("No match", mappingMatchPattern10({a: "s", b: 5}));
}

function mappingMatchPattern11(any v) returns string {
    match v {
        {a: var x, b: var y, c: var z} => {
            return "match1";
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern11() {
    assertEquals("match1", mappingMatchPattern11({a: 5, b: 2, c: 1}));
    assertEquals("No match", mappingMatchPattern11({a: "5", b: 2}));
}

function mappingMatchPattern12(any v) returns int|string {
    match v {
        {a: var x, b: var y, c: var z } if x is string && y is string && z is string => {
            return x + y + z;
        }
        {a: var x, b: var y, c: var z} if x is int && y is int && z is int => {
            return x + y + z;
        }
    }
    return "No match";
}

function testMappingMatchPattern12() {
    assertEquals("Hello Ballerina World!", mappingMatchPattern12({a: "Hello ", b: "Ballerina ", c: "World!"}));
    assertEquals(8, mappingMatchPattern12({a: 5, b: 2, c: 1}));
    assertEquals("No match", mappingMatchPattern12({a: 5, b: 2}));
}

const int CONST1 = 2;
const string CONST2 = "const";
const int CONST3 = 100;

function mappingMatchPattern13(any v) returns int|string {
    match v {
        {a: CONST1, b: CONST2} => {
            return "match1";
        }
        {a: CONST2, b: CONST3} => {
            return "match2";
        }
    }
    return "No match";
}

function testMappingMatchPattern13() {
    assertEquals("match1", mappingMatchPattern13({a: CONST1, b: CONST2}));
    assertEquals("match1", mappingMatchPattern13({a: 2, b: "const"}));
    assertEquals("match2", mappingMatchPattern13({a: CONST2, b: CONST3}));
    assertEquals("match2", mappingMatchPattern13({a: "const", b: 100}));
    assertEquals("No match", mappingMatchPattern13({a: 5, b: 2}));
}

function mappingMatchPattern14(any v) returns string {
    match v {
        {a: {x: 2, y: 3}} => {
            return "match1";
        }
        {a: {x: 20, y: 30}, b: {x: 4, y: 5}} => {
            return "match2";
        }
        {a: {x: 22, y: 33}, b: {x: 4, y: 5}} | {a: {x: 12, y: 13}, b: {x: 14, y: 15}} => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingMatchPattern14() {
    assertEquals("match1", mappingMatchPattern14({a: {x: 2, y: 3}}));
    assertEquals("match2", mappingMatchPattern14({a: {x: 20, y: 30 }, b: {x: 4, y: 5}}));
    assertEquals("match3", mappingMatchPattern14({a: {x: 22, y: 33}, b: {x: 4, y: 5}}));
    assertEquals("match3", mappingMatchPattern14({a: {x: 12, y: 13}, b: {x: 14, y: 15}}));
    assertEquals("No match", mappingMatchPattern14({a: {x: 11, y: 13}, b: {x: 14, y: 15}}));
}

function mappingMatchPattern15(any v) returns string {
    match v {
        {a: {x: 2, y: {m: 4, n: 5}}} => {
            return "match1";
        }
        {a: {x: 2, y: {m: 4, n: {p: 2, q: 3}}}} => {
            return "match2";
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern15() {
    assertEquals("match1", mappingMatchPattern15({a: {x: 2, y: {m: 4, n: 5}}}));
    assertEquals("match2", mappingMatchPattern15({a: {x: 2, y: {m: 4, n: {p: 2, q: 3}}}}));
    assertEquals("No match", mappingMatchPattern15("No match"));
}

function mappingMatchPattern16(any v) returns string {
    match v {
        {a: {x: 2, y: var c}} => {
            return "match1";
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern16() {
    assertEquals("match1", mappingMatchPattern16({a: {x: 2, y: 3}}));
    assertEquals("match1", mappingMatchPattern16({a: {x: 2, y: "3"}}));
    assertEquals("match1", mappingMatchPattern16({a: {x: 2, y: true}}));
    map<string> m = {a1: "str"};
    assertEquals("match1", mappingMatchPattern16({a: {x: 2, y: m}}));
    assertEquals("No match", mappingMatchPattern16("No match"));
}

function mappingMatchPattern17(any v) returns string {
    match v {
        {a: {x: 2, y: var c}} if c is int => {
            return "match1";
        }
        {a: {x: 2, y: var c}} if c is string => {
            return "match2";
        }
        {a: {x: 2, y: var c} } if c is boolean => {
            return "match3";
        }
        {a: {x: 2, y: var c}} if c is record {string a1;} => {
            return c.a1;
        }
        {a: {x: 2, y: var c}} if c is map<string> => {
            return "match4";
        }
        _  => {
            return "No match";
        }
    }
}

function testMappingMatchPattern17() {
    assertEquals("match1", mappingMatchPattern17({a: {x: 2, y: 3}}));
    assertEquals("match2", mappingMatchPattern17({a: {x: 2, y: "3"}}));
    assertEquals("match3", mappingMatchPattern17({a: {x: 2, y: true}}));
    map<string> m = {a1: "str"};
    assertEquals("match4", mappingMatchPattern17({a: {x: 2, y: m}}));
    var r = {a1: "str"};
    assertEquals("str", mappingMatchPattern17({a: {x: 2, y: r}}));
    assertEquals("No match", mappingMatchPattern17("No match"));
}

function mappingMatchPattern18(any v) returns string {
    match v {
        {a: {x: CONST1, y: CONST2}} | {a: {x: CONST1}, b: {y: CONST2}} => {
            return "match1";
        }
        _  => {
            return "No match";
        }
    }
}

function testMappingMatchPattern18() {
    assertEquals("match1", mappingMatchPattern18({a: {x: CONST1, y: CONST2}}));
    assertEquals("match1", mappingMatchPattern18({a: {x: 2, y: "const"}}));
    assertEquals("match1", mappingMatchPattern18({a: {x: CONST1}, b: {y: CONST2}}));
    assertEquals("match1", mappingMatchPattern18({a: {x: 2}, b: {y: "const"}}));
    assertEquals("No match", mappingMatchPattern18("No match"));
}

function mappingMatchPattern19(record {int x; int|string y;} v) returns string {
    match v {
        {x: 2, y: 3} => {
            return "match1";
        }
        {x: 2, y: "3"} => {
            return "match2";
        }
        {x: 2, y: var a} => {
            return "match3";
        }
    }
    return "No match";
}

function testMappingMatchPattern19() {
    assertEquals("match1", mappingMatchPattern19({x: 2, y: 3}));
    assertEquals("match2", mappingMatchPattern19({x: 2, y: "3"}));
    assertEquals("match3", mappingMatchPattern19({x: 2, y: 8}));
    assertEquals("No match", mappingMatchPattern19({x: 8, y: "3"}));
}

function mappingMatchPattern20(map<string> v) returns string {
    match v {
        {x: "s", y: var a} => {
            return a;
        }
        {x: var a, y: var b} => {
            return a + b;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern20() {
    assertEquals("str", mappingMatchPattern20({x: "s", y: "str"}));
    assertEquals("Hello world", mappingMatchPattern20({x: "Hello ", y: "world"}));
    assertEquals("Hello Hello", mappingMatchPattern20({x: "Hello ", y: "Hello", z: "world"}));
    assertEquals("Hello Hello", mappingMatchPattern20({x: "Hello ", y: "Hello", z: "world"}));
    assertEquals("No match", mappingMatchPattern20({a: "No match"}));
}

function mappingMatchPattern21(map<string|int> v) returns string|int {
    match v {
        {x: "s", y: var a} if a is string => {
            return a;
        }
        {x: 2, y: var a} if a is int => {
            return a;
        }
        {x: var a, y: var b} if a is int && b is int => {
            return a + b;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern21() {
    assertEquals("str", mappingMatchPattern21({x: "s", y: "str"}));
    assertEquals(3, mappingMatchPattern21({x: 2, y: 3}));
    assertEquals(11, mappingMatchPattern21({x: 5, y: 6, z: "world"}));
    assertEquals("No match", mappingMatchPattern21({x: 2, y: "Hello", z: "world"}));
}

function mappingMatchPattern22(map<string|int>|map<boolean|int> v) returns string|int {
    match v {
        {x: "s", y: var a} if a is string => {
            return a;
        }
        {x: 2, y: var a, z: true} if a is int => {
            return a;
        }
        {x: var a, y: var b} if a is int && b is int => {
            return a + b;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern22() {
    assertEquals("str", mappingMatchPattern22({x: "s", y: "str"}));
    assertEquals(3, mappingMatchPattern22({x: 2, y: 3, z: true}));
    assertEquals(11, mappingMatchPattern21({x: 5, y: 6, z: "world"}));
    assertEquals("No match", mappingMatchPattern21({x: 2, y: "Hello", z: "world"}));
}

function mappingMatchPattern23(record {int x; int y;} v) returns string|int {
    match v {
        {x: 2, y: var a} => {
            return a;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern23() {
    assertEquals(3, mappingMatchPattern23({x: 2, y: 3}));
    assertEquals("No match", mappingMatchPattern23({x: 3, y: 3}));
}

function mappingMatchPattern24(anydata v) returns anydata {
    match v {
        {x: var a, y: var b} => {
            return a;
        }
        _ => {
            return "No match";
        }
    }
}

function testMappingMatchPattern24() {
    assertEquals(2, mappingMatchPattern24({x: 2, y: 3}));
    assertEquals("No match", mappingMatchPattern24({x: 3, z: 3}));
}

function mappingMatchPattern25(anydata v) returns string {
    match v {
        {x: _, y: _} => {
            return "matched x and y";
        }
        var z => {
            return "other";
        }
    }
}

function testMappingMatchPattern25() {
    assertEquals("matched x and y", mappingMatchPattern25({x: "abc", y: 1}));
    assertEquals("matched x and y", mappingMatchPattern25({x: "abc", y: 1, z: "hello"}));
    assertEquals("other", mappingMatchPattern25({a: "abc", b: 1}));
    assertEquals("other", mappingMatchPattern25(1));
}

function testMappingMatchPatternWithWildCard() {
    map<string>|error v1 = {a: "str5", b: "str6", c: "str7"};
    string result = "";
    match v1 {
        {a: "str1", b: "str2"} | {a: "str5", b: "str6", c: "str8"} => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Default", result);

    any|error v2 = error("SampleError");
    result = "Not Matched";
    match v2 {
        {a: "str1", b: "str2"} => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Not Matched", result);
}

function mappingMatchPattern26(json j) returns json {
    match j {
        {x: var x} => {
            return x;
        }
    }
    return ();
}

function mappingMatchPattern27(json j) returns json {
    match j {
        {x: var x} => {
            var lambdaFunc = function() returns json {
                return x;
            };
            return lambdaFunc();
        }
    }
    return ();
}

function testMappingMatchPattern26() {
    assertEquals("hello", mappingMatchPattern26({x: "hello"}));
    assertEquals(1, mappingMatchPattern26({y: "hello world", x: 1}));
    assertEquals((), mappingMatchPattern26({y: "hello world", x: ()}));
    assertEquals((), mappingMatchPattern26({a: "hello world", x1: 1}));
    assertEquals((), mappingMatchPattern26({}));
    assertEquals((), mappingMatchPattern26(1));
    assertEquals(1, mappingMatchPattern27({y: "hello world", x: 1}));
}

type Foo record {|
    int x;
    int y = 1;
|};

function fn1() returns string {
    Foo v = {x: 0, y: 1};
    string matched = "";

    match v {
        {x: 0, y: 1} => {
            matched = "Matched";
        }
    }
    return matched;
}

function fn2() returns string {
    Foo v = {x: 0};
    string matched = "";

    match v {
        {x: 0} => {
            matched = "Matched";
        }
    }
    return matched;
}

function testMappingBindingToRecordWithDefaultValue() {
    assertEquals("Matched", fn1());
    assertEquals("Matched", fn2());
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
