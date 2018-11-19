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

function testStaticMatchPatternsBasic1() returns string[] {
    string | int | boolean a1 =  12;
    string | int | boolean a2 =  "Hello";
    string | int | boolean a3 =  true;

    string | int | boolean a4 =  15;
    string | int | boolean a5 =  "HelloAgain";
    string | int | boolean a6 =  false;

    string | int | boolean a7 =  "NothingToMatch";

    string[] result = [foo(a1), foo(a2), foo(a3), foo(a4), foo(a5), foo(a6), foo(a7)];

    return result;
}

function foo(string | int | boolean a) returns string {
    match a {
        12 => return "Value is '12'";
        "Hello" => return "Value is 'Hello'";
        15 => return "Value is '15'";
        true => return "Value is 'true'";
        false => return "Value is 'false'";
        "HelloAgain" => return "Value is 'HelloAgain'";
    }

    return "Value is 'Default'";
}


function testStaticMatchPatternsBasic2() returns string[] {
    string | int | boolean a1 =  12;
    string | int | boolean a2 =  "Hello";

    string | int | boolean a3 =  15;
    string | int | boolean a4 =  "HelloWorld";

    string | int | boolean a5 =  "HelloAgain";
    string | int | boolean a6 =  34;

    string | int | boolean a7 =  "NothingToMatch";
    string | int | boolean a8 =  false;

    string | int | boolean a9 =  15;
    string | int | boolean a10 =  34;

    string | int | boolean a11 =  true;
    string | int | boolean a12 =  false;

    string[] result = [bar(a1, a2), bar(a3, a4), bar(a5, a6), bar(a7, a8), bar(a9, a10), bar(a11, a12)];

    return result;
}

function bar(string | int | boolean a, string | int | boolean b)  returns string {
    match a {
        12 => return "Value is '12'";
        "Hello" => return "Value is 'Hello'";
        15 => match b {
                 34 => return "Value is '15 & 34'";
                 "HelloWorld" => return "Value is '15 & HelloWorld'";
              }
        "HelloAgain" => match b {
                           34 => return "Value is 'HelloAgain & 34'";
                           "HelloWorld" => return "Value is 'HelloAgain & HelloWorld'";
                        }
        true => return "Value is 'true'";
    }

    return "Value is 'Default'";
}

type Foo record {
    int x;
    string y;
};

function testRecordStaticMatch() returns string[] {

    Foo f1 = {x: 12, y: "Ballerina"};
    map m1 = {x: 10, y: "B"};
    Foo f2 = {x: 12, y: "Ballerina", z: true};
    map m2 = {x: 10, z: "Ballerina"};

    string[] result = [tar1(f1), tar1(m1), tar1(15), tar1(f2), tar1(m2)];

    return result;
}

function tar1(Foo|map|int f) returns string {

    match f {
        {x: 12, y: "B"} => return "Value is 'x: 12, y: B'";
        {x: 10, y: "Ballerina"} => return "Value is 'x: 10, y: B'";
        {x: 12, y: "Ballerina", z: true} => return "Value is 'x: 12, y: Ballerina, z: true'";
        {x: 12, y: "Ballerina"} => return "Value is 'x: 12, y: Ballerina'";
        {x: 10, y: "B"} => return "Value is 'x: 10, y: B'";
        16 => return "Value is '16'";
        15 => return "Value is '15'";
    }

    return "Value is 'Default'";
}

function testTupleStaticMatch() returns string[] {

    (int, string) t1 = (12, "Ballerina");
    anydata t2 = t1;
    (int, string, byte) t3 = (15, "Bal", 100);
    (int, string) t4 = (15, "Ballerina");
    (int, string) t5 = (16, "Ballerina");

    string[] result = [tar2(t1), tar2(t2), tar2(15), tar2(t3), tar2(t4), tar2(t5)];

    return result;
}

function tar2(anydata f) returns string {

    match f {
        (15) => return "Value is '(15)'";
        (12, "Ballerina") => return "Value is '(12, Ballerina)'";
        (15, "Ballerina", 100) => return "Value is '(15, Ballerina, 100)'";
        (15, "Ballerina") => return "Value is '(15, Ballerina)'";
        (15, "Bal", 100) => return "Value is '(15, Bal, 100)'";
        16 => return "Value is '16'";
    }

    return "Value is 'Default'";
}

type Bar record {
    int x;
    (string, Foo, string) y;
};

function testRecordAndTupleComplexStaticMatch() returns string[] {
    (boolean, float) t1 = (true, 12.1);
    Foo f1 = {x: 12, y: "Ballerina", z: t1};
    Bar b1 = {x: 15, y: ("John", f1, "Snow"), z: 15.1};

    string[] result = [tar3(b1)];

    return result;
}

function tar3(anydata f) returns string {
    match f {
        {x: 15, y: ("John", {x: 12, y: "Ballerina", z: (true, 12.1)}, "Snow", true), z:15.1} => return "Value is 'Incorrect'";
        {x: 15, y: ("John", {x: 12, y: "Ballerina", z: (true, 12.1)}, "Snow"), z:15.1} => return "Value is 'Correct'";
        {x: 15, y: ("John", {x: 12, y: "Ballerina", z: (true, 12.1)}, "Snow")} => return "Value is 'Incorrect'";
        {x: 15} => return "Value is 'Incorrect'";
    }
    return "Value is 'Default'";
}

//type Finite "A" | true | 15.2 | "B" | "C";

//function testFiniteType() returns string {
//    Finite a = 15.2;
//    match a {
//        "A" => return "Value is 'A'";
//        true => return "Value is 'true'";
//        15.2 => return "Value is '15.2'";
//        "B" => return "Value is 'B'";
//        "C" => return "Value is 'C'";
//    }
//    return "Value is 'Default'";
//}

//function testFiniteType2() returns string {
//    Finite a = true;
//    anydata ad = a;
//    match ad {
//        "A" => return "Value is 'A'";
//        true => return "Value is 'true'";
//        15.2 => return "Value is '15.2'";
//        "B" => return "Value is 'B'";
//        "C" => return "Value is 'C'";
//    }
//    return "Value is 'Default'";
//}

type Obj object {
    int var1;
    new (var1) {}
};

function testNonAnyDataType() returns string {
    Obj y = new(12);
    any obj = y;

    match obj {
        {var1: 12} => return "Value is '12'";
    }

    return "Value is 'Default'";
}

function testStringLiteralKeyInRecordMatch() returns string {
    Foo f = {x: 12, y: "B"};
    match f {
        {"x": 12, y: "B"} => return "Value is 'Correct'";
    }
    return "Value is 'Default'";
}
