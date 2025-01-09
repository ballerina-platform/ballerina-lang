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
        [1, ...var a] => {
            return "match";
        }
    }
    return "No match";
}

function testListMatchPatternWithRest1() {
    assertEquals("match", listMatchPattern1([1]));
    assertEquals("match", listMatchPattern1([1, 2]));
    assertEquals("match", listMatchPattern1([1, 2, 3]));
}

function listMatchPattern2([int, int] v) returns int {
    match v {
        [1, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest2() {
    assertEquals(2, listMatchPattern2([1, 2]));
}

function listMatchPattern3([int, int, int] v) returns int {
    match v {
        [1, 2, ...var a] | [1, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest3() {
    assertEquals(3, listMatchPattern3([1, 2, 3]));
    assertEquals(4, listMatchPattern3([1, 4, 3]));
}

function listMatchPattern4([int, int, string] v) returns int|string {
    match v {
        [1, 2, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest4() {
    assertEquals("str", listMatchPattern4([1, 2, "str"]));
}

function listMatchPattern5(any v) returns int|string {
    match v {
        [1, "str", ...var a] => {
            if (a[0] is string) {
                return <string> checkpanic a[0];
            } else if (a[0] is int) {
                return <int> checkpanic a[0];
            }
        }
        [1, ...var a] => {
            if (a[0] is string) {
                return <string> checkpanic a[0];
            } else if (a[0] is int) {
                return <int> checkpanic a[0];
            }
        }
        _ => {
            return "No match";
        }
    }
    return "No match";
}

function testListMatchPatternWithRest5() {
    assertEquals(2, listMatchPattern5([1, "str", 2, 3]));
    assertEquals("s", listMatchPattern5([1, "str", "s", 3]));
    assertEquals(2, listMatchPattern5([1, 2, "s", 3]));
    assertEquals("No match", listMatchPattern5([1, true, "s", 3]));
}

function listMatchPattern6(anydata a) returns anydata {
    match a {
        [var p, ...var oth] if p is any[] => {
            anydata[] m = p;
            return m;
        }
        _ => {
            return "other";
        }
    }
}

function listMatchPattern7((int|error)[][] a) returns [int[], (int|error)[][]]|string {
    match a {
        [var p, ...var oth] if p is anydata => {
            int[] m = p;
            (int|error)[][] n = oth;
            return [m, n];
        }
        _ => {
            return "other";
        }
    }
}

function testListMatchPatternWithRestPatternWithArrayAndAnydataIntersection() {
    int[] m1 = [1, 2];
    anydata[] m2 = [m1, "hello"];
    assertEquals(m1, listMatchPattern6(m2));
    anydata[] m3 = [1, "foo"];
    assertEquals("other", listMatchPattern6(m3));
    assertEquals("other", listMatchPattern6("foo"));

    (int|error)[] m4 = [1, error("error!")];
    [int[], (int|error)[][]] res = <[int[], (int|error)[][]]> listMatchPattern7([m1, m4]);
    assertEquals(m1, res[0]);
    assertEquals(true, m4 === res[1][0]);
    assertEquals(1, res[1].length());
    assertEquals("other", <string> listMatchPattern7([[error("error!")]]));
    assertEquals("other", <string> listMatchPattern7([]));
}

function listMatchPattern8(int[3] val) returns int {
    match val {
        [var a, var b, ...var c] => {
            return a + b + c[0];
        }
    }
}

function testListMatchPatternWithClosedArray() {
    assertEquals(6, listMatchPattern8([1, 2, 3]));
}

function listMatchPatternWithRestPattern11(json j) returns json[] {
    match j {
        [var x, ...var y] => {
            y.push(x);
            return y;
        }
    }
    return [];
}

function testListMatchPatternWithRestPattern11() {
    assertEquals(["hello"], listMatchPatternWithRestPattern11(["hello"]));
    assertEquals([1, "hello world"], listMatchPatternWithRestPattern11(["hello world", 1]));
    assertEquals(["world", (), "hello"], listMatchPatternWithRestPattern11(["hello", "world", ()]));
    assertEquals([], listMatchPatternWithRestPattern11([]));
    assertEquals([], listMatchPatternWithRestPattern11({}));
    assertEquals([], listMatchPatternWithRestPattern11(1));
}

type Bar record {
    int id;
    boolean flag;
};

function testListMatchPatternWithRestPattern12() {
    [[string, Bar, boolean...], string, boolean...] t1 = [["Ballerina", {id: 34, flag: true}, false, true, false],
                                                          "A", true, false];
    string a1;
    [Bar, boolean...] a2;
    [string, boolean...] a3;
    Bar a4;
    boolean[] a5;
    string matched = "Not Matched";
    match t1 {
        var [[f1, ...f2], ...f3] => {
            matched = "Matched1";
            a1 = f1;
            a2 = f2;
            a3 = f3;
            match f2 {
                [var f4, ...var f5] => {
                    matched += " Matched2";
                    a4 = f4;
                    a5 = f5;
                }
            }
        }
    }
    assertEquals("Matched1 Matched2", matched);
    assertEquals("Ballerina", a1);
    assertEquals(4, a2.length());
    assertEquals(34, a2[0].id);
    assertEquals(true, a2[0].flag);
    assertEquals(false, a2[1]);
    assertEquals(true, a2[2]);
    assertEquals(false, a2[3]);
    assertEquals(3, a3.length());
    assertEquals("A", a3[0]);
    assertEquals(true, a3[1]);
    assertEquals(false, a3[2]);
    assertEquals(34, a4.id);
    assertEquals(true, a4.flag);
    assertEquals(3, a5.length());
    assertEquals(false, a5[0]);
    assertEquals(true, a5[1]);
    assertEquals(false, a5[2]);
}

class FooObj {
    public string s;
    public float f;
    public byte b;
    function init(string s, float f, byte b) {
        self.s = s;
        self.f = f;
        self.b = b;
    }
}

class BarObj {
    public boolean b;
    public int i;
    function init(boolean b, int i) {
        self.b = b;
        self.i = i;
    }
}

function testListMatchPatternWithRestPattern13() {
    FooObj fooObj1 = new ("Fooo", 3.7, 23);
    BarObj barObj1 = new (true, 56);
    FooObj fooObj2 = new ("Foo2", 10.2, 30);
    BarObj barObj2 = new (false, 56);
    BarObj barObj3 = new (true, 58);
    string matched = "Not Matched";

    [[string, [error, map<string>, int, (FooObj|BarObj)...], Bar, (byte|float)...], string, boolean...] t2 =
                [["Ballerina", [error("Error", detail1= 12, detail2= true),
                {firstName: "John", lastName: "Damon"}, 12, fooObj1, barObj1], {id: 34, flag: true}, 10.5, 20],
                "A", true, false];

    string a1;
    error a2;
    [map<string>, int, (FooObj|BarObj)...] a3;
    [Bar, (byte|float)...] a4;
    [string, boolean...] a5;
    map<string> a6;
    [int, (FooObj|BarObj)...] a7;

    string b1;
    error b2;
    [map<string>, int, (FooObj|BarObj)...] b3;
    [Bar, (byte|float)...] b4;
    [string, boolean...] b5;
    map<string> b6;
    [int, (FooObj|BarObj)...] b7;

    match t2 {
        [[var g1, [var g2, ... var g3], ...var g4], ...var g5] => {
            matched = "Matched1";
            a1 = g1;
            a2 = g2;
            a3 = g3;
            a4 = g4;
            a5 = g5;
            match a3 {
                var [g6, ...g7] => {
                    matched += " Matched2";
                    a6 = g6;
                    a7 = g7;
                }
            }

            [[g1, g2, ...g3], [...g5], ...g4] = [["Hello", error("Transaction Error"), [{primary: "Blue",
                   secondary: "Green"}, 1000, barObj2, fooObj2, barObj3]], [["World", true, false, true, false]],
                   [{id: 40, flag: true}, 0x5, 0x7, 20.25, 0x8]];
            b1 = g1;
            b2 = g2;
            b3 = g3;
            b5 = g5;
            b4 = g4;
        }
    }
    assertEquals("Matched1 Matched2", matched);
    assertEquals("Ballerina", a1);
    assertEquals(true, a2.message() === "Error");
    assertEquals(true, a2.detail()["detail1"] === 12);
    assertEquals(true, a2.detail()["detail2"] === true);
    assertEquals(4, a3.length());
    assertEquals("John", a3[0]["firstName"]);
    assertEquals("Damon", a3[0]["lastName"]);
    assertEquals(12, a3[1]);
    assertEquals(true, a3[2] === fooObj1);
    assertEquals(true, a3[3] === barObj1);
    assertEquals(3, a4.length());
    assertEquals(34, a4[0].id);
    assertEquals(true, a4[0].flag);
    assertEquals(10.5, a4[1]);
    assertEquals(20, a4[2]);
    assertEquals(3, a5.length());
    assertEquals("A", a5[0]);
    assertEquals(true, a5[1]);
    assertEquals(false, a5[2]);

    assertEquals("Hello", b1);
    assertEquals(true, b2.message() === "Transaction Error");
    assertEquals(5, b3.length());
    assertEquals("Blue", b3[0]["primary"]);
    assertEquals("Green", b3[0]["secondary"]);
    assertEquals(1000, b3[1]);
    assertEquals(true, b3[2] is BarObj);
    assertEquals(false, (<BarObj>b3[2]).b);
    assertEquals(56, (<BarObj>b3[2]).i);
    assertEquals(true, b3[3] is FooObj);
    assertEquals("Foo2", (<FooObj>b3[3]).s);
    assertEquals(10.2, (<FooObj>b3[3]).f);
    assertEquals(30, (<FooObj>b3[3]).b);
    assertEquals(true, b3[4] is BarObj);
    assertEquals(true, (<BarObj>b3[4]).b);
    assertEquals(58, (<BarObj>b3[4]).i);
    assertEquals(5, b5.length());
    assertEquals("World", b5[0]);
    assertEquals(true, b5[1]);
    assertEquals(false, b5[2]);
    assertEquals(true, b5[3]);
    assertEquals(false, b5[4]);
    assertEquals(5, b4.length());
    assertEquals(40, b4[0].id);
    assertEquals(true, b4[0].flag);
    assertEquals(5, b4[1]);
    assertEquals(7, b4[2]);
    assertEquals(20.25, b4[3]);
    assertEquals(8, b4[4]);
}

function testListMatchPatternWithRestPattern14() {
    int[5] t3 = [1, 2, 3, 4, 5];
    int a1;
    [int, int, int, int] a2;
    int a3;
    [int, int, int] a4;
    match t3 {
        [var a, ...var b] => {
            a1 = a;
            a2 = b;
            match b {
                var [c, ...d] => {
                    a3 = c;
                    a4 = d;
                }
            }
        }
    }
    assertEquals(1, a1);
    assertEquals(2, a2[0]);
    assertEquals(3, a2[1]);
    assertEquals(4, a2[2]);
    assertEquals(5, a2[3]);
    assertEquals(2, a3);
    assertEquals(3, a4[0]);
    assertEquals(4, a4[1]);
    assertEquals(5, a4[2]);
}

function testListMatchPatternWithRestPattern15() {
    int[] t4 = [1, 2, 3, 4, 5];
    int a1 = 10;
    [int...] a2 = [];
    [int...] a3 = [];
    match t4 {
        [var a, ...var b] => {
            a1 += a;
            a2 = b;
            match b {
                var [c, ...d] => {
                    a1 += c;
                    a3 = d;
                }
            }
        }
    }
    assertEquals(13, a1);
    assertEquals(4, a2.length());
    assertEquals(2, a2[0]);
    assertEquals(3, a2[1]);
    assertEquals(4, a2[2]);
    assertEquals(5, a2[3]);
    assertEquals(3, a3.length());
    assertEquals(3, a3[0]);
    assertEquals(4, a3[1]);
    assertEquals(5, a3[2]);
}

function listMatchPatternWithRestPattern16(any a) returns string {
    match a {
        [] => {
            return "match1";
        }
        ["add", [...var operands]] => {
            return "match2";
        }
        [...var operands] => {
            return "match3";
        }
        _ => {
            return "No match";
        }
    }
}

public function testListMatchPatternWithRestPattern16() {
    assertEquals("match1", listMatchPatternWithRestPattern16([]));
    assertEquals("match2", listMatchPatternWithRestPattern16(["add", [1, 2]]));
    assertEquals("match3", listMatchPatternWithRestPattern16([1, 2, 3]));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
