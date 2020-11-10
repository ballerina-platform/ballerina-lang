// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;

function testStaticMatchPatternsBasic1() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;

    string | int | boolean a4 = 15;
    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = false;

    string | int | boolean a7 = "NothingToMatch";

    string[] result = [foo(a1), foo(a2), foo(a3), foo(a4), foo(a5), foo(a6), foo(a7)];

    return result;
}

function foo(string | int | boolean a) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            return "Value is '15'";
        }
        true => {
            return "Value is 'true'";
        }
        false => {
            return "Value is 'false'";
        }
        "HelloAgain" => {
            return "Value is 'HelloAgain'";
        }
    }

    return "Value is 'Default'";
}


function testStaticMatchPatternsBasic2() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";

    string | int | boolean a3 = 15;
    string | int | boolean a4 = "HelloWorld";

    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = 34;

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = false;

    string | int | boolean a9 = 15;
    string | int | boolean a10 = 34;

    string | int | boolean a11 = true;
    string | int | boolean a12 = false;

    string[] result = [bar(a1, a2), bar(a3, a4), bar(a5, a6), bar(a7, a8), bar(a9, a10), bar(a11, a12)];

    return result;
}

function bar(string | int | boolean a, string | int | boolean b) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            match b {
                34 => {
                    return "Value is '15 & 34'";
                }
                "HelloWorld" => {
                    return "Value is '15 & HelloWorld'";
                }
            }
        }
        "HelloAgain" => {
            match b {
                34 => {
                    return "Value is 'HelloAgain & 34'";
                }
                "HelloWorld" => {
                    return "Value is 'HelloAgain & HelloWorld'";
                }
            }
        }
        true => {
            return "Value is 'true'";
        }
    }

    return "Value is 'Default'";
}

type Foo record {
    int x;
    string y;
};

function testRecordStaticMatch() returns string[] {

    Foo f1 = {x: 12, y: "Ballerina"};
    map<any> m1 = {x: 10, y: "B"};
    Foo f2 = {x: 12, y: "Ballerina", "z": true};
    map<any> m2 = {x: 10, z: "Ballerina"};

    string[] result = [tar1(f1), tar1(m1), tar1(15), tar1(f2), tar1(m2)];

    return result;
}

function tar1(Foo | map<any> | int f) returns string {

    match f {
        {x: 12, y: "B"} => {
            return "Value is 'x: 12, y: B'";
        }
        {x: 10, y: "Ballerina"} => {
            return "Value is 'x: 10, y: B'";
        }
        {x: 12, y: "Ballerina", z: true} => {
            return "Value is 'x: 12, y: Ballerina, z: true'";
        }
        {x: 12, y: "Ballerina"} => {
            return "Value is 'x: 12, y: Ballerina'";
        }
        {x: 10, y: "B"} => {
            return "Value is 'x: 10, y: B'";
        }
        16 => {
            return "Value is '16'";
        }
        15 => {
            return "Value is '15'";
        }
    }

    return "Value is 'Default'";
}

function testTupleStaticMatch() returns string[] {

    [int, string] t1 = [12, "Ballerina"];
    anydata t2 = t1;
    [int, string, byte] t3 = [15, "Bal", 100];
    [int, string] t4 = [15, "Ballerina"];
    [int, string] t5 = [16, "Ballerina"];

    string[] result = [tar2(t1), tar2(t2), tar2(15), tar2(t3), tar2(t4), tar2(t5)];

    return result;
}

function tar2(anydata f) returns string {

    match f {
        15 => {
            return "Value is '(15)'";
        }
        [12, "Ballerina"] => {
            return "Value is '(12, Ballerina)'";
        }
        [15, "Ballerina", 100] => {
            return "Value is '(15, Ballerina, 100)'";
        }
        [15, "Ballerina"] => {
            return "Value is '(15, Ballerina)'";
        }
        [15, "Bal", 100] => {
            return "Value is '(15, Bal, 100)'";
        }
        16 => {
            return "Value is '16'";
        }
    }

    return "Value is 'Default'";
}

type Bar record {
    int x;
    [string, Foo, string] y;
};

function testRecordAndTupleComplexStaticMatch() returns string[] {
    [boolean, float] t1 = [true, 12.1];
    Foo f1 = {x: 12, y: "Ballerina", "z": t1};
    Bar b1 = {x: 15, y: ["John", f1, "Snow"], "z": 15.1};

    string[] result = [tar3(b1)];

    return result;
}

