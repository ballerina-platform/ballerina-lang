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

function listMatchPattern1(any v) returns string {
    match v {
        [1] => {
            return "[1]";
        }
        [1, 2] => {
            return "[1, 2]";
        }
        [1, 2, 3] => {
            return "[1, 2, 3]";
        }
        [1, "s"] => {
            return "[1, 's']";
        }
        [1, "s", 2] => {
            return "[1, 's', 2]";
        }
        [true, "s"] => {
            return "[true, 's']";
        }
        [true, false] => {
            return "[true, false]";
        }
        [true, 1, false] => {
            return "[true, 1, false]";
        }
    }

    return "No match";
}

function testListMatchPattern1() {
        assertEquals("[1]", listMatchPattern1([1]));
        assertEquals("[1, 2]", listMatchPattern1([1, 2]));
        assertEquals("[1, 2, 3]", listMatchPattern1([1, 2, 3]));
        assertEquals("[1, 's']", listMatchPattern1([1, "s"]));
        assertEquals("[1, 's', 2]", listMatchPattern1([1, "s", 2]));
        assertEquals("[true, 's']", listMatchPattern1([true, "s"]));
        assertEquals("[true, false]", listMatchPattern1([true, false]));
        assertEquals("[true, 1, false]", listMatchPattern1([true, 1, false]));
        assertEquals("No match", "No match");
}

function listMatchPattern2(any v) returns string {
    match v {
        [[1]] => {
            return "[[1]]";
        }
        [1, [2]] => {
            return "[1, [2]]";
        }
        [[1], [2]] => {
             return "[[1], [2]]";
        }
        [1, [2, 3]] => {
            return "[1, [2, 3]]";
        }
        [[1, "s"]] => {
            return "[[1, 's']]";
        }
        [[[1, "str"], "s"], 2] => {
            return "[[[1, 'str'], 's'], 2]";
        }
        [[true, 2], "s"] => {
            return "[[true, 2], 's']";
        }
        [[true, false], [true, 2]] => {
            return "[[true, false], [true, 2]]";
        }
        [[true, "s1"], [1, 2, "s2"], [false, "s3"]] => {
            return "[[true, 's1'], [1, 2, 's2'], [false, 's3']]";
        }
    }

    return "No match";
}

function testListMatchPattern2() {
    assertEquals("[[1]]", listMatchPattern2([[1]]));
    assertEquals("[1, [2]]", listMatchPattern2([1, [2]]));
    assertEquals("[[1], [2]]", listMatchPattern2([[1], [2]]));
    assertEquals("[1, [2, 3]]", listMatchPattern2([1, [2, 3]]));
    assertEquals("[[1, 's']]", listMatchPattern2([[1, "s"]]));
    assertEquals("[[[1, 'str'], 's'], 2]", listMatchPattern2([[[1, "str"], "s"], 2]));
    assertEquals("[[true, 2], 's']", listMatchPattern2([[true, 2], "s"]));
    assertEquals("[[true, false], [true, 2]]", listMatchPattern2([[true, false], [true, 2]]));
    assertEquals("[[true, 's1'], [1, 2, 's2'], [false, 's3']]",
                            listMatchPattern2([[true, "s1"], [1, 2, "s2"], [false, "s3"]]));
    assertEquals("No match", "No match");
}

const CONST1 = "Ballerina";
const CONST2 = 200;

function listMatchPattern3(any v) returns string {
    match v {
        [CONST1] => {
            return "[CONST1]";
        }
        [CONST1, CONST2] => {
            return "[CONST1, CONST2]";
        }
        [CONST2, CONST1] => {
            return "[CONST2, CONST1]";
        }
        [[CONST1, CONST2]] => {
            return "[[CONST1, CONST2]]";
        }
        [[CONST1], [CONST2]] => {
            return "[[CONST1], [CONST2]]";
        }
        [[CONST1], [CONST2], [CONST1, CONST2]] => {
            return "[[CONST1], [CONST2], [CONST1, CONST2]]";
        }
    }

    return "No match";
}

function testListMatchPattern3() {
    assertEquals("[CONST1]", listMatchPattern3([CONST1]));
    assertEquals("[CONST1]", listMatchPattern3(["Ballerina"]));
    assertEquals("[CONST1, CONST2]", listMatchPattern3(["Ballerina", 200]));
    assertEquals("[CONST1, CONST2]", listMatchPattern3([CONST1, CONST2]));
    assertEquals("[CONST2, CONST1]", listMatchPattern3([200, "Ballerina"]));
    assertEquals("[CONST2, CONST1]", listMatchPattern3([CONST2, CONST1]));
    assertEquals("[[CONST1, CONST2]]", listMatchPattern3([[CONST1, CONST2]]));
    assertEquals("[[CONST1, CONST2]]", listMatchPattern3([["Ballerina", 200]]));
    assertEquals("[[CONST1], [CONST2]]", listMatchPattern3([[CONST1], [CONST2]]));
    assertEquals("[[CONST1], [CONST2]]", listMatchPattern3([["Ballerina"], [200]]));
    assertEquals("[[CONST1], [CONST2], [CONST1, CONST2]]", listMatchPattern3([[CONST1], [CONST2], [CONST1, CONST2]]));
    assertEquals("[[CONST1], [CONST2], [CONST1, CONST2]]",
                                                    listMatchPattern3([["Ballerina"], [200], ["Ballerina", 200]]));
    assertEquals("No match", "No match");
}

function listMatchPattern4([int, string, CONST1] v) returns string {
    match v {
        [1, "str", CONST1] => {
            return "[1, 'str', CONST1]";
        }
    }

    return "No match";
}

function testListMatchPattern4() {
    assertEquals("[1, 'str', CONST1]", listMatchPattern4([1, "str", CONST1]));
    assertEquals("No match", listMatchPattern4([2, "str", CONST1]));
}

