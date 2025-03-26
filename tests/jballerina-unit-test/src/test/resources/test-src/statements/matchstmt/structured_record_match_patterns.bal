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

type FooSRMP record {
    string s;
    int i;
    float f;
};

function testStructuredMatchPatternsBasic1() returns string {
    FooSRMP foo = {s: "S", i: 23, f: 5.6};

    match foo {
        var {s, i: integer, f} => {
            return "Matched Values : " + s + ", " + integer.toString() + ", " + f.toString();
        }
    }

    return "Default";
}

type BarSRMP record {
    byte b;
    FooSRMP f;
};

function testStructuredMatchPatternsBasic2() returns string {
    FooSRMP foo = {s: "S", i: 23, f: 5.6};
    BarSRMP bar = {b: 12, f: foo};

    match bar {
        var {b: byteValue, f: {s, i, f}} => {
            return "Matched Values : " + s + ", " + i.toString() + ", " +
            f.toString() + ", " + byteValue.toString();
        }
    }

    return "Default";
}

function testStructuredMatchPatternsBasic3() returns string {
    FooSRMP foo = {s: "S", i: 23, f: 5.6};
    BarSRMP bar = {b: 12, f: foo};

    match bar {
        var {b, f} => {
            return "Matched Values : " + b.toString() + ", " + f.toString();
        }
    }

    return "Default";
}

function testStructuredMatchPatternsBasic4() returns string {
    FooSRMP foo = {s: "S", i: 23, f: 5.6};
    BarSRMP bar = {b: 12, f: foo};

    match bar {
        var {a} => {
            return "Matched Values : " + a.toString();
        }
        var y => {
            return "Matched Values : " + y.toString();
        }
    }
}

type ClosedFoo1 record {|
    string var1;
    int var2;
|};

type ClosedFoo2 record {|
    float var1;
    boolean var2;
|};

type ClosedFoo3 record {|
    string var1;
    int var2;
    boolean var3;
|};

type ClosedFoo4 record {|
    string var1;
|};

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
        var {var1, var2, var3} => {
            return "Matched with three vars : " + var1.toString() + ", " +
            var2.toString() + ", " + var3.toString();
        }
        var {var1, var2} => {
            return "Matched with two vars : " + var1.toString() + ", " + var2.toString();
        }
        var {var1} => {
            return "Matched with single var : " + var1.toString();
        }
    }

    return "Default";
}

type ClosedBar1 record {|
    string var1;
    int var2;
|};

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
        var {var1, var2: {var1: v1, var2}} => {
            return "Matched with three vars : " + var1.toString() + ", " + v1.toString() +
            ", " + var2.toString();
        }
        var {var1, var2} => {
            return "Matched with two vars : " + var1.toString() + ", " + var2.toString();
        }
        var s => {
            return "Matched with single var : " + s.toString();
        }
    }
}

function testRuntimeCheck() returns string[] {
    [int, boolean] tuple = [50, true];
    FooSRMP foo1 = {s: "S", i: 23, f: 5.6, "t": tuple};
    FooSRMP foo2 = {s: "S", i: 23, f: 5.6};
    FooSRMP foo3 = {s: "S", i: 23, f: 5.6, "t": 12};

    string[] values = [
        matchRuntimeCheck(foo1),
        matchRuntimeCheck(foo2),
        matchRuntimeCheck(foo3),
        matchRuntimeCheckWithAny(foo1),
        matchRuntimeCheckWithAny(foo2),
        matchRuntimeCheckWithAny(foo3)
    ];
    return values;
}

function matchRuntimeCheck(FooSRMP foo) returns string {
    match foo {
        var {s, i, f, t: [i2, b]} => {
            return "Matched with five vars : " + s.toString() + ", " + i.toString() +
            ", " + f.toString() + ", " + i2.toString() + ", " + b.toString();
        }
        var {s, i, f, t} => {
            return "Matched with four vars : " + s.toString() + ", " + i.toString() +
            ", " + f.toString() + ", " + t.toString();
        }
        var {s, i, f} => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() +
            ", " + f.toString();
        }
    }
    return "Default";
}

function matchRuntimeCheckWithAny(any foo) returns string {
    match foo {
        var {s, i, f, t: [i2, b]} => {
            return "Matched with five vars : " + toString(s) + ", " + toString(i) +
            ", " + toString(f) + ", " + toString(i2) + ", " + toString(b);
        }
        var {s, i, f, t} => {
            return "Matched with four vars : " + toString(s) + ", " + toString(i) +
            ", " + toString(f) + ", " + toString(t);
        }
        var {s, i, f} => {
            return "Matched with three vars : " + toString(s) + ", " + toString(i) +
            ", " + toString(f);
        }
    }
    return "Default";
}

