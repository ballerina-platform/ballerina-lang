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

function testStructuredMatchPatternsBasic1() returns string {
    (string, int, float) a = ("S", 23, 5.6);

    match a {
        var (s, i, f) => return "Matched Values : " + s + ", " + i + ", " + f;
    }

    return "Default";
}

function testStructuredMatchPatternsBasic2() returns string {
    (string, (int, float)) a = ("S", (23, 5.6));

    match a {
        var (s, (i, f)) => return "Matched Values : " + s + ", " + i + ", " + f;
    }

    return "Default";
}

function testStructuredMatchPatternsBasic3() returns string {
    (string, (int, float)) a = ("S", (23, 5.6));

    match a {
        var (s, t) => return "Matched Values : " + io:sprintf("%s", s) + ", " +  io:sprintf("%s", t);
    }

    return "Default";
}

function testStructuredMatchPatternsBasic4() returns string {
    (string, (int, float)) a = ("S", (23, 5.6));

    match a {
        var y => return "Matched Values : " + io:sprintf("%s", y);
    }

    return "Default";
}

function testStructuredMatchPatternsBasics5() returns string[] {
    (string, int)|(float, boolean)|(float, string, boolean)|float a1 = 66.6;
    (string, int)|(float, boolean)|(float, string, boolean)|float a2 = ("Hello", 12);
    (string, int)|(float, boolean)|(float, string, boolean)|float a3 = (4.5, true);
    (string, int)|(float, boolean)|(float, string, boolean)|float a4 = (6.7, "Test", false);

    string[] result = [foo1(a1), foo1(a2), foo1(a3), foo1(a4)];

    return result;
}

function foo1((string, int)|(float, boolean)|(float, string, boolean)|float a) returns string {
    match a {
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
        var (s, i, b) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i) + ", " +
                            io:sprintf("%s", b);
        var s => return "Matched with single var : " + io:sprintf("%s", s);
    }

    return "Default";
}


function testStructuredMatchPatternComplex1() returns string[] {
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a1 = 66.6;
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a2 = ("Hello", 34);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a3 = (66.6, ("Test", (true, 456)));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a4 = (5.6, ("Ballerina", false));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a5 = ("Bal", 543, 67.8);

    string[] result = [bar1(a1), bar1(a2), bar1(a3), bar1(a4), bar1(a5)];

    return result;
}

function bar1((string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a) returns string {
    match a {
        var (f, (s, (b, i))) => return "Matched with four vars : " + io:sprintf("%s", f) + ", " + io:sprintf("%s", s) +
                                    ", " + io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, (i, b)) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " +
                                    io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
        var s => return "Matched with single var : " + io:sprintf("%s", s);
    }

    return "Default";
}


function testStructuredMatchPatternComplex2() returns string[] {
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a1 = 66.6;
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a2 = ("Hello", 34);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a3 = (66.6, ("Test", (true, 456)));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a4 = (5.6, ("Ballerina", false));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a5 = ("Bal", 543, 67.8);

    string[] result = [baz1(a1), baz1(a2), baz1(a3), baz1(a4), baz1(a5)];

    return result;
}

function baz1((string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a) returns string {
    match a {
        var (s, (i, b)) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " +
                                    io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
    }

    return "Default";
}

function bar2(any a) returns string {
    match a {
        var (f, (s, (b, i))) => return "Matched with four vars : " + io:sprintf("%s", f) + ", " + io:sprintf("%s", s) +
                                    ", " + io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, (i, b)) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " +
                                    io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
        var s => return "Matched with single var : " + io:sprintf("%s", s);
    }

    return "Default";
}

function testStructuredMatchPatternComplex3() returns string[] {
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a1 = 66.6;
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a2 = ("Hello", 34);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a3 = (66.6, ("Test", (true, 456)));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a4 = (5.6, ("Ballerina", false));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a5 = ("Bal", 543, 67.8);

    string[] result = [bar2(a1), bar2(a2), bar2(a3), bar2(a4), bar2(a5)];

    return result;
}

function baz2(any a) returns string {
    match a {
        var (s, (i, b)) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " +
                                    io:sprintf("%s", i) + ", " + io:sprintf("%s", b);
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
    }

    return "Default";
}

function testStructuredMatchPatternComplex4() returns string[] {
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a1 = 66.6;
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a2 = ("Hello", 34);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a3 = (66.6, ("Test", (true, 456)));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a4 = (5.6, ("Ballerina", false));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float|(string, int, float) a5 = ("Bal", 543, 67.8);

    string[] result = [baz2(a1), baz2(a2), baz2(a3), baz2(a4), baz2(a5)];

    return result;
}


function testStructuredMatchPatternWithTypeGuard1() returns string[] {
    (string, int)|(float, boolean)|(boolean, int)|(int, boolean)|int|float a1 = ("Hello", 45);
    (string, int)|(float, boolean)|(boolean, int)|(int, boolean)|int|float a2 = (4.5, true);
    (string, int)|(float, boolean)|(boolean, int)|(int, boolean)|int|float a3 = (false, 4);
    (string, int)|(float, boolean)|(boolean, int)|(int, boolean)|int|float a4 = (455, true);
    (string, int)|(float, boolean)|(boolean, int)|(int, boolean)|float a5 = 5.6;

    string[] result = [foo3(a1), foo3(a2), foo3(a3), foo3(a4), foo3(a5)];

    return result;
}

function foo3(any x) returns string {

    match x {
        var (s, i) if s is string => {return "Matched with string : " + s + " added text with " + io:sprintf("%s", i);}
        var (s, i) if s is float => {return "Matched with float : " + io:sprintf("%s", s + 4.5) + " with " + io:sprintf("%s", i);}
        var (s, i) if i is int => {return "Matched with int : "+ io:sprintf("%s", s) + " with " + io:sprintf("%s" , i + 3456);}
        var (s, i) if i is boolean => {return "Matched with boolean : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);}
        var y => {return "Matched with default type - float : " + io:sprintf("%s", y);}
    }

    return "Default";
}

function testStructuredMatchPatternWithTypeGuard2() returns string[] {
    (string, int, float)|(float, (boolean, int))|((boolean, int), float)|(int, boolean)|float a1 = ("Hello", 45, 5.6);
    (string, int, float)|(float, (boolean, int))|((boolean, int), float)|(int, boolean)|float a2 = (5.7, (true, 67));
    (string, int, float)|(float, (boolean, int))|((boolean, int), float)|(int, boolean)|float a3 = ((true, 67), 7.8);
    (string, int, float)|(float, (boolean, int))|((boolean, int), float)|(int, boolean)|float a4 = (678, false);
    (string, int, float)|(float, (boolean, int))|((boolean, int), float)|(int, boolean)|float a5 = 67.89;

    string[] result = [foo4(a1), foo4(a2), foo4(a3), foo4(a4), foo4(a5)];

    return result;
}


function foo4(any x) returns string {

    match x {
        var (s, i, f) if s is string => {return "Matched with string : " + s + " added text with " + io:sprintf("%s", i);}
        var (s, (i, f)) if s is float => {return "Matched with float : " + io:sprintf("%s", s + 4.5) + " with " + io:sprintf("%s", i) + " and " +
                                          io:sprintf("%s", f);}
        var ((s, i), f) if i is int => {return "Matched with int : "+ io:sprintf("%s", s) + " with " + io:sprintf("%s" , i + 3456)+ " and " +
                                          io:sprintf("%s", f);}
        var (s, i) if i is boolean => {return "Matched with boolean : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);}
    }

    return "Default";
}