function listMatchPattern5([int, string, CONST1|CONST2] v) returns string {
    match v {
        [1, "s", CONST1] => {
            return "[1, 's', CONST1]";
        }
        [2, "a", CONST2] => {
            return "[2, 'a', CONST2]";
        }
        _ => {
            return "No match";
        }
    }
}

function testListMatchPattern5() {
    assertEquals("[1, 's', CONST1]", listMatchPattern5([1, "s", CONST1]));
    assertEquals("[2, 'a', CONST2]", listMatchPattern5([2, "a", CONST2]));
    assertEquals("No match", listMatchPattern5([3, "a", CONST2]));
}

function listMatchPattern6(any v) returns string {
    match v {
        [var a] => {
            return "[var a]";
        }
        [var a, var b] => {
            return "[var a, var b]";
        }
        [var a, var b, var c] => {
            return "[var a, var b, var c]";
        }
    }
    return "No match";
}

function testListMatchPattern6() {
    assertEquals("[var a]", listMatchPattern6([1]));
    assertEquals("[var a, var b]", listMatchPattern6([1, "str"]));
    assertEquals("[var a, var b, var c]", listMatchPattern6([1, "str", true]));
    assertEquals("No match", listMatchPattern6(1));
}

function listMatchPattern7(any v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "[var a, var b] if a is int";
        }
        [var a, var b] if b is int => {
            return "[var a, var b] if b is int";
        }
    }
    return "No match";
}

function testListMatchPattern7() {
    assertEquals("[var a, var b] if a is int", listMatchPattern7([1, 2]));
    assertEquals("[var a, var b] if b is int", listMatchPattern7(["str", 2]));
    assertEquals("No match", listMatchPattern7([1]));
}

function listMatchPattern8(any v) returns string {
    match v {
        [1] | [1, 2] | [1, 2, 3] => {
            return "match1";
        }
        [1, "s"] | [1, "s", 2] => {
            return "match2";
        }
        [true, "s"] | [true, false] | [true, 1, false] => {
            return "match3";
        }
    }

    return "No match";
}

function testListMatchPattern8() {
    assertEquals("match1", listMatchPattern8([1]));
    assertEquals("match1", listMatchPattern8([1, 2]));
    assertEquals("match1", listMatchPattern8([1, 2, 3]));
    assertEquals("match2", listMatchPattern8([1, "s"]));
    assertEquals("match2", listMatchPattern8([1, "s", 2]));
    assertEquals("match3", listMatchPattern8([true, "s"]));
    assertEquals("match3", listMatchPattern8([true, false]));
    assertEquals("match3", listMatchPattern8([true, 1, false]));
    assertEquals("No match", "No match");
}

