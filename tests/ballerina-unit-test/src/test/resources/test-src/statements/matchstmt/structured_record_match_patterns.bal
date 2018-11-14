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

type Foo record {
    string s;
    int i;
    float f;
};

function testStructuredMatchPatternsBasic1() returns string {
    Foo foo = {s: "S", i: 23, f: 5.6};

    match foo {
        var {s, i: integer, f} => return "Matched Values : " + s + ", " + integer + ", " + f;
    }

    return "Default";
}

type Bar record {
    byte b;
    Foo f;
};

function testStructuredMatchPatternsBasic2() returns string {
    Foo foo = {s: "S", i: 23, f: 5.6};
    Bar bar = {b: 12, f: foo};

    match bar {
        var {b: byteValue, f: {s, i, f}} => return "Matched Values : " + s + ", " + i + ", " + f + ", " + <int> byteValue;
    }

    return "Default";
}

function testStructuredMatchPatternsBasic3() returns string {
    Foo foo = {s: "S", i: 23, f: 5.6};
    Bar bar = {b: 12, f: foo};

    match bar {
        var {b, f} => return "Matched Values : " + io:sprintf("%s", b) + ", " +  io:sprintf("%s", f);
    }

    return "Default";
}

function testStructuredMatchPatternsBasic4() returns string {
    Foo foo = {s: "S", i: 23, f: 5.6};
    Bar bar = {b: 12, f: foo};

    match bar {
        var y => return "Matched Values : " + io:sprintf("%s", y);
    }

    return "Default";
}

type ClosedFoo1 record {
    string var1;
    int var2;
    !...
};

type ClosedFoo2 record {
    float var1;
    boolean var2;
    !...
};

type ClosedFoo3 record {
    string var1;
    int var2;
    boolean var3;
    !...
};

type ClosedFoo4 record {
    string var1;
    !...
};

function testStructuredMatchPatternsBasics5() returns string[] {

    ClosedFoo1 foo1 = {var1: "Hello", var2: 150};
    ClosedFoo2 foo2 = {var1: 12.4, var2: true};
    ClosedFoo3 foo3 = {var1: "Hello", var2: 150, var3: true};
    ClosedFoo4 foo4 = {var1: "Hello"};

    ClosedFoo1|ClosedFoo2|ClosedFoo3|ClosedFoo4 a1 = foo1;
    ClosedFoo1|ClosedFoo2|ClosedFoo3|ClosedFoo4 a2 = foo2;
    ClosedFoo1|ClosedFoo2|ClosedFoo3|ClosedFoo4 a3 = foo3;
    ClosedFoo1|ClosedFoo2|ClosedFoo3|ClosedFoo4 a4 = foo4;

    string[] result = [basicMatch(a1), basicMatch(a2), basicMatch(a3), basicMatch(a4)];

    return result;
}

function basicMatch(ClosedFoo1|ClosedFoo2|ClosedFoo3|ClosedFoo4 a) returns string {
    match a {
        var {var1, var2, var3} => return "Matched with three vars : " + io:sprintf("%s", var1) + ", " +
                            io:sprintf("%s", var2) + ", " + io:sprintf("%s", var3);
        var {var1, var2} => return "Matched with two vars : " + io:sprintf("%s", var1) + ", " + io:sprintf("%s", var2);
        var {var1} => return "Matched with single var : " + io:sprintf("%s", var1);
    }

    return "Default";
}

type ClosedBar1 record {
    string var1;
    int var2;
    !...
};

type ClosedBar2 record {
    string var1;
    ClosedBar1 var2;
};

function testStructuredMatchPatternComplex1() returns string[] {
    ClosedBar1 bar1 = {var1: "Ballerina", var2: 500};
    ClosedBar2 bar2 = {var1: "Language", var2: bar1};

    ClosedBar1|ClosedBar2|string a1 = bar1;
    ClosedBar1|ClosedBar2|string a2 = bar2;
    ClosedBar1|ClosedBar2|string a3 = "bar2";

    string[] result = [complexMatch(a1), complexMatch(a2), complexMatch(a3)];

    return result;
}

function complexMatch(ClosedBar1|ClosedBar2|string a) returns string {
    match a {
        var {var1, var2: {var1: v1, var2}} => return "Matched with three vars : " + io:sprintf("%s", var1) + ", " + io:sprintf("%s", v1) +
                                    ", " + io:sprintf("%s", var2);
        var {var1, var2} => return "Matched with two vars : " + io:sprintf("%s", var1) + ", " +
                                    io:sprintf("%s", var2);
        var s => return "Matched with single var : " + io:sprintf("%s", s);
    }

    return "Default";
}
