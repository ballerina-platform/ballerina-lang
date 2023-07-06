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

function testStructuredMatchPatternsBasic1() returns string {
    [string, int, float] a = ["S", 23, 5.6];

    match a {
        var [s, i, f] => {
            return "Matched Values : " + s + ", " + i.toString() + ", " + f.toString();
        }
    }
}

function testStructuredMatchPatternsBasic2() returns string {
    [string, [int, float]] a = ["S", [23, 5.6]];

    match a {
        var [s, [i, f]] => {
            return "Matched Values : " + s + ", " + i.toString() + ", " + f.toString();
        }
    }
}

function testStructuredMatchPatternsBasic3() returns string {
    [string, [int, float]] a = ["S", [23, 5.6]];

    match a {
        var [s, t] => {
            return "Matched Values : " + s.toString() + ", " + t.toString();
        }
    }
}

function testStructuredMatchPatternsBasics5() returns string[] {
    [string, int] | [float, boolean] | [float, string, boolean] | float a1 = 66.6;
    [string, int] | [float, boolean] | [float, string, boolean] | float a2 = ["Hello", 12];
    [string, int] | [float, boolean] | [float, string, boolean] | float a3 = [4.5, true];
    [string, int] | [float, boolean] | [float, string, boolean] | float a4 = [6.7, "Test", false];

    string[] result = [foo1(a1), foo1(a2), foo1(a3), foo1(a4)];

    return result;
}

function foo1([string, int] | [float, boolean] | [float, string, boolean] | float a) returns string {
    match a {
        var [s, i] => {
            return "Matched with two vars : " + s.toString() + ", " + i.toString();
        }
        var [s, i, b] => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() + ", " + b.toString();
        }
        var s => {
            return "Matched with single var : " + s.toString();
        }
    }
}