function listMatchPattern9(any v) returns string {
    match v {
        [CONST1] | [CONST1, CONST2] => {
            return "match1";
        }
        [[CONST1, CONST2]] | [[CONST1], [CONST2]] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern9() {
    assertEquals("match1", listMatchPattern9([CONST1]));
    assertEquals("match1", listMatchPattern9(["Ballerina"]));
    assertEquals("match1", listMatchPattern9(["Ballerina", 200]));
    assertEquals("match1", listMatchPattern9([CONST1, CONST2]));
    assertEquals("match2", listMatchPattern9([[CONST1, CONST2]]));
    assertEquals("match2", listMatchPattern9([["Ballerina", 200]]));
    assertEquals("match2", listMatchPattern9([[CONST1], [CONST2]]));
    assertEquals("match2", listMatchPattern9([["Ballerina"], [200]]));
    assertEquals("No match", "No match");
}

function listMatchPattern10(any v) returns string {
    match v {
        [var a, 5] | [var a, 7] => {
            return "match1";
        }
        [var a, var b, 5] | [var a, var b, 7] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern10() {
    assertEquals("match1", listMatchPattern10([2, 5]));
    assertEquals("match1", listMatchPattern10([2, 7]));
    assertEquals("match1", listMatchPattern10(["s", 7]));
    assertEquals("match2", listMatchPattern10(["s", true, 5]));
    assertEquals("match2", listMatchPattern10([10, false, 7]));
    assertEquals("No match", listMatchPattern10([1]));
}

function listMatchPattern11([int, int] v) returns string {
    match v {
        [var a, 5] | [var a, 7] => {
            return "match1";
        }
        [var a, 2] | [3, var a] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern11() {
    assertEquals("match1", listMatchPattern11([2, 5]));
    assertEquals("match1", listMatchPattern11([2, 7]));
    assertEquals("match2", listMatchPattern11([2, 2]));
    assertEquals("match2", listMatchPattern11([3, 8]));
    assertEquals("No match", listMatchPattern11([1, 1]));
}

function listMatchPattern12([int, int] v) returns int|string {
    match v {
        [var a, 5] | [var a, 7] => {
            return a;
        }
        [var a, 2] | [3, var a] => {
            return a + 20;
        }
    }

    return "No match";
}

function testListMatchPattern12() {
    assertEquals(2, listMatchPattern12([2, 5]));
    assertEquals(2, listMatchPattern12([2, 7]));
    assertEquals(22, listMatchPattern12([2, 2]));
    assertEquals(28, listMatchPattern12([3, 8]));
    assertEquals("No match", listMatchPattern12([1, 1]));
}

function listMatchPattern13([int, int, int]|[boolean, boolean, boolean] v) returns int|boolean {
    match v {
        [var a, var b, 5] | [var a, var b, false] => {
            return a;
        }
    }

    return false;
}

function testListMatchPattern13() {
    assertEquals(2, listMatchPattern13([2, 2, 5]));
    assertEquals(true, listMatchPattern13([true, true, false]));
    assertEquals(false, listMatchPattern13([1, 1, 1]));
}

function listMatchPattern14() returns string {
    anydata v = [1, 3];
    match v {
        [var a, var b] => {
            return a.toString();
        }
        _ => {
            return "No match";
        }
    }
}

function testListMatchPattern14() {
    assertEquals("1", listMatchPattern14());
}

function listMatchPattern15([string, int] | [float, boolean] | [float, string, boolean] | float a) returns string {
    match a {
        [var s, var i] => {
            return "Matched with two vars : " + s.toString() + ", " + i.toString();
        }
        [var s, var i, var b] => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() + ", " + b.toString();
        }
        var s => {
            return "Matched with single var : " + s.toString();
        }
    }
}

function testListMatchPattern15() {
    [string, int] | [float, boolean] | [float, string, boolean] | float a1 = 66.6;
    [string, int] | [float, boolean] | [float, string, boolean] | float a2 = ["Hello", 12];
    [string, int] | [float, boolean] | [float, string, boolean] | float a3 = [4.5, true];
    [string, int] | [float, boolean] | [float, string, boolean] | float a4 = [6.7, "Test", false];

    assertEquals("Matched with single var : 66.6" ,listMatchPattern15(a1));
    assertEquals("Matched with two vars : Hello, 12" ,listMatchPattern15(a2));
    assertEquals("Matched with two vars : 4.5, true" ,listMatchPattern15(a3));
    assertEquals("Matched with three vars : 6.7, Test, false" ,listMatchPattern15(a4));
}

function listMatchPattern16([string, int] | [float, [string, boolean]] | [float, [string, [boolean, int]]] | float a)
                                                                                                        returns string {
    match a {
        [var f, [var s, [var b, var i]]] => {
            return "Matched with four vars : " + f.toString() + ", " + s.toString() + ", " + i.toString() + ", "
             + b.toString();
        }
        [var s, [var i, var b]] => {
            return "Matched with three vars : " + s.toString() + ", " + i.toString() + ", " + b.toString();
        }
        [var s, var i] => {
            return "Matched with two vars : " + s.toString() + ", " + i.toString();
        }
        var s => {
            return "Matched with single var : " + s.toString();
        }
    }
}

function testListMatchPattern16() {
    float a1 = 66.6;
    [string, int] a2 = ["Hello", 34];
    [float, [string, [boolean, int]]] a3 = [66.6, ["Test", [true, 456]]];
    [float, [string, boolean]] a4 = [5.6, ["Ballerina", false]];

    assertEquals("Matched with single var : 66.6" ,listMatchPattern16(a1));
    assertEquals("Matched with two vars : Hello, 34" ,listMatchPattern16(a2));
    assertEquals("Matched with four vars : 66.6, Test, 456, true" ,listMatchPattern16(a3));
    assertEquals("Matched with three vars : 5.6, Ballerina, false" ,listMatchPattern16(a4));
}

function listMatchPattern17(any x) returns string {
    match x {
        [var s, var i] if s is string => {
            return "Matched with string : " + s + " added text with " + (checkpanic i).toString();
        }
        [var s, var i] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + (checkpanic i).toString();
        }
        [var s, var i] if i is int => {
            return "Matched with int : " + (checkpanic s).toString() + " with " + (i + 3456).toString();
        }
        [var s, var i] if i is boolean => {
            return "Matched with boolean : " + (checkpanic s).toString() + ", " + i.toString();
        }
        var y => {
            return "Matched with default type - float : " + y.toString();
        }
    }
}

function testListMatchPattern17() {
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a1 = ["Hello", 45];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a2 = [4.5, true];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a3 = [false, 4];
    [int, boolean] ib = [455, true];
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | int | float a4 = ib;
    [string, int] | [float, boolean] | [boolean, int] | [int, boolean] | float a5 = 5.6;

    assertEquals("Matched with string : Hello added text with 45", listMatchPattern17(a1));
    assertEquals("Matched with float : 9.0 with true", listMatchPattern17(a2));
    assertEquals("Matched with int : false with 3460", listMatchPattern17(a3));
    assertEquals("Matched with boolean : 455, true", listMatchPattern17(a4));
    assertEquals("Matched with default type - float : 5.6", listMatchPattern17(a5));
}

function listMatchPattern18(any x) returns string {
    match x {
        [var s, var i, var f] if s is string => {
            return "Matched with string : " + s + " added text with " + (checkpanic i).toString();
        }
        [var s, [var i, var f]] if s is float => {
            return "Matched with float : " + (s + 4.5).toString() + " with " + (checkpanic i).toString()
                                                                            + " and " + (checkpanic f).toString();
        }
        [[var s, var i], var f] if i is int => {
            return "Matched with int : " + (checkpanic s).toString() + " with " + (i + 3456).toString()
                                                                            + " and " + (checkpanic f).toString();
        }
        [var s, var i] if i is boolean => {
            return "Matched with boolean : " + (checkpanic s).toString() + ", " + i.toString();
        }
    }
    return "Default";
}

function testListMatchPattern18() {
        [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float
                                                                                        a1 = ["Hello", 45, 5.6];
        [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float
                                                                                        a2 = [5.7, [true, 67]];
        [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float
                                                                                        a3 = [[true, 67], 7.8];
        [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float
                                                                                        a4 = [678, false];
        [string, int, float] | [float, [boolean, int]] | [[boolean, int], float] | [int, boolean] | float a5 = 67.89;

        assertEquals("Matched with string : Hello added text with 45" ,listMatchPattern18(a1));
        assertEquals("Matched with float : 10.2 with true and 67" ,listMatchPattern18(a2));
        assertEquals("Matched with int : true with 3523 and 7.8" ,listMatchPattern18(a3));
        assertEquals("Matched with boolean : 678, false" ,listMatchPattern18(a4));
        assertEquals("Default" ,listMatchPattern18(a5));
}

type FooRecLMP record {
    string s;
    int i;
    float f;
};

type BarRecLMP record {
    byte b;
    FooRecLMP f;
};

function listMatchPattern19(any a) returns string {
    match a {
        [var i, var s] if i is FooRecLMP && s is BarRecLMP => {
            return "Matched with FooRec and BarRec : " + i.toString() + " , " + s.toString();
        }
        [var i, var s] if i is FooRecLMP && s is float => {
            return "Matched with FooRec and float : " + i.toString() + " , " + s.toString();
        }
        [var i, var s] if i is BarRecLMP && s is FooRecLMP => {
            return "Matched with BarRec and FooRec : " + i.toString() + " , " + s.toString();
        }
        [var i, var s] if i is BarRecLMP && s is int => {
            return "Matched with BarRec and int : " + i.toString() + " , " + s.toString();
        }
        [var i, var s] if i is float && s is FooRecLMP => {
            return "Matched with float and FooRec : " + i.toString() + " , " + s.toString();
        }
        [var i, var s] if i is int && s is BarRecLMP => {
            return "Matched with int and BarRec : " + i.toString() + " , " + s.toString();
        }
    }

    return "Default";
}

function testListMatchPattern19() {
    FooRecLMP fooRec1 = {s: "S", i: 23, f: 5.6};
    BarRecLMP barRec1 = {b: 12, f: fooRec1};

    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a1 = [fooRec1, barRec1];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a2 = [fooRec1, 4.5];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a3 = [barRec1, fooRec1];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a4 = [barRec1, 543];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a5 = [5.2, fooRec1];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a6 = [15, barRec1];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a7 = [65, 7.4];
    [int|FooRecLMP, float|BarRecLMP]|[float|BarRecLMP, int|FooRecLMP] a8 = [3.6, 42];

    assertEquals("Matched with FooRec and BarRec : {\"s\":\"S\",\"i\":23,\"f\":5.6} , " +
                                "{\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}", listMatchPattern19(a1));
    assertEquals("Matched with FooRec and float : {\"s\":\"S\",\"i\":23,\"f\":5.6} , 4.5", listMatchPattern19(a2));
    assertEquals("Matched with BarRec and FooRec : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} , " +
                                "{\"s\":\"S\",\"i\":23,\"f\":5.6}", listMatchPattern19(a3));
    assertEquals("Matched with BarRec and int : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} , 543",
            listMatchPattern19(a4));
    assertEquals("Matched with float and FooRec : 5.2 , {\"s\":\"S\",\"i\":23,\"f\":5.6}", listMatchPattern19(a5));
    assertEquals("Matched with int and BarRec : 15 , {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}",
            listMatchPattern19(a6));
    assertEquals("Default", listMatchPattern19(a7));
    assertEquals("Default", listMatchPattern19(a8));
}

function listMatchPattern20() returns string {
    [boolean, string]|[int, string, decimal] v = [1, "A", 1.1d];
    match v {
        [var i, ...var s] => {
            return "i: " + i.toString() + " s: " + s.toString();
        }
    }
}

function testListMatchPattern20() {
    assertEquals("i: 1 s: [\"A\",1.1]", listMatchPattern20());
}

function listMatchPattern21(int[] v) returns int {
    match v {
        [var a, var b, ...var c] => {
            return a + b + c[0];
        }
    }
    return -1;
}

function testListMatchPattern21() {
    assertEquals(6, listMatchPattern21([1, 2, 3, 4, 5]));
}

function listMatchPattern22(int[5] v) returns int {
    match v {
        [var a, var b, ...var c] => {
            return a + b + c[1];
        }
    }
}

function testListMatchPattern22() {
    assertEquals(7, listMatchPattern22([1, 2, 3, 4, 5]));
}

function listMatchPattern23(any[] v) returns string {
    match v {
        [var a, var b, ...var c] => {
            return a.toString();
        }
    }
    return "No Match";
}

function testListMatchPattern23() {
    assertEquals("1", listMatchPattern23([1, 2, 3, 4, 5]));
}

function listMatchPattern24(any[] a) returns string {
    match a {
        [var x, var y, var z] => {
            return x.toString() + z.toString();
        }
        _ => {
            return "No match";
        }
    }
}

function testListMatchPattern24() {
    assertEquals("No match", listMatchPattern24([1, 2, 3, 4, 5]));
    assertEquals("13", listMatchPattern24([1, 2, 3]));
}

function listMatchPattern25(any[] a) returns string {
    match a {
        [var x, _, var z] => {
            return x.toString() + z.toString();
        }
        _ => {
            return "No match";
        }
    }
}

function testListMatchPattern25() {
    assertEquals("No match", listMatchPattern25([1, 2, 3, 4, 5]));
    assertEquals("13", listMatchPattern25([1, 2, 3]));
}

function listMatchPattern26(int[3] a) returns int {
    match a {
        [var x, _, var z] => {
            return x + z;
        }
    }
}

function testListMatchPattern26() {
    assertEquals(4, listMatchPattern26([1, 2, 3]));
}

function listMatchPattern27(any a) returns string {
    match a {
        [12, _, "A"] => {
            return "Matched";
        }
        _ => {
            return "Match Default";
        }
    }
}

function testListMatchPattern27() {
    assertEquals("Matched", listMatchPattern27([12, 2, "A"]));
    assertEquals("Matched", listMatchPattern27([12, [12, 3], "A"]));
    assertEquals("Match Default", listMatchPattern27([13, 2, "A"]));
}

function listMatchPattern28(anydata val) returns anydata {
    match val {
        [var m] if m is any[] => {
            anydata[] o = m;
            return o;
        }
        _ => {
            return "other";
        }
    }
}

function testListMatchPatternWithWildCard() {
    [int, string, CONST1]|error v1 = [1, "str", CONST1];
    string result = "";
    match v1 {
        [1, "str", "Ballerina1"] => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Default", result);

    [int, string, CONST1]|error v2 = error("SampleError");
    result = "Not Matched";
    match v2 {
        [1, "str", "Ballerina"] => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Not Matched", result);
}

function testListMatchPatternWithArrayAndAnydataIntersection() {
    int[] x = [1, 2, 3];
    assertEquals(x, listMatchPattern28(<int[][]>[x]));
    anydata[] y = [["hello", "world"]];
    assertEquals(["hello", "world"], listMatchPattern28(y));
    assertEquals("other", listMatchPattern28(<anydata[]>[["hello", "world"], 1, 2]));
    assertEquals("other", listMatchPattern28("hello"));
}

function listMatchPattern29(json j) returns json {
    match j {
        [var x] => {
            return x;
        }
    }
    return ();
}

function testListMatchPattern29() {
    assertEquals("hello", listMatchPattern29(["hello"]));
    assertEquals(1, listMatchPattern29([1]));
    assertEquals((), listMatchPattern29([()]));
    assertEquals((), listMatchPattern29({a: "hello world", x1: 1}));
    assertEquals((), listMatchPattern29([]));
    assertEquals((), listMatchPattern29([1, 2, 3]));
    assertEquals((), listMatchPattern29(1));
}

type RecLMP record {|
    int|float a;
|};

function testListMatchPattern30() {
    [int, RecLMP|string] a1 = [12, {a: 1}];
    string result = "";

    match a1 {
        [12, "B"] => {
            result = "Pattern1";
        }
        [12, {a: 2}] => {
            result = "Pattern2";
        }
        [12, {a: 1}] => {
            result = "Pattern3";
        }
    }
    assertEquals("Pattern3", result);

    result = "";

    match a1 {
        [12, "B"] => {
            result = "Pattern1";
        }
        [12, {a: 2}] => {
            result = "Pattern2";
        }
        [12, ...var y] => {
            result = "Pattern3";
        }
    }
    assertEquals("Pattern3", result);

    [int, RecLMP|string...] a2 = [12, {a: 1}];
    result = "";

    match a2 {
        [12, "B"] => {
            result = "Pattern1";
        }
        [12, {a: 2}] => {
            result = "Pattern2";
        }
        [12, {a: 1}] => {
            result = "Pattern3";
        }
    }
    assertEquals("Pattern3", result);

    [int, string, RecLMP|string...] a3 = [12, "C", {a: 1.5}];
    result = "";

    match a3 {
        [12, "A", "B", "C"] => {
            result = "Pattern1";
        }
        [12, "A", {a: 1}, "B"]|[12, "C", {a: 1.5}] => {
            result = "Pattern2";
        }
        [12, "A"] => {
            result = "Pattern3";
        }
    }
    assertEquals("Pattern2", result);

    [RecLMP|string...] a4 = [{a: 1}, {a: 2}, {a: 3}];
    result = "";

    match a4 {
        ["A", "B", "C"] => {
            result = "Pattern1";
        }
        [...var a] => {
            result = "Pattern2";
        }
    }
    assertEquals("Pattern2", result);

    result = "";

    match a4 {
        [{a: 1}] => {
            result = "Pattern1";
        }
        [{a: 1}, ...var a] => {
            result = "Pattern2";
        }
    }
    assertEquals("Pattern2", result);

    error err1 = error("Error One", data = [{b: 5}, 12]);
    [error, RecLMP|string...] a5 = [err1, {a: 2}, {a: 3}];
    result = "";

    match a5 {
        [error(_), {a: 1}] => {
            result = "Pattern1";
        }
        [error(var a, var b), {a: 2}, {a: 3}] => {
            result = "Pattern2";
        }
    }
    assertEquals("Pattern2", result);
}

type TLMP readonly & SLMP;

type SLMP [INT, int]|[STRING, string];

const INT = 1;
const STRING = 2;

function testListMatchPattern31() {
    TLMP t1 = [STRING, "hello"];
    TLMP t2 = [INT, 1234];

    assertEquals(["hello", ()], listMatchPattern31(t1));
    assertEquals([(), 1234], listMatchPattern31(t2));
}

function listMatchPattern31(TLMP t) returns [string?, int?] {
    string? s = ();
    int? i = ();

    match t {
        [STRING, var val] => {
            s = val;
        }
        [INT, var val] => {
            i = val;
        }
    }

    return [s, i];
}

function testListMatchPattern32() {
    TLMP t1 = [STRING, "hello"];
    TLMP t2 = [INT, 1234];

    assertEquals("hello", listMatchPattern32(t1));
    assertEquals(1234, listMatchPattern32(t2));
}

function listMatchPattern32(TLMP t) returns string|int {
    string|int s;

    match t {
        [_, var val] => {
            s = val;
        }
    }

    return s;
}

type T2LMP readonly & ([1, string]|[2, string]|[3, string]);

function testListMatchPattern33() {
    T2LMP t1 = [1, "hello"];
    T2LMP t2 = [2, "1234"];
    T2LMP t3 = [3, "abcd"];

    assertEquals("hello", listMatchPattern33(t1));
    assertEquals("1234", listMatchPattern33(t2));
    assertEquals("abcd", listMatchPattern33(t3));
}

function listMatchPattern33(T2LMP t) returns string {
    string s;

    match t {
        [_, var val] => {
            s = val;
        }
    }

    return s;
}

type T3LMP readonly & S3LMP;

type S3LMP string[2]|int[2];

function testListMatchPattern34() {
    T3LMP t1 = ["1", "hello"];
    T3LMP t2 = [2, 1234];

    assertEquals("hello", listMatchPattern34(t1));
    assertEquals(1234, listMatchPattern34(t2));
}

function listMatchPattern34(T3LMP t) returns string|int {
    string|int s = 10;

    match t {
        [_, var val] => {
            s = val; // error: incompatible types: expected '(string|int)', found '(any|error)'
        }
    }

    return s;
}

public type T4LMP ["list", T4LMP[]]|"int";

function testListMatchPattern35() {
    T4LMP[] t1 = ["int"];
    T4LMP[] t2 = ["int", "int", "int"];

    T4LMP x1 = ["list", t1];
    T4LMP x2 = ["list", ["int", "int"]];
    T4LMP x3 = ["list", t2];
    assertEquals(listMatchPattern35(x1, t1), "match 4");
    assertEquals(listMatchPattern35("int", ()), "match 1");
    assertEquals(listMatchPattern35(x2, ()), "match 2");
    assertEquals(listMatchPattern35(x3, t2), "match 4");
}

function listMatchPattern35(T4LMP x, T4LMP[]? t) returns string? {
    match x {
        "int" => {
            return "match 1";
        }
        ["list", ["int", "int"]] => {
            return "match 2";
        }
        ["list1", var y] => {
            return "match 3";
        }
        ["list", var y] => {
            assertEquals(y, t);
            return "match 4";
        }
        [...var y] => {
            return "no match";
        }
    }
}

function testListMatchPattern36() {
    T4LMP[] t1 = ["int"];
    T4LMP[] t2 = ["int", "int", "int"];

    T4LMP x1 = ["list", t1];
    T4LMP x2 = ["list", ["int", "int"]];
    T4LMP x3 = ["list", t2];
    assertEquals(listMatchPattern36(x1, t1), "match 4");
    assertEquals(listMatchPattern36("int", ()), "match 1");
    assertEquals(listMatchPattern36(x2, ()), "match 2");
    assertEquals(listMatchPattern36(x3, t2), "match 4");
}

function listMatchPattern36((T4LMP|anydata)? x, T4LMP[]? t) returns string? {
    string? a = ();
    match x {
        "int" => {
            return "match 1";
        }
        ["list", ["int", "int"]] => {
            return "match 2";
        }
        ["list1", var y] => {
            return "match 3";
        }
        ["list", var y] => {
            assertEquals(y, t);
            return "match 4";
        }
    }
}

public type T5LMP ["array", T6LMP]|["cell", T6LMP, string];

public type T6LMP ["|", T6LMP]|"int";

function testListMatchPattern37() {
    T6LMP t1 = ["|", ["|", "int"]];
    T6LMP t2 = "int";
    T5LMP x1 = ["cell", t1, "inf"];
    T5LMP x2 = ["array", t1];
    T5LMP x3 = ["cell", t2, "inf1"];
    T5LMP x4 = ["array", t2];

    assertEquals(listMatchPattern37(x1, t1, "inf"), "match 2");
    assertEquals(listMatchPattern37(x2, t1, ()), "match 4");
    assertEquals(listMatchPattern37(x3, (), ()), "match 1");
    assertEquals(listMatchPattern37(x4, t2, ()), "match 4");
}

function listMatchPattern37(T5LMP x, T6LMP? t, string? s) returns string {
    match x {
        ["cell", "int", "inf1"] => {
            return "match 1";
        }
        ["cell", var y, var z] => {
            assertEquals(y, t);
            assertEquals(z, s);
            return "match 2";
        }
        ["array1", var y] => {
            return "match 3";
        }
        ["array", var y] => {
            assertEquals(y, t);
            return "match 4";
        }
        [var y, ...var z] => {
            return "no match";
        }
    }
}

function testListMatchPattern38() {
    T6LMP t1 = ["|", ["|", "int"]];
    T6LMP t2 = "int";
    T5LMP x1 = ["cell", t1, "inf"];
    T5LMP x2 = ["array", t1];
    T5LMP x3 = ["cell", t2, "inf1"];
    T5LMP x4 = ["array", t2];

    assertEquals(listMatchPattern38(x1, t1, "inf"), "match 2");
    assertEquals(listMatchPattern38(x2, t1, ()), "match 4");
    assertEquals(listMatchPattern38(x3, (), ()), "match 1");
    assertEquals(listMatchPattern38(x4, t2, ()), "match 4");
}

function listMatchPattern38((anydata|T5LMP)? x, T6LMP? t, string? s) returns string? {
    match x {
        ["cell", "int", "inf1"] => {
            return "match 1";
        }
        ["cell", var y, var z] => {
            assertEquals(y, t);
            assertEquals(z, s);
            return "match 2";
        }
        ["array1", var y] => {
            return "match 3";
        }
        ["array", var y] => {
            assertEquals(y, t);
            return "match 4";
        }
    }
}

public type T7LMP ["array", T6LMP]|["cell", T6LMP];

function testListMatchPattern39() {
    T6LMP y1 = "int";
    T6LMP y2 = ["|", "int"];

    T7LMP x1 = ["cell", y1];
    T7LMP x2 = ["array", y1];
    T7LMP x3 = ["cell", y2];

    assertEquals(listMatchPattern39(x1, y1), "match 3");
    assertEquals(listMatchPattern39(x2, y1), "match 2");
    assertEquals(listMatchPattern39(x3, y2), "match 3");
}

function listMatchPattern39(T7LMP x, T6LMP y) returns string {
    match x {
        ["list", var _] => {
            T6LMP _ = x[1];
            T6LMP a = x[1];
            assertEquals(a, y);
            assertEquals(x[0], "list");
            return "match 1";
        }
        ["array", var _] => {
            T6LMP _ = x[1];
            T6LMP a = x[1];
            assertEquals(a, y);
            assertEquals(x[0], "array");
            return "match 2";
        }
        ["cell", var _] => {
            T6LMP _ = x[1];
            T6LMP a = x[1];
            assertEquals(a, y);
            assertEquals(x[0], "cell");
            return "match 3";
        }
        [_, _] => {
            return "no match";
        }
    }
}

public type T8LMP ["list", T8LMP, T8LMP[]]|["list", T8LMP[]]|"int";

function testListMatchPattern40() {
    T8LMP t1 = "int";
    T8LMP[] t2 = ["int", "int", "int"];
    T8LMP[] t3 = [t1];
    T8LMP t4 = ["list", ["int", "int", "int"]];

    T8LMP x1 = ["list", t3];
    T8LMP x2 = ["list", ["int", "int"]];
    T8LMP x3 = ["list", t2];
    T8LMP x4 = ["list", "int", t2];
    T8LMP x5 = ["list", t4, t3];

    assertEquals(listMatchPattern40(x1, (), t3, ()), "match 4");
    assertEquals(listMatchPattern40("int", (), (), ()), "match 1");
    assertEquals(listMatchPattern40(x2, (), (), ()), "match 2");
    assertEquals(listMatchPattern40(x3, (), t2, ()), "match 4");
    assertEquals(listMatchPattern40(x4, (), (), ()), "match 5");
    assertEquals(listMatchPattern40(x5, t4, t3, ()), "match 6");
}

function listMatchPattern40(T8LMP x, T8LMP? t1, T8LMP[]? t2, T8LMP? t3) returns string? {
    match x {
        "int" => {
            return "match 1";
        }
        ["list", ["int", "int"]] => {
            return "match 2";
        }
        ["list1", var y] => {
            return "match 3";
        }
        ["list", var y] => {
            assertEquals(y, t2);
            return "match 4";
        }
        ["list", "int", ["int", "int", "int"]] => {
            return "match 5";
        }
        ["list", var y, var z] => {
            T8LMP _ = z[0];
            assertEquals(y, t1);
            assertEquals(z, t2);
            return "match 6";
        }
        [...var y] => {
            return "no match";
        }
    }
}

public type T9LMP ["array", T9LMP]|["cell", T6LMP]|["array", T6LMP]|[string, int];

function testListMatchPattern41() {
    T9LMP x1 = ["cell", "int"];
    T9LMP x2 = ["array", ["|", "int"]];
    T9LMP x3 = ["cell", ["|", "int"]];
    T9LMP x4 = ["string 1", 1];
    T9LMP x5 = ["array", x4];
    T9LMP x6 = ["string 2", 1];

    assertEquals(listMatchPattern41(x1), "match 1");
    assertEquals(listMatchPattern41(x2), "match 4");
    assertEquals(listMatchPattern41(x3), "match 6");
    assertEquals(listMatchPattern41(x4), "match 5");
    assertEquals(listMatchPattern41(x5), "match 4");
    assertEquals(listMatchPattern41(x6), "match 6");
}

function listMatchPattern41(T9LMP x) returns string {
    match x {
        ["cell", "int"] => {
            return "match 1";
        }
        ["cell", var y, var z] => {
            return "match 2";
        }
        ["array1", var y] => {
            return "match 3";
        }
        ["array", var y] => {
            return "match 4";
        }
        ["string 1", 1] => {
            return "match 5";
        }
        [var y, var z] => {
            return "match 6";
        }
    }
}

public type T10LMP [string, decimal, string]|[string, boolean...]|[int...]|[boolean];

function testListMatchPattern42() {
    T10LMP x1 = ["string", 1d, "string"];
    T10LMP x2 = ["string", true, true, true, true, true, true];
    T10LMP x3 = [1, 1, 1, 1];
    T10LMP x4 = [true];
    T10LMP x5 = ["string", true];

    assertEquals(listMatchPattern42(x1, ["string", 1d, "string"]), "match 1");
    assertEquals(listMatchPattern42(x2, ["string", true, true, true, true, [true, true]]), "match 3");
    assertEquals(listMatchPattern42(x3, [1, 1, 1, [1]]), "match 4");
    assertEquals(listMatchPattern42(x4, [true]), "match 2");
    assertEquals(listMatchPattern42(x5, ["string", true]), "match 5");
}

function listMatchPattern42(T10LMP t, anydata a) returns string {
    match t {
        [var x, var y, var z] => {
            assertEquals([x, y, z], a);
            return "match 1";
        }
        [var x] => {
            assertEquals([x], a);
            return "match 2";
        }
        [var p, var q, var r, var s, var y, ...var z] => {
            assertEquals([p, q, r, s, y, z], a);
            return "match 3";
        }
        [var p, var q, var r, ...var z] => {
            assertEquals([p, q, r, z], a);
            return "match 4";
        }
        [...var x] => {
            assertEquals(x, a);
            return "match 5";
        }
    }
}

public type T11LMP [int, T11LMP, T11LMP...]|[T11LMP...]|["int"];

public function testListMatchPattern43() {
    T11LMP[] t1 = [["int"], ["int"], ["int"]];
    T11LMP x1 = [1, ["int"], ["int"]];
    T11LMP x2 = [1, ["int"], ["int"], ["int"], ["int"], ["int"], ["int"], ["int"]];
    T11LMP x3 = [["int"], ["int"], ["int"], ["int"]];
    T11LMP x4 = [["int"]];
    T11LMP x5 = [t1, ["int"]];

    assertEquals(listMatchPattern43(x1, [1, ["int"], ["int"]]), "match 1");
    assertEquals(listMatchPattern43(x2,
                    [1, ["int"], ["int"], ["int"], ["int"], [["int"], ["int"], ["int"]]]), "match 3");
    assertEquals(listMatchPattern43(x3, [["int"], ["int"], ["int"], [["int"]]]), "match 4");
    assertEquals(listMatchPattern43(x4, [["int"]]), "match 2");
    assertEquals(listMatchPattern43(x5, [t1, ["int"]]), "match 5");
}

function listMatchPattern43(T11LMP t, anydata a) returns string {
    match t {
        [var x, var y, var z] => {
            assertEquals([x, y, z], a);
            return "match 1";
        }
        [var x] => {
            assertEquals([x], a);
            return "match 2";
        }
        [var p, var q, var r, var s, var y, ...var z] => {
            assertEquals([p, q, r, s, y, z], a);
            return "match 3";
        }
        [var p, var q, var r, ...var z] => {
            assertEquals([p, q, r, z], a);
            return "match 4";
        }
        [...var x] => {
            assertEquals(x, a);
            return "match 5";
        }
    }
}

public type T12LMP [int, T12LMP[], T12LMP...]|[T12LMP[]...]|"int";

public type T13LMP [int, T13LMP, T13LMP, T13LMP[]...]|[T13LMP...]|"int";

public function testListMatchPattern44() {
    T12LMP[] t1 = ["int", "int", "int"];
    T12LMP x1 = [1, t1, "int", "int"];
    T12LMP x2 = [1, t1, "int", "int", "int", "int", "int", "int", "int"];
    T12LMP x3 = [t1, t1, t1, t1, t1];
    T12LMP x4 = [t1];
    T12LMP x5 = [t1, t1];

    T13LMP[] t2 = ["int", "int", "int"];
    T13LMP y1 = [1, "int", "int", t2, t2];
    T13LMP y2 = [1, "int", "int", t2, t2, t2, t2, t2, t2, t2];
    T13LMP y3 = [t2, t2, t2, t2, t2, t2];
    T13LMP y4 = [t2];
    T13LMP y5 = [t2, t2];

    assertEquals(listMatchPattern44(x1, [1, t1, "int", "int"]), "match 1");
    assertEquals(listMatchPattern44(x2, [1, t1, "int", "int", "int", "int", ["int", "int", "int"]]), "match 3");
    assertEquals(listMatchPattern44(x3, [t1, t1, t1, t1, [t1]]), "match 4");
    assertEquals(listMatchPattern44(x4, [t1]), "match 2");
    assertEquals(listMatchPattern44(x5, [t1, t1]), "match 5");

    assertEquals(listMatchPattern44(y1, [1, "int", "int", t2, t2]), "match 1");
    assertEquals(listMatchPattern44(y2, [1, "int", "int", t1, t2, t2, t2, [t2, t2, t2]]), "match 3");
    assertEquals(listMatchPattern44(y3, [t2, t2, t2, t2, t2, [t2]]), "match 4");
    assertEquals(listMatchPattern44(y4, [t2]), "match 2");
    assertEquals(listMatchPattern44(y5, [t2, t2]), "match 5");
}

function listMatchPattern44(T12LMP|T13LMP t, anydata a) returns string? {
    if t is T12LMP {
        match t {
            [var p, var x, var y, var z] => {
                assertEquals([p, x, y, z], a);
                return "match 1";
            }
            [var x] => {
                assertEquals([x], a);
                return "match 2";
            }
            [var p, var q, var r, var s, var u, var y, ...var z] => {
                assertEquals([p, q, r, s, u, y, z], a);
                return "match 3";
            }
            [var m, var p, var q, var r, ...var z] => {
                assertEquals([m, p, q, r, z], a);
                return "match 4";
            }
            [...var x] => {
                assertEquals(x, a);
                return "match 5";
            }
        }
    } else {
        match t {
            [var p, var q, var x, var y, var z] => {
                assertEquals([p, q, x, y, z], a);
                return "match 1";
            }
            [var x] => {
                assertEquals([x], a);
                return "match 2";
            }
            [var p, var q, var r, var s, var u, var v, var y, ...var z] => {
                assertEquals([p, q, r, s, u, v, y, z], a);
                return "match 3";
            }
            [var m, var n, var p, var q, var r, ...var z] => {
                assertEquals([m, n, p, q, r, z], a);
                return "match 4";
            }
            [...var x] => {
                assertEquals(x, a);
                return "match 5";
            }
        }
    }
}

public type T14LMP [string]|[int, string]|[int, int, string];

public type T15LMP [int]|[T15LMP, T15LMP]|[T15LMP[], T15LMP[], T15LMP[]];

public function testListMatchPattern45() {
    T14LMP x1 = ["string"];
    T14LMP x2 = [1, "string"];
    T14LMP x3 = [1, 1, "string"];

    T15LMP y1 = [1];
    T15LMP[] t2 = [y1, y1];
    T15LMP y2 = [y1, y1];
    T15LMP y3 = [t2, t2, t2];

    assertEquals(listMatchPattern45(x1, x1), "match 3");
    assertEquals(listMatchPattern45(x2, x2), "match 2");
    assertEquals(listMatchPattern45(x3, x3), "match 1");

    assertEquals(listMatchPattern45(y1, y1), "match 3");
    assertEquals(listMatchPattern45(y2, y2), "match 2");
    assertEquals(listMatchPattern45(y3, y3), "match 1");
}

function listMatchPattern45(T14LMP|T15LMP t, anydata a) returns string? {
    match t {
        [var p, var x, var y, ...var z] => {
            assertEquals([p, x, y], a);
            assertEquals(z == [], true);
            return "match 1";
        }
        [var p, var x, ...var z] => {
            assertEquals([p, x], a);
            assertEquals(<anydata>z == [], true);
            return "match 2";
        }
        [var p, ...var z] => {
            assertEquals([p], a);
            assertEquals(<anydata>z == [], true);
            return "match 3";
        }
    }
}

public type T16LMP [string, int];

public function testListMatchPattern46() {
    assertEquals(listMatchPattern46(), "string");
}

public function listMatchPattern46() returns string {
    T16LMP a = ["string", 1];
    string b;
    match a {
        [_, var x] => {
            b = "string";
        }
    }
    return b;
}

type DataLMP string|DataLMP[];

type Data2LMP ["call", string, DataLMP...];

type Data3LMP ["branch", string];

type Data4LMP Data2LMP|Data3LMP;

public function testListMatchPattern47() {
    assertEquals(listMatchPattern47(["branch", "b.target"]), "match 2");
    assertEquals(listMatchPattern47(["call", "add", "1", "2"]), "match 1");
}

function listMatchPattern47(Data4LMP d) returns string {
    match d {
        ["call", "add", ...var operands] => {
            return "match 1";
        }
        _ => {
            return "match 2";
        }
    }
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