function tar3(anydata f) returns string {
    match f {
        {x: 15, y: ["John", {x: 12, y: "Ballerina", z: [true, 12.1]}, "Snow", true], z: 15.1} => {
            return "Value is 'Incorrect'";
        }
        {x: 15, y: ["John", {x: 12, y: "Ballerina", z: [true, 12.1]}, "Snow"], z: 15.1} => {
            return "Value is 'Correct'";
        }
        {x: 15, y: ["John", {x: 12, y: "Ballerina", z: [true, 12.1]}, "Snow"]} => {
            return "Value is 'Incorrect'";
        }
        {x: 15} => {
            return "Value is 'Incorrect'";
        }
    }
    return "Value is 'Default'";
}

type Finite "A" | true | 15.2 | "B" | "C";

function testFiniteType() returns string {
    Finite a = 15.2;
    match a {
        "A" => {
            return "Value is 'A'";
        }
        true => {
            return "Value is 'true'";
        }
        15.2 => {
            return "Value is '15.2'";
        }
        "B" => {
            return "Value is 'B'";
        }
        "C" => {
            return "Value is 'C'";
        }
    }
    return "Value is 'Default'";
}

function testFiniteType2() returns string {
    Finite a = true;
    anydata ad = a;
    match ad {
        "A" => {
            return "Value is 'A'";
        }
        true => {
            return "Value is 'true'";
        }
        15.2 => {
            return "Value is '15.2'";
        }
        "B" => {
            return "Value is 'B'";
        }
        "C" => {
            return "Value is 'C'";
        }
    }
    return "Value is 'Default'";
}

class Obj {
    int var1;
    function init(int var1) {
        self.var1 = var1;
    }
}

function testNonAnyDataType() returns string {
    Obj y = new (12);
    any obj = y;

    match obj {
        {var1: 12} => {
            return "Value is '12'";
        }
    }

    return "Value is 'Default'";
}

function testStringLiteralKeyInRecordMatch() returns string {
    Foo f = {x: 12, y: "B"};
    match f {
        {"x": 12, y: "B"} => {
            return "Value is 'Correct'";
        }
    }
    return "Value is 'Default'";
}

function testStaticMatchOrPatterns1() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;

    string | int | boolean a4 = 15;
    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = false;

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = 13;
    string | int | boolean a9 = 14;
    string | int | boolean a10 = "World";
    string | int | boolean a11 = "Test";

    string[] result = [baz(a1), baz(a2), baz(a3), baz(a4), baz(a5), baz(a6), baz(a7), baz(a8), baz(a9), baz(a10), baz(a11)];

    return result;
}

function baz(string | int | boolean a) returns string {
    match a {
        12 | 13 | 14 => {
            return "Value is : " + io:sprintf("%s", a);
        }
        "Hello" | "World" => {
            return "Value is : " + io:sprintf("%s", a);
        }
        15 | "Test" => {
            return "Value is : " + io:sprintf("%s", a);
        }
        true | false => {
            return "Value is : " + io:sprintf("%s", a);
        }
        "HelloAgain" => {
            return "Value is : " + io:sprintf("%s", a);
        }
    }

    return "Default value is : " + io:sprintf("%s", a);
}


function testStaticMatchOrPatterns2() returns string[] {

    int t1 = 15;
    [int, string] t2 = [12, "Ballerina"];
    anydata t3 = t2;
    [int, string] t4 = [15, "Ballerina"];
    [int, string] t5 = [20, "Ballerina"];
    [int, string] t6 = [20, "Balo"];
    [int, string] t7 = [20, "NothingToMatch"];

    [int, string, byte] t8 = [15, "Bal", 100];

    [int, string, byte, int] t9 = [15, "Bal", 200, 230];
    [int, string, string, int, string] t10 = [15, "Bal", "Ballerina", 5678, "Test"];
    [int, string, string, int, string] t11 = [15, "Bal", "Ballerina", 5678, "NothingToMatch"];

    string[] result = [caz1(t1), caz1(t2), caz1(t3), caz1(t4), caz1(t5), caz1(t6), caz1(t7), caz1(t8), caz1(t9), caz1(t10), caz1(t11)];

    return result;
}

function caz1(anydata f) returns string {

    match f {
        15 | [12, "Ballerina"] => {
            return "Value is : " + io:sprintf("%s", f);
        }
        [15, "Ballerina"] | [20, "Ballerina"] | [20, "Balo"] => {
            return "Value is : " + io:sprintf("%s", f);
        }
        [15, "Bal", 100] | [15, "Bal", 200, 400] | [15, "Bal", "Ballerina", 5678, "Test"] => {
            return "Value is : " + io:sprintf("%s", f);
        }
    }

    return "Default Value is : " + io:sprintf("%s", f);
}

