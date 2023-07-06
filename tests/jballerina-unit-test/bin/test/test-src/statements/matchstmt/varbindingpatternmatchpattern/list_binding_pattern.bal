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

function listBindingPattern1(any v) returns string {
    match v {
        var [a] => {
            return "var [a]";
        }
        var [a, b] => {
            return "var [a, b]";
        }
        var [a, b, c] => {
            return "var [a, b, c]";
        }
    }
    return "No match";
}

function testListBindingPattern1() {
    assertEquals("var [a]", listBindingPattern1([1]));
    assertEquals("var [a, b]", listBindingPattern1([1, "str"]));
    assertEquals("var [a, b, c]", listBindingPattern1([1, "str", true]));
    assertEquals("No match", listBindingPattern1(1));
}

function listBindingPattern2(any v) returns string {
    match v {
        var [a, b] if a is int => {
            return "var [a, b] if a is int";
        }
        var [a, b] if b is int => {
            return "var [a, b] if b is int";
        }
    }
    return "No match";
}

function testListBindingPattern2() {
    assertEquals("var [a, b] if a is int", listBindingPattern2([1, 2]));
    assertEquals("var [a, b] if b is int", listBindingPattern2(["str", 2]));
    assertEquals("No match", listBindingPattern2([1]));
}

function listBindingPattern3(any v) returns string {
    match v {
        [var [a, b], 5]|[var [a, b], 7] => {
            return "match2";
        }
    }

    return "No match";
}

function testListBindingPattern3() {
    assertEquals("match2", listBindingPattern3([["s", true], 5]));
    assertEquals("match2", listBindingPattern3([[10, false], 7]));
    assertEquals("No match", listBindingPattern3([1]));
}

function listBindingPattern4(any v) returns string {
    match v {
        [var [a], 5]|[var [a], 7] => {
            return "match1";
        }
        [var [a], 2]|[3, var [a]] => {
            return "match2";
        }
    }

    return "No match";
}

function testListBindingPattern4() {
    assertEquals("match1", listBindingPattern4([[2], 5]));
    assertEquals("match1", listBindingPattern4([[2], 7]));
    assertEquals("match2", listBindingPattern4([[2], 2]));
    assertEquals("match2", listBindingPattern4([3, [8]]));
    assertEquals("No match", listBindingPattern4([1, 1]));
}

function listBindingPattern5([[int], int] v) returns int|string {
    match v {
        [var [a], 5]|[var [a], 7] => {
            return a;
        }
        [var [a], 2]|[var [a], 3] => {
            return a + 20;
        }
    }

    return "No match";
}

function testListBindingPattern5() {
    assertEquals(2, listBindingPattern5([[2], 5]));
    assertEquals(2, listBindingPattern5([[2], 7]));
    assertEquals(22, listBindingPattern5([[2], 2]));
    assertEquals(28, listBindingPattern5([[8], 3]));
    assertEquals("No match", listBindingPattern5([[1], 1]));
}

function listBindingPattern6([[int, int], int]|[[boolean, boolean], boolean] v) returns int|boolean {
    match v {
        [var [a, b], 5]|[var [a, b], false] => {
            return a;
        }
    }

    return false;
}

function testListBindingPattern6() {
    assertEquals(2, listBindingPattern6([[2, 2], 5]));
    assertEquals(true, listBindingPattern6([[true, true], false]));
    assertEquals(false, listBindingPattern6([[1, 1], 1]));
}

function listBindingPattern7(any v) returns string {
    match v {
        var [a, b, [c]] => {
            return "match1";
        }
        var [a, b, [c, d]] => {
            return "match2";
        }
        _ => {
            return "No match";
        }
    }
}

function testListBindingPattern7() {
    assertEquals("match1", listBindingPattern7([1, "str", [true]]));
    assertEquals("match2", listBindingPattern7([1, "str", [true, false]]));
    assertEquals("No match", listBindingPattern7([1, "str"]));
}