function testStructuredMatchPatternWithTypeGuard1() returns string[] {
    ClosedBar1 bar1 = {var1: "Ballerina", var2: 500};
    ClosedBar2 bar2 = {var1: "Language", var2: bar1};

    [string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float a1 = ["Hello", 45];
    [string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float a2 = bar1;
    [string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float a3 = bar2;
    [string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float a4 = [455, true];
    [string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float a5 = 5.6;

    string[] result = [typeGuard1(a1), typeGuard1(a2), typeGuard1(a3), typeGuard1(a4), typeGuard1(a5)];

    return result;
}

function typeGuard1([string, int] | ClosedBar1 | ClosedBar2 | [int, boolean] | int | float x) returns string {
    match x {
        var [s, i] if s is string => {
            return "Matched with string : " + s + " added text with " + i.toString();
        }
        var {var1, var2} if var2 is int => {
            return "Matched with record int : " + var1.toString() + " with " + (var2 + 12).toString();
        }
        var {var1, var2} if var2 is ClosedBar1 => {
            return "Matched with record with ClosedBar1 : " + var1.toString() + " with " + var2.var1.toString();
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + s.toString() + ", " + i.toString();
        }
        var y => {
            return "Matched with default type - float : " + y.toString();
        }
    }
}

function testStructuredMatchPatternWithTypeGuard2() returns string[] {
    ClosedBar1 bar1 = {var1: "Ballerina", var2: 500};
    ClosedBar2 bar2 = {var1: "Language", var2: bar1};

    ClosedBar1 | ClosedBar2 | int | float a1 = bar1;
    ClosedBar1 | ClosedBar2 | int | float a2 = bar2;

    string[] result = [typeGuard2(a1), typeGuard2(a2), typeGuard2(true)];

    return result;
}

function typeGuard2(any matchExpr) returns string {
    match matchExpr {
        var {var1, var2} if var2 is string => {
            return "Matched with string";
        }
        var {var1, var2} if (var1 is int && var2 is int) => {
            return "Matched with int and int : " + var1.toString();
        }
        var {var1, var2} if (var1 is string && var2 is int) => {
            return "Matched with string and int : " + var1.toString();
        }
        var {var1, var2} if (var1 is int && var2 is ClosedBar1) => {
            return "Matched with int and ClosedBar1 : " + var1.toString();
        }
        var {var1, var2} if (var1 is string && var2 is ClosedBar1) => {
            return "Matched with string and ClosedBar1 : " + var2.var1.toString();
        }
        var x => {
            return "Matched with Default";
        }
    }
}

type FooRec record {
    [int, string] var1;
    [float, boolean] var2;
};

type BarRec record {
    [decimal, int] var1;
    [float, FooRec] var2;
};

function testStructuredMatchPatternWithTypeGuard3() returns string[] {
    FooRec foo = {var1: [12, "Bal"], var2: [12.5, true]};
    BarRec bar = {var1: [12.1, 100], var2: [400.1, foo]};

    string[] result = [typeGuard3(foo), typeGuard3(bar), typeGuard3(true)];

    return result;
}

function typeGuard3(any matchExpr) returns string {
    match matchExpr {
        var {var1, var2: [x, y]} if (x is float && y is boolean) => {
            return "Matched with foo : " + (x + 6.1).toString();
        }
        var {var1, var2: [x, {var1: y}]} if (x is float && y is [string, string]) => {
            return "Matched with nothing : " + (x + 5f).toString();
        }
        var {var1, var2: [x, {var1: y, var2: z}]} if (y is [int, string] && z is [float, boolean]) => {
            return "Matched with bar : " + (y[0] + 5).toString();
        }
        var y => {
            return "Matched with default : " + y.toString();
        }
    }
}

type RestParam record {|
    int var1;
    boolean...;
|};

type ClosedRec record {|
    string var1;
|};

function testStructuredMatchPatternWithTypeGuard4() returns string[] {
    RestParam foo1 = {var1: 500};
    RestParam foo2 = {var1: 500, "var2": true};
    RestParam foo3 = {var1: 500, "var2": true, "var3": true};
    ClosedRec bar1 = {var1: "Bal"};

    string[] result = [typeGuard4(foo1), typeGuard4(foo2), typeGuard4(foo3), typeGuard4(bar1)];

    return result;
}

function typeGuard4(RestParam | ClosedRec matchExpr) returns string {
    match matchExpr {
        var {var1} if var1 is string => {
            return "Matched with string : " + var1.toString();
        }
        var {var1, ...rest} => {
            return "Matched with restparam : " + rest.toString();
        }
        var y => {
            return "Matched with default : " + y.toString();
        }
    }
}

function testClosedRecord() returns string[] {
    RestParam rec = {var1: 500};
    RestParam rec2 = {var1: 500, "var2": true};

    string[] results = [matchClosedRecordPattern(rec), matchClosedRecordPattern(rec2)];

    return results;
}

function matchClosedRecordPattern(any matchExpr) returns string {
    match matchExpr {
        var {var1, var2, ...rest} => {
            return "Matched with opened pattern";
        }
        var {var1} => {
            return "Matched with closed pattern";
        }
    }

    return "Default";
}

function testStructuredMatchPatternWithEmptyRecord() returns string[] {
    record {} rec = {};
    string[] result = [];
    result[result.length()] = foo8(rec);

    rec["a"] = 1;
    result[result.length()] = foo8(rec);

    rec["b"] = 2;
    result[result.length()] = foo8(rec);

    rec["c"] = 3;
    result[result.length()] = foo8(rec);

    rec["d"] = 4;
    result[result.length()] = foo8(rec);

    return result;
}

function foo8(any x) returns string {
    match x {
        var {a, b, c} => {
            return "Matched with a: " + toString(a) + ", b: " + toString(b) + ", c: " + toString(c);
        }
        var {a, b} => {
            return "Matched with a: " + toString(a) + ", b: " + toString(b);
        }
        var {a} => {
            return "Matched with a: " + toString(a);
        }
        var {} => {
            return "Matched with empty record";
        }
        var s => {
            return "Matched with default";
        }
    }
}

function toString(any|error val) returns string {
    return val is any ? val.toString() : val.toString();
}