type AnotherFoo record {
    int x;
    [string, Foo, string] y;
};


function testStaticMatchOrPatterns3() returns string[] {

    Foo f1 = {x: 12, y: "Ballerina"};
    map<any> m1 = {x: 10, y: "B"};
    Foo f2 = {x: 12, y: "Ballerina", "z": true};
    map<any> m2 = {x: 10, z: "Ballerina"};
    AnotherFoo af1 = {x: 15, y: ["John", {x: 12, y: "Ballerina"}, "Snow"], "z": 15.1};
    AnotherFoo af2 = {x: 15, y: ["Stark", f1, "Sansa"], "z": 15.1};
    AnotherFoo af3 = {x: 15, y: ["Stark", {x: 12, y: "Ballerina", "z": true}, "Sansa"], "z": 15.1};
    AnotherFoo af4 = {x: 40, y: ["Tyrion", {x: 12, y: "Ballerina"}, "Lanister"], "z": 56.9};
    int i1 = 16;
    int i2 = 12;

    string[] result = [caz2(f1), caz2(m1), caz2(f2), caz2(m2), caz2(af1), caz2(af2), caz2(af3), caz2(af4), caz2(i1), caz2(i2)];

    return result;
}

function caz2(Foo | AnotherFoo | map<any> | int f) returns string {

    match f {
        {x: 10, y: "Ballerina"} | {x: 12, y: "Ballerina", z: true} => {
            return "Value is : 1st pattern - " + io:sprintf("%s", f);
        }
        {x: 12, y: "B"} | {x: 12, y: "Ballerina"} => {
            return "Value is : 2nd pattern - " + io:sprintf("%s", f);
        }
        {x: 15, y: ["Stark", {x: 12, y: "Ballerina", z: true}, "Sansa"], z: 15.1} |
        {x: 40, y: ["Tyrion", {x: 12, y: "Ballerina"}, "Lanister"], z: 56.9} => {
            return "Value is : 3rd pattern - " + io:sprintf("%s", f);
        }
        16 | 15 | {x: 10, y: "B"} => {return "Value is : 4th pattern - " + io:sprintf("%s", f);}
        {x: 15, y: ["John", {x: 12, y: "Ballerina"}, "Snow"], z: 15.1} => {
            return "Value is : 5th pattern - " + io:sprintf("%s", f);
        }
    }

    return "Value is Default pattern - " + io:sprintf("%s", f);
}


function testStaticMatchOrPatterns4() returns string[] {

    Foo f1 = {x: 12, y: "Ballerina"};
    map<any> m1 = {x: 10, y: "B"};
    Foo f2 = {x: 12, y: "Ballerina", "z": true};
    map<any> m2 = {x: 10, z: "Ballerina"};
    AnotherFoo af1 = {x: 15, y: ["John", {x: 12, y: "Ballerina"}, "Snow"], "z": 15.1};
    AnotherFoo af2 = {x: 15, y: ["Stark", f1, "Sansa"], "z": 15.1};
    AnotherFoo af3 = {x: 15, y: ["Stark", {x: 12, y: "Ballerina", "z": true}, "Sansa"], "z": 15.1};
    AnotherFoo af4 = {x: 40, y: ["Tyrion", {x: 12, y: "Ballerina"}, "Lanister"], "z": 56.9};
    int i1 = 16;
    int i2 = 12;
    float f = 7.8;

    string[] result = [caz3(f1), caz3(m1), caz3(f2), caz3(m2), caz3(af1), caz3(af2), caz3(af3), caz3(af4), caz3(i1), caz3(i2), caz3(f)];

    return result;
}

function caz3(any f) returns string {

    match f {
        {x: 10, y: "Ballerina"} | {x: 12, y: "Ballerina", z: true} => {
            return "Value is : 1st pattern - " + io:sprintf("%s", f);
        }
        {x: 12, y: "B"} | {x: 12, y: "Ballerina"} => {
            return "Value is : 2nd pattern - " + io:sprintf("%s", f);
        }
        {x: 15, y: ["Stark", {x: 12, y: "Ballerina", z: true}, "Sansa"], z: 15.1} |
        {x: 40, y: ["Tyrion", {x: 12, y: "Ballerina"}, "Lanister"], z: 56.9} => {
            return "Value is : 3rd pattern - " + io:sprintf("%s", f);
        }
        16 | 15 | {x: 10, y: "B"} => {return "Value is : 4th pattern - " + io:sprintf("%s", f);}
        {x: 15, y: ["John", {x: 12, y: "Ballerina"}, "Snow"], z: 15.1} => {
            return "Value is : 5th pattern - " + io:sprintf("%s", f);
        }
    }

    return "Value is Default pattern - " + io:sprintf("%s", f);
}

