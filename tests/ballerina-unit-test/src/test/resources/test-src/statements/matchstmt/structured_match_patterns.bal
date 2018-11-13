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

    string[] result = [foo(a1), foo(a2), foo(a3), foo(a4)];

    return result;
}

function foo((string, int)|(float, boolean)|(float, string, boolean)|float a) returns string {
    match a {
        var (s, i) => return "Matched with two vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
        var (s, i, b) => return "Matched with three vars : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i) + ", " +
                            io:sprintf("%s", b);
        var s => return "Matched with single var : " + io:sprintf("%s", s);
    }

    return "Default";
}


function testStructuredMatchPatternComplex1() returns string[] {
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float a1 = 66.6;
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float a2 = ("Hello", 34);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float a3 = (66.6, ("Test", (true, 456)));
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float a4 = (5.6, ("Ballerina", false));

    string[] result = [bar(a1), bar(a2), bar(a3), bar(a4)];

    return result;
}

function bar((string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float a) returns string {
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