function testStructuredMatchPatternComplex1() returns string[] {
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a1 = 66.6;
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a2 = ["Hello", 34];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a3 = [66.6, ["Test", [true, 456]]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a4 = [5.6, ["Ballerina", false]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a5 = ["Bal", 543, new(4)];

    string[] result = [bar1(a1), bar1(a2), bar1(a3), bar1(a4), bar1(a5)];

    return result;
}

function bar1([string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a) returns string {
    match a {
        var [f, [s, [b, i]]] => {
            return "Matched with four vars : " + f.toString() + ", " + s.toString() +
            ", " + i.toString() + ", " + b.toString();
        }
        var [s, [i, b]] => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() + ", " + b.toString();
        }
        var [s, i] => {
            return "Matched with two vars : " + s.toString() + ", " + i.toString();
        }
        var s => {
            return "Matched with single var : " + s.toString();
        }
    }
}


function testStructuredMatchPatternComplex2() returns string[] {
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a1 = 66.6;
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a2 = ["Hello", 34];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a3 = [66.6, ["Test", [true, 456]]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a4 = [5.6, ["Ballerina", false]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a5 = ["Bal", 543, new(4)];

    string[] result = [baz1(a1), baz1(a2), baz1(a3), baz1(a4), baz1(a5)];

    return result;
}

function baz1([string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a) returns string {
    match a {
        var [s, [i, b]] => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() + ", " + b.toString();
        }
        var [s, i] => {
            return "Matched with two vars : " + s.toString() + ", " + i.toString();
        }
    }

    return "Default";
}

function bar2(any a) returns string {
    match a {
        var [f, [s, [b, i]]] => {
            return "Matched with four vars : " + toString(f) + ", " + toString(s) +
            ", " + toString(i) + ", " + toString(b);
        }
        var [s, [i, b]] => {
            return "Matched with three vars : " + toString(s) + ", " + toString(i) + ", " + toString(b);
        }
        var [s, i] => {
            return "Matched with two vars : " + toString(s) + ", " + toString(i);
        }
        var s => {
            return "Matched with single var : " + toString(s);
        }
    }
}

function testStructuredMatchPatternComplex3() returns string[] {
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a1 = 66.6;
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a2 = ["Hello", 34];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a3 = [66.6, ["Test", [true, 456]]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a4 = [5.6, ["Ballerina", false]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a5 = ["Bal", 543, new(4)];

    string[] result = [bar2(a1), bar2(a2), bar2(a3), bar2(a4), bar2(a5)];

    return result;
}

function baz2(any a) returns string {
    match a {
        var [s, [i, b]] => {
            return "Matched with three vars : " + toString(s) + ", " + toString(i) + ", " + toString(b);
        }
        var [s, i] => {
            return "Matched with two vars : " + toString(s) + ", " + toString(i);
        }
    }

    return "Default";
}

function testStructuredMatchPatternComplex4() returns string[] {
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a1 = 66.6;
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a2 = ["Hello", 34];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a3 = [66.6, ["Test", [true, 456]]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a4 = [5.6, ["Ballerina", false]];
    [string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float | [string, int, NoFillerObject] a5 = ["Bal", 543, new(2)];

    string[] result = [baz2(a1), baz2(a2), baz2(a3), baz2(a4), baz2(a5)];

    return result;
}


function testStructuredMatchPatternWithTypeGuard1() returns string[] {
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a1 = ["Hello", 45];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a2 = [4.5, true];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a3 = [false, 4];
    [int, boolean] ib = [455, true];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a4 = ib;
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | float a5 = 5.6;

    string[] result = [foo3(a1), foo3(a2), foo3(a3), foo3(a4), foo3(a5)];

    return result;
}

function foo3(any x) returns string {

    match x {
        var [s, i] if s is string => {
            return "Matched with string : " + s + " added text with " + toString(i);
        }
        var [s, i] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + toString(i);
        }
        var [s, i] if i is int => {
            return "Matched with int : " + toString(s) + " with " + (i + 3456).toString();
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + toString(s) + ", " + toString(i);
        }
        var y => {
            return "Matched with default type - float : " + toString(y);
        }
    }
}

function testStructuredMatchPatternWithTypeGuard2() returns string[] {
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a1 = ["Hello", 45, 5.6];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a2 = [5.7, [true, 67]];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a3 = [[true, 67], 7.8];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a4 = [678, false];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a5 = 67.89;

    string[] result = [foo4(a1), foo4(a2), foo4(a3), foo4(a4), foo4(a5)];

    return result;
}


function foo4(any x) returns string {

    match x {
        var [s, i, f] if s is string => {
            return "Matched with string : " + s + " added text with " + toString(i);
        }
        var [s, [i, f]] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + toString(i) + " and " + toString(f);
        }
        var [[s, i], f] if i is int => {
            return "Matched with int : " + toString(s) + " with " + (i + 3456).toString() + " and " + toString(f);
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + toString(s) + ", " + toString(i);
        }
    }

    return "Default";
}


function testStructuredMatchPatternWithTypeGuard3() returns string[] {
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a1 = ["Hello", 45, 5.6];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a2 = [5.7, [true, 67]];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a3 = [[true, 67], 7.8];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a4 = [678, false];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a5 = 67.89;

    string[] result = [foo5(a1), foo5(a2), foo5(a3), foo5(a4), foo5(a5)];

    return result;
}

function foo5(any x) returns string {

    match x {
        var [s, i, f] if (s is string && i is int && f is float) => {
            return "Matched with string : " + s + " added text with " + (i + 5).toString() +
            " with " + (f + 5.4).toString();
        }
        var [s, [i, f]] if (s is float && i is boolean) => {
            return "Matched with float : " + (s + 4.5).toString() + " and boolean with " +
            toString(i) + " and " + toString(f);
        }
        var [[s, i], f] if (i is int && (s is boolean && f is float)) => {
            return "Matched with boolean : " + toString(s) + " and int with " +
            (i + 3456).toString() + " and float with " + toString(f);
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + toString(s) + ", " + toString(i);
        }
    }

    return "Default";
}


type FooRec record {
    string s;
    int i;
    float f;
};

type BarRec record {
    byte b;
    FooRec f;
};


function testStructuredMatchPatternWithTypeGuard4() returns string[] {
    FooRec fooRec1 = {s: "S", i: 23, f: 5.6};
    BarRec barRec1 = {b: 12, f: fooRec1};

    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a1 = [fooRec1, barRec1];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a2 = [fooRec1, 4.5];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a3 = [barRec1, fooRec1];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a4 = [barRec1, 543];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a5 = [5.2, fooRec1];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a6 = [15, barRec1];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a7 = [65, 7.4];
    [FooRec | int, BarRec | float] | [BarRec | float, FooRec | int] a8 = [3.6, 42];

    return [foo6(a1), foo6(a2), foo6(a3), foo6(a4), foo6(a5), foo6(a6), foo6(a7), foo6(a8)];
}

function foo6(any a) returns string {
    match a {
        var [i, s] if i is FooRec && s is BarRec => {
            return "Matched with FooRec and BarRec : " + i.toString() + " , " + s.toString();
        }
        var [i, s] if i is FooRec && s is float => {
            return "Matched with FooRec and float : " + i.toString() + " , " + s.toString();
        }
        var [i, s] if i is BarRec && s is FooRec => {
            return "Matched with BarRec and FooRec : " + i.toString() + " , " + s.toString();
        }
        var [i, s] if i is BarRec && s is int => {
            return "Matched with BarRec and int : " + i.toString() + " , " + s.toString();
        }
        var [i, s] if i is float && s is FooRec => {
            return "Matched with float and FooRec : " + i.toString() + " , " + s.toString();
        }
        var [i, s] if i is int && s is BarRec => {
            return "Matched with int and BarRec : " + i.toString() + " , " + s.toString();
        }
    }

    return "Default";
}

function testStructuredMatchPatternWithTypeGuard5() returns string[] {
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a1 = ["Hello", 45, 5.6];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a2 = [5.7, [true, 67]];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a3 = [[true, 67], 7.8];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a4 = [678, false];
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a5 = 876;
    [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | int | float a6 = 67.89;

    string[] result = [foo7(a1), foo7(a2), foo7(a3), foo7(a4), foo7(a5), foo7(a6)];

    return result;
}

function foo7(any x) returns string {

    match x {
        var [s, i, f] if (s is string && i is int && f is float) => {
            return "Matched with string : " + s + " added text with " + (i + 5).toString() +
            " with " + (f + 5.4).toString();
        }
        var [s, [i, f]] if (s is float && i is boolean) => {
            return "Matched with float : " + (s + 4.5).toString() + " and boolean with " +
            toString(i) + " and " + toString(f);
        }
        var [[s, i], f] if (i is int && (s is boolean && f is float)) => {
            return "Matched with boolean : " + s.toString() + " and int with " +
            (i + 3456).toString() + " and float with " + f.toString();
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + toString(s) + ", " + toString(i);
        }

        var s if s is int => {
            return "Matched with int : " + s.toString();
        }

        var s => {
            return "Default";
        }
    }
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
        var [] => {
            return "Matched with empty array";
        }
        var [i] => {
            return "Matched with i: " + toString(i);
        }
        var [i, j] => {
            return "Matched with i: " + toString(i) + ", j: " + toString(j);
        }
        var [i, j, k] => {
            return "Matched with i: " + toString(i) + ", j: " + toString(j) + ", k: " + toString(k);
        }
        var s => {
            return "Matched with default";
        }
    }
}

class NoFillerObject {
    int i;

    function init(int i) {
        self.i = i;
    }
}

function toString(any|error val) returns string {
    return val is any ? val.toString() : val.toString();
}