function listBindingPattern8(any v) returns string {
    match v {
        [1, 2, var [c, d, e], 3]|[1, 2, var [c, d, e]] => {
            return "match1";
        }
        [1, 3, var [c, d, e]] => {
            return "match2";
        }
        _ => {
            return "No match";
        }
    }
}

function testListBindingPattern8() {
    assertEquals("match1", listBindingPattern8([1, 2, [true, true, false], 3]));
    assertEquals("match1", listBindingPattern8([1, 2, [true, true, false]]));
    assertEquals("match2", listBindingPattern8([1, 3, [true, true, false]]));
    assertEquals("No match", listBindingPattern8([1, 2, [true, false]]));
}

function listBindingPattern9(any v) returns string {
    match v {
        var [_, _] => {
            return "match1";
        }
        var [_] => {
            return "match2";
        }
        _ => {
            return "match3";
        }
    }
}

function testListBindingPattern9() {
    assertEquals("match1", listBindingPattern9([1, 2]));
    assertEquals("match1", listBindingPattern9([1, [2]]));
    assertEquals("match2", listBindingPattern9([[1, 2]]));
    assertEquals("match3", listBindingPattern9([[1, 2], 3, 4]));
}

function listBindingPattern10([string, int]|[float, boolean]|[float, string, boolean]|float a) returns string {
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

function testListBindingPattern10() {
    [string, int]|[float, boolean]|[float, string, boolean]|float a1 = 66.6;
    [string, int]|[float, boolean]|[float, string, boolean]|float a2 = ["Hello", 12];
    [string, int]|[float, boolean]|[float, string, boolean]|float a3 = [4.5, true];
    [string, int]|[float, boolean]|[float, string, boolean]|float a4 = [6.7, "Test", false];

    assertEquals("Matched with single var : 66.6", listBindingPattern10(a1));
    assertEquals("Matched with two vars : Hello, 12", listBindingPattern10(a2));
    assertEquals("Matched with two vars : 4.5, true", listBindingPattern10(a3));
    assertEquals("Matched with three vars : 6.7, Test, false", listBindingPattern10(a4));
}

function listBindingPattern11([string, int]|[float, [string, boolean]]|[float, [string, [boolean, int]]]|float a)
                                                                                                        returns string {
    match a {
        var [f, [s, [b, i]]] => {
            return "Matched with four vars : " + f.toString() + ", " + s.toString() + ", " + i.toString() + ", "
            + b.toString();
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

function testListBindingPattern11() {
    float a1 = 66.6;
    [string, int] a2 = ["Hello", 34];
    [float, [string, [boolean, int]]] a3 = [66.6, ["Test", [true, 456]]];
    [float, [string, boolean]] a4 = [5.6, ["Ballerina", false]];

    assertEquals("Matched with single var : 66.6", listBindingPattern11(a1));
    assertEquals("Matched with two vars : Hello, 34", listBindingPattern11(a2));
    assertEquals("Matched with four vars : 66.6, Test, 456, true", listBindingPattern11(a3));
    assertEquals("Matched with three vars : 5.6, Ballerina, false", listBindingPattern11(a4));
}

function listBindingPattern12(any x) returns string {
    match x {
        var [s, i] if s is string => {
            return "Matched with string : " + s + " added text with " + (checkpanic i).toString();
        }
        var [s, i] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + (checkpanic i).toString();
        }
        var [s, i] if i is int => {
            return "Matched with int : " + (checkpanic s).toString() + " with " + (i + 3456).toString();
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + (checkpanic s).toString() + ", " + i.toString();
        }
        var y => {
            return "Matched with default type - float : " + y.toString();
        }
    }
}

function testListBindingPattern12() {
    [string, int]|[float, boolean]|[boolean, int]|[int, boolean]|int|float a1 = ["Hello", 45];
    [string, int]|[float, boolean]|[boolean, int]|[int, boolean]|int|float a2 = [4.5, true];
    [string, int]|[float, boolean]|[boolean, int]|[int, boolean]|int|float a3 = [false, 4];
    [int, boolean] ib = [455, true];
    [string, int]|[float, boolean]|[boolean, int]|[int, boolean]|int|float a4 = ib;
    [string, int]|[float, boolean]|[boolean, int]|[int, boolean]|float a5 = 5.6;

    assertEquals("Matched with string : Hello added text with 45", listBindingPattern12(a1));
    assertEquals("Matched with float : 9.0 with true", listBindingPattern12(a2));
    assertEquals("Matched with int : false with 3460", listBindingPattern12(a3));
    assertEquals("Matched with boolean : 455, true", listBindingPattern12(a4));
    assertEquals("Matched with default type - float : 5.6", listBindingPattern12(a5));
}

function listBindingPattern13(any x) returns string {
    match x {
        var [s, i, f] if s is string => {
            return "Matched with string : " + s + " added text with " + (checkpanic i).toString();
        }
        var [s, [i, f]] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + (checkpanic i).toString()
                                                                                + " and " + (checkpanic f).toString();
        }
        var [[s, i], f] if i is int => {
            return "Matched with int : " + (checkpanic s).toString() + " with " + (i + 3456).toString()
                                                                                + " and " + (checkpanic f).toString();
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + (checkpanic s).toString() + ", " + i.toString();
        }
    }
    return "Default";
}

function testListBindingPattern13() {
    [string, int, float]|[float, [boolean, int]]|[[boolean, int], float]|[int, boolean]|float
                                                                                        a1 = ["Hello", 45, 5.6];
    [string, int, float]|[float, [boolean, int]]|[[boolean, int], float]|[int, boolean]|float
                                                                                        a2 = [5.7, [true, 67]];
    [string, int, float]|[float, [boolean, int]]|[[boolean, int], float]|[int, boolean]|float
                                                                                        a3 = [[true, 67], 7.8];
    [string, int, float]|[float, [boolean, int]]|[[boolean, int], float]|[int, boolean]|float
                                                                                        a4 = [678, false];
    [string, int, float]|[float, [boolean, int]]|[[boolean, int], float]|[int, boolean]|float a5 = 67.89;

    assertEquals("Matched with string : Hello added text with 45", listBindingPattern13(a1));
    assertEquals("Matched with float : 10.2 with true and 67", listBindingPattern13(a2));
    assertEquals("Matched with int : true with 3523 and 7.8", listBindingPattern13(a3));
    assertEquals("Matched with boolean : 678, false", listBindingPattern13(a4));
    assertEquals("Default", listBindingPattern13(a5));
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

function listBindingPattern14(any a) returns string {
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

function testListBindingPattern14() {
    FooRec fooRec1 = {s: "S", i: 23, f: 5.6};
    BarRec barRec1 = {b: 12, f: fooRec1};

    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a1 = [fooRec1, barRec1];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a2 = [fooRec1, 4.5];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a3 = [barRec1, fooRec1];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a4 = [barRec1, 543];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a5 = [5.2, fooRec1];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a6 = [15, barRec1];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a7 = [65, 7.4];
    [int|FooRec, float|BarRec]|[float|BarRec, int|FooRec] a8 = [3.6, 42];

    assertEquals("Matched with FooRec and BarRec : {\"s\":\"S\",\"i\":23,\"f\":5.6} , " +
                                "{\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}", listBindingPattern14(a1));
    assertEquals("Matched with FooRec and float : {\"s\":\"S\",\"i\":23,\"f\":5.6} , 4.5", listBindingPattern14(a2));
    assertEquals("Matched with BarRec and FooRec : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} , " +
                                "{\"s\":\"S\",\"i\":23,\"f\":5.6}", listBindingPattern14(a3));
    assertEquals("Matched with BarRec and int : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} , 543",
                                listBindingPattern14(a4));
    assertEquals("Matched with float and FooRec : 5.2 , {\"s\":\"S\",\"i\":23,\"f\":5.6}", listBindingPattern14(a5));
    assertEquals("Matched with int and BarRec : 15 , {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}",
                                listBindingPattern14(a6));
    assertEquals("Default", listBindingPattern14(a7));
    assertEquals("Default", listBindingPattern14(a8));
}

function listBindingPattern15() returns string {
    [boolean, string]|[int, string, decimal] v = [1, "A", 1.1d];
    match v {
        var [i, ...s] => {
            return "i: " + i.toString() + " s: " + s.toString();
        }
    }
}

function testListBindingPattern15() {
    assertEquals("i: 1 s: [\"A\",1.1]", listBindingPattern15());
}

function listBindingPattern16(int[] v) returns int {
    match v {
        var [a, b, ...c] => {
            return a + b + c[0];
        }
    }
    return -1;
}

function testListBindingPattern16() {
    assertEquals(6, listBindingPattern16([1, 2, 3, 4, 5]));
}

function listBindingPattern17(int[5] v) returns int {
    match v {
        var [a, b, ...c] => {
            return a + b + c[1];
        }
    }
}

function testListBindingPattern17() {
    assertEquals(7, listBindingPattern17([1, 2, 3, 4, 5]));
}

function listBindingPattern18(any[] v) returns string {
    match v {
        var [a, b, ...c] => {
            return a.toString();
        }
    }
    return "No Match";
}

function testListBindingPattern18() {
    assertEquals("1", listBindingPattern18([1, 2, 3, 4, 5]));
}

function listBindingPattern19(int[] v) returns int {
    match v {
        var [a, b] => {
            return a + b;
        }
    }
    return -1;
}

function testListBindingPattern19() {
    assertEquals(3, listBindingPattern19([1, 2]));
    assertEquals(-1, listBindingPattern19([1, 2, 3]));
}

function listBindingPattern20(int[3] a) returns int {
    match a {
        var [x, _, z] => {
            return x + z;
        }
    }
}

function testListBindingPattern20() {
    assertEquals(4, listBindingPattern20([1, 2, 3]));
}

type Person record {
    string name;
};

function listBindingPattern21(string[]|Person v) returns string {
    match v {
        var [a, b] => {
            return "string[2]";
        }
        _ => {
            return "other";
        }
    }
}

function testListBindingPattern21() {
    assertEquals("string[2]", listBindingPattern21(["hello", "world"]));
    assertEquals("other", listBindingPattern21(["hello"]));
    assertEquals("other", listBindingPattern21(["hello", "world", "ballerina"]));
    assertEquals("other", listBindingPattern21({name: "May"}));
}

function listBindingPattern22(json j) returns json {
    match j {
        var [x] => {
            return x;
        }
    }
    return ();
}

function testListBindingPattern22() {
    assertEquals("hello", listBindingPattern22(["hello"]));
    assertEquals(1, listBindingPattern22([1]));
    assertEquals((), listBindingPattern22([()]));
    assertEquals((), listBindingPattern22({a: "hello world", x1: 1}));
    assertEquals((), listBindingPattern22([]));
    assertEquals((), listBindingPattern22([1, 2, 3]));
    assertEquals((), listBindingPattern22(1));
}

function listBindingPattern23(any a) returns string {
    match a {
        var [] => {
            return "match1";
        }
        var [s, [...i]] => {
            return "match2";
        }
        var [...i] => {
            return "match3";
        }
        _ => {
            return "No match";
        }
    }
}

function testListBindingPattern23() {
    assertEquals("match1", listBindingPattern23([]));
    assertEquals("match2", listBindingPattern23(["str", [1, 2]]));
    assertEquals("match3", listBindingPattern23([1,2,3]));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