function testBracedUnionType() returns string {
    any|error a = 12;
    match a {
        1 | 2 => {return "1|2";}
        3 | 4 => {return "3|4";}
        11 | 12 => {return "11|12";}
        _ => {return "Default";}
    }
    return "NoMatch";
}

const CONST1 = "Ballerina";
const CONST2 = 200;
const NIL = ();

function testStaticMatchWithConstants() returns string[] {
    string a1 = "Bal";
    string a2 = "Ballerina";
    int a3 = 100;
    int a4 = 200;
    () a5 = ();
    float a6 = 100.0;

    string[] result = [caz4(a1), caz4(a2), caz4(a3), caz4(a4), caz4(a5), caz4(a6)];

    return result;
}

function caz4(any a) returns string {
    match a {
        100 => {
            return "100";
        }
        CONST2 => {
            return "200";
        }
        "Bal" => {
            return "Bal";
        }
        NIL => {
            return "Nil";
        }
        CONST1 => {
            return "Ballerina";
        }
        _ => {
            return "Default";
        }
    }
}

const CONST_1 = "A";
const CONST_2 = "B";
const CONST_3 = 10;
const CONST_4 = true;

function testMatchingConstTypesWithConstPatterns() returns string[] {
    string[] results = [caz5(CONST_1), caz5(CONST_2), caz5(CONST_3), caz5(CONST_4)];
    return results;
}

function caz5(CONST_1 | CONST_2 | CONST_3 | CONST_4 a) returns string {
    string results = "";

    match a {
        CONST_2 => {
            results += "B";
        }
        CONST_1 => {
            results += "A";
        }
        true => {
            results += "true";
        }
        CONST_3 => {
            results += "10";
        }
    }

    match a {
        10 => {
            results += "10";
        }
        "A" => {
            results += "A";
        }
        CONST_4 => {
            results += "true";
        }
        "B" => {
            results += "B";
        }
    }

    return results;
}

function testStructuredMatchPatternWithEmptyTuple() returns string[] {
    int[] arr = [];
    string[] result = [];
    result[result.length()] = foo8(arr);

    arr[arr.length()] = 1;
    result[result.length()] = foo8(arr);

    arr[arr.length()] = 2;
    result[result.length()] = foo8(arr);

    arr[arr.length()] = 3;
    result[result.length()] = foo8(arr);

    arr[arr.length()] = 4;
    result[result.length()] = foo8(arr);

    return result;
}

function foo8(any x) returns string {
    match x {
        [] => {
            return "Matched with empty array";
        }
        [1] => {
            return "Matched with i: 1";
        }
        [1, 2] => {
            return "Matched with i: 1, j: 2";
        }
        [1, 2, 3] => {
            return "Matched with i: 1, j: 2, k: 3";
        }
        _ => {
            return "Matched with default";
        }
    }
}

function testStructuredMatchPatternWithEmptyRecord() returns string[] {
    record {} rec = {};
    string[] result = [];
    result[result.length()] = foo9(rec);

    rec["a"] = 1;
    result[result.length()] = foo9(rec);

    rec["b"] = 2;
    result[result.length()] = foo9(rec);

    rec["c"] = 3;
    result[result.length()] = foo9(rec);

    rec["d"] = 4;
    result[result.length()] = foo9(rec);

    return result;
}

function foo9(any x) returns string {
    match x {
        {a: 1, b: 2, c: 3} => {
            return "Matched with a: 1, b: 2, c: 3";
        }
        {a: 1, b: 2} => {
            return "Matched with a: 1, b: 2";
        }
        {a: 1} => {
            return "Matched with a: 1";
        }
        {} => {
            return "Matched with empty record";
        }
        _ => {
            return "Matched with default";
        }
    }
}

function testErrorShouldNotMatchWildCardPattern() returns string {
    any|error v = error("{UserGenError}Error");
    match v {
        0 => { return "zero"; }
        _ => { return "other"; }
    }
    return "no-match";
}
