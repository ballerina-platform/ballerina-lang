// Copyright [c] 2018 WSO2 Inc. [http://www.wso2.org] All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 [the "License"]; you may not use this file except
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

function testTupleVarRefBasic1() returns [string, int, boolean] {
    [string, [int, boolean]] [a, [b, c]] = ["Ballerina", [123, true]];
    [[a, b], c] = [["UpdatedBallerina", 453], false];

    return [a, b, c];
}

function testTupleVarRefBasic3() returns [string, int, boolean] {
    [string, [int, boolean]] t = ["Ballerina", [123, true]];
    var [a, [b, c]] = t;
    [[a, b], c] = [["UpdatedBallerina", 453], false];

    return [a, b, c];
}

function testTupleVarRefBasic4() returns [string, int, boolean] {
    [string, [int, boolean]] [a, [b, c]] = ["Ballerina", [123, true]];
    a = "UpdatedBallerina";
    b = 453;
    c = false;

    return [a, b, c];
}

function testTupleVarRefBasic6() returns [string, int, boolean] {
    [string, [int, boolean]] [a, [b, c]] = ["Ballerina", [123, true]];
    [a, b] = ["UpdatedBallerina", 453];
    c = false;

    return [a, b, c];
}

function testTupleVarRefBasic7() returns [string, int, boolean] {
    [string, [int, boolean]] [a, [b, c]] = ["Ballerina", [123, true]];
    [[a, b], c] = [["UpdatedBallerina", 453], false];

    return [a, b, c];
}

function testTupleVarRefBasic8() returns [string, int, boolean, float] {
    [string, [int, [boolean, float]]] [a, [b, [c, d]]] = ["Ballerina", [123, [true, 5.6]]];
    [a, [b, [c, d]]] = ["UpdatedBallerina", [453, [false, 12.34]]];
    return [a, b, c, d];
}

function testTupleVarRefBasic9() returns [string, int, boolean, float] {
    [string, [int, [boolean, float]]] [a, [b, [c, d]]] = ["Ballerina", [123, [true, 5.6]]];
    [[a, b], c] = [["UpdatedBallerina", 453], false];
    [b, c, d] = [657, true, 76.8];
    return [a, b, c, d];
}

function testTupleVarRefAssignment1() returns [string, int, boolean] {
    [string, [int, boolean]] [a, [b, c]] = ["Ballerina", [123, true]];
    [a, [b, c]] = foo();

    return [a, b, c];
}

function foo() returns [string, [int, boolean]] {
    return ["UpdatedBallerina", [453, false]];
}

function bar() returns [Foo, [Bar, [FooObj, BarObj]]] {
    Foo foo2 = {name:"TestUpdate", age:24};
    Bar bar2 = {id:35, flag:false};
    FooObj fooObj2 = new ("FoooUpdate", 4.7, 24);
    BarObj barObj2 = new (false, 66);

    return [foo2, [bar2, [fooObj2, barObj2]]];
}

function testTupleVarRefAssignment2() returns [string, int, int, boolean, string, float, byte, boolean, int] {
    Foo foo1 = {name:"Test", age:23};
    Bar bar1 = {id:34, flag:true};
    FooObj fooObj1 = new ("Fooo", 3.7, 23);
    BarObj barObj1 = new (true, 56);

    [Foo, [Bar, [FooObj, BarObj]]] [a, [b, [c, d]]] = [foo1, [bar1, [fooObj1, barObj1]]];

    Foo foo2 = {name:"TestUpdate", age:24};
    Bar bar2 = {id:35, flag:false};
    FooObj fooObj2 = new ("FoooUpdate", 4.7, 24);
    BarObj barObj2 = new (false, 66);

    [a, [b, [c, d]]] = [foo2, [bar2, [fooObj2, barObj2]]];

    return [a.name, a.age, b.id, b.flag, c.s, c.f, c.b, d.b, d.i];
}

function testTupleVarRefAssignment3() returns [string, int, int, boolean, string, float, byte, boolean, int] {
    Foo foo1 = {name:"Test", age:23};
    Bar bar1 = {id:34, flag:true};
    FooObj fooObj1 = new ("Fooo", 3.7, 23);
    BarObj barObj1 = new (true, 56);

    [Foo, [Bar, [FooObj, BarObj]]] [a, [b, [c, d]]] = [foo1, [bar1, [fooObj1, barObj1]]];

    [a, [b, [c, d]]] = bar();

    return [a.name, a.age, b.id, b.flag, c.s, c.f, c.b, d.b, d.i];
}

type Foo record {
    string name;
    int age;
};

type Bar record {
    int id;
    boolean flag;
};

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

function testTupleVarRefWithArray1() returns [string, int[], boolean, float[]] {
    [string, [int[], [boolean, float[]]]] [a, [b, [c, d]]] = ["Test", [[32, 67], [false, [6.3, 4.2]]]];
    [a, [b, [c, d]]] = ["Ballerina", [[123, 345], [true, [2.3, 4.5]]]];

    return [a, b, c, d];
}

function testTupleVarRefWithArray2() returns [string[], int[], boolean[], float[]] {
    [string[], [int[], [boolean[], float[]]]] [a, [b, [c, d]]] = [["Q", "W", "R"], [[456, 876, 56, 78], [[false, true, false], [3.5, 6.7, 9.8]]]];
    [a, [b, [c, d]]] = [["A", "B"], [[123, 345], [[true, false], [2.3, 4.5]]]];

    return [a, b, c, d];
}

function testTupleVarRefWithArray3() returns [string[][], int[][], float[]] {
    [string[][], [int[][], float[]]] [a, [b, c]] = [[["W", "R"], ["G", "H"]], [[[44, 66], [2, 6, 8]], [7.3, 6.7, 7.8]]];
    [a, [b, c]] = [[["A", "B"], ["C", "D"]], [[[123, 345], [12, 34, 56]], [2.3, 4.5]]];

    return [a, b, c];
}

function testVarRefWithUnionType1() returns [string|int|float, string|float, string] {
    [string|int|float, [string|float, string]] [a, [b, c]] = [2, [56.7, "Hello"]];
    [a, [b, c]] = [34, [6.7, "Test"]];
    return [a, b, c];
}

function testVarRefWithUnionType2() returns [string|int|float, string|float, string] {
    string|int|float v1 = 34;
    string|float v2 = 6.7;
    string v3 = "Test";

    [string|int|float, [string|float, string]] [a, [b, c]] = [2, [56.7, "Hello"]];
    [a, [b, c]] = [v1, [v2, v3]];
    return [a, b, c];
}

function testVarRefWithUnionType3() returns [string|int|float, string|float, string] {
    [string|int|float, [string|float, string]] [a, [b, c]] = [2, [56.7, "Hello"]];
    [a, [b, c]] = fn2();
    return [a, b, c];
}

function fn2() returns [string|int|float, [string|float, string]] {
    [string|int|float, [string|float, string]] v = [34, [6.7, "Test"]];
    return v;
}

function testVarRefWithUnionType4() returns [[string|int, int|boolean], float|[int, boolean], [string|float, string]] {
    [[string|int, int|boolean], float|[int, boolean], [string|float, string]] [[a, b], c, [d, e]] = [[12, true], [45, false], ["Hello", "World"]];
    [[a, b], c, [d, e]] = [["TestUpdated", 23], 4.5, [5.7, "FooUpdated"]];
    return [[a, b], c, [d, e]];
}

function fn3() returns [[string|int, int|boolean], float|[int, boolean], [string|float, string]] {
    [[string|int, int|boolean], float|[int, boolean], [string|float, string]] [[v1, v2], v3, [v4, v5]] = [["TestUpdated", 23], 4.5, [5.7, "FooUpdated"]];
    return [[v1, v2], v3, [v4, v5]];
}

function testVarRefWithUnionType5() returns [[string|int, int|boolean], float|[int, boolean], [string|float, string]] {
    [[string|int, int|boolean], float|[int, boolean], [string|float, string]] [[a, b], c, [d, e]] = [["Test", 23], [45, false], ["Hello", "World"]];
    [[a, b], c, [d, e]] = fn3();
    return [[a, b], c, [d, e]];
}

function testRestVarRefType1() {
    [string, int, float...] t1 = ["A", 100, 200.5, 300.5];

    var [a1, ...a2] = t1;
    assertEqual("typedesc A", (typeof a1).toString());
    assertEqual("typedesc [int,float...]", (typeof a2).toString());

    [int, float...] x1;
    x1 = a2;
    assertEqual(3, x1.length());
    assertEqual(100, x1[0]);
    assertEqual(200.5, x1[1]);
    assertEqual(300.5, x1[2]);

    var [b1, b2, ...b3] = t1;
    assertEqual("typedesc A", (typeof b1).toString());
    assertEqual("typedesc 100", (typeof b2).toString());
    assertEqual("typedesc float[]", (typeof b3).toString());

    float[] x2;
    x2 = b3;
    assertEqual(2, x2.length());
    assertEqual(200.5, x2[0]);
    assertEqual(300.5, x2[1]);
}

function testRestVarRefType2() {
    [[int...]...] t2 = [[1, 2, 3, 4], [5, 6]];
    var [...c1] = t2;
    assertEqual("typedesc [int...][]", (typeof c1).toString());
    assertEqual(2, c1.length());
    assertEqual(true, c1[0] is int[]);
    foreach int i in 0...3 {
        assertEqual(i + 1, c1[0][i]);
    }
    assertEqual(true, c1[1] is int[]);
    assertEqual(5, c1[1][0]);
    assertEqual(6, c1[1][1]);
}

function testRestVarRefType3() {
    [int, string] t3 = [12, "A"];
    var [d1, ...d2] = t3;
    assertEqual("typedesc [string]", (typeof d2).toString());

    [string] x3 = d2;
    assertEqual(1, x3.length());
    assertEqual("A", x3[0]);
}

function testRestVarRefType4() {
    [[string, int], boolean...] [[e1, e2], ...e3] = [["Ballerina", 453], false];
    assertEqual("typedesc boolean[]", (typeof e3).toString());
    assertEqual(1, e3.length());
    assertEqual(false, e3[0]);

    Bar e4;
    int[] e5;
    [e1, [e2, ...e3], e4, ...e5] = ["Hello Ballerina", [525, true, false, true], {id: 34, flag: true}, 12, 13];
    assertEqual("typedesc boolean[]", (typeof e3).toString());
    assertEqual("Hello Ballerina", e1);
    assertEqual(525, e2);
    assertEqual(3, e3.length());
    assertEqual(true, e3[0]);
    assertEqual(false, e3[1]);
    assertEqual(true, e3[2]);
    assertEqual(34, e4.id);
    assertEqual(true, e4.flag);
    assertEqual(2, e5.length());
    assertEqual(12, e5[0]);
    assertEqual(13, e5[1]);
}

function testRestVarRefType5() {
    [[string, Bar, boolean...], string, boolean[]] t4 = [["Ballerina", {id: 34, flag: true}, false], "A", [true, false]];
    var [[f1, ...f2], ...f3] = t4;
    assertEqual("typedesc [Bar,boolean...]", (typeof f2).toString());
    assertEqual(2, f2.length());
    assertEqual(34, f2[0].id);
    assertEqual(true, f2[0].flag);
    assertEqual("typedesc [string,boolean[]]", (typeof f3).toString());
    assertEqual(2, f3.length());
    assertEqual("A", f3[0]);
    assertEqual(true, f3[1][0]);
    assertEqual(false, f3[1][1]);
}

function testRestVarRefType6() {
    FooObj fooObj1 = new ("Fooo", 3.7, 23);
    BarObj barObj1 = new (true, 56);
    [[string, [error, map<string>, int, (FooObj|BarObj)...], Bar, (byte|float)...], string, boolean...] t5 =
                [["Ballerina", [error("Error", detail1= 12, detail2= true),
                {firstName: "John", lastName: "Damon"}, 12, fooObj1, barObj1], {id: 34, flag: true}, 10.5, 20], "A",
                true, false];
    var [[g1, [g2, ...g3], ...g4], ...g5] = t5;
    assertEqual("typedesc Ballerina", (typeof g1).toString());
    assertEqual("typedesc error", (typeof g2).toString());
    assertEqual("typedesc [map<string>,int,(FooObj|BarObj)...]", (typeof g3).toString());
    assertEqual("typedesc [Bar,(byte|float)...]", (typeof g4).toString());
    assertEqual("typedesc [string,boolean...]", (typeof g5).toString());
    assertEqual("Ballerina", g1);
    assertEqual("Error", g2.message());
    assertEqual(12, g2.detail()["detail1"]);
    assertEqual(true, g2.detail()["detail2"]);
    assertEqual(4, g3.length());
    assertEqual("John", g3[0]["firstName"]);
    assertEqual("Damon", g3[0]["lastName"]);
    assertEqual(12, g3[1]);
    assertEqual(fooObj1, g3[2]);
    assertEqual(barObj1, g3[3]);
    assertEqual(3, g4.length());
    assertEqual(34, g4[0].id);
    assertEqual(true, g4[0].flag);
    assertEqual(10.5, g4[1]);
    assertEqual(20, g4[2]);
    assertEqual(3, g5.length());
    assertEqual("A", g5[0]);
    assertEqual(true, g5[1]);
    assertEqual(false, g5[2]);

    FooObj fooObj2 = new ("Foo2", 10.2, 30);
    BarObj barObj2 = new (false, 56);
    BarObj barObj3 = new (true, 58);
    [[g1, g2, ...g3], [...g5], ...g4] = [["Hello", error("Transaction Error"), [{primary: "Blue", secondary: "Green"},
                1000, barObj2, fooObj2, barObj3]], [["World", true, false, true, false]], [{id: 40, flag: true},
                0x5, 0x7, 20.25, 0x8]];
    assertEqual("Hello", g1);
    assertEqual("Transaction Error", g2.message());
    assertEqual(5, g3.length());
    assertEqual("Blue", g3[0]["primary"]);
    assertEqual("Green", g3[0]["secondary"]);
    assertEqual(1000, g3[1]);
    assertEqual(true, g3[2] is BarObj);
    assertEqual(false, (<BarObj>g3[2]).b);
    assertEqual(56, (<BarObj>g3[2]).i);
    assertEqual(true, g3[3] is FooObj);
    assertEqual("Foo2", (<FooObj>g3[3]).s);
    assertEqual(10.2, (<FooObj>g3[3]).f);
    assertEqual(30, (<FooObj>g3[3]).b);
    assertEqual(true, g3[4] is BarObj);
    assertEqual(true, (<BarObj>g3[4]).b);
    assertEqual(58, (<BarObj>g3[4]).i);
    assertEqual(5, g5.length());
    assertEqual("World", g5[0]);
    assertEqual(true, g5[1]);
    assertEqual(false, g5[2]);
    assertEqual(true, g5[3]);
    assertEqual(false, g5[4]);
    assertEqual(5, g4.length());
    assertEqual(40, g4[0].id);
    assertEqual(true, g4[0].flag);
    assertEqual(5, g4[1]);
    assertEqual(7, g4[2]);
    assertEqual(20.25, g4[3]);
    assertEqual(8, g4[4]);
}

function testRestVarRefType7() {
    int[5] t6 = [10, 20, 30, 40, 50];
    var [h1, h2, ...h3] = t6;
    assertEqual("typedesc 10", (typeof h1).toString());
    assertEqual("typedesc 20", (typeof h2).toString());
    assertEqual("typedesc [int,int,int,int...]", (typeof h3).toString());
    assertEqual(10, h1);
    assertEqual(20, h2);
    assertEqual(3, h3.length());
    assertEqual(30, h3[0]);
    assertEqual(40, h3[1]);
    assertEqual(50, h3[2]);
}

function testRestVarRefType8() {
    (int|Bar)[5] t7 = [10, 20, {id: 34, flag: true}, 40, {id: 35, flag: false}];
    var [h1, h2, ...h3] = t7;
    assertEqual("typedesc [(int|Bar),(int|Bar),(int|Bar),(int|Bar)...]", (typeof h3).toString());
    assertEqual(10, h1);
    assertEqual(20, h2);
    assertEqual(3, h3.length());
    assertEqual(true, h3[0] is Bar);
    assertEqual(34, (<Bar>h3[0]).id);
    assertEqual(true, (<Bar>h3[0]).flag);
    assertEqual(40, h3[1]);
    assertEqual(true, h3[2] is Bar);
    assertEqual(35, (<Bar>h3[2]).id);
    assertEqual(false, (<Bar>h3[2]).flag);
}

function testRestVarRefType9() {
    FooObj fooObj1 = new ("Fooo", 3.7, 23);
    [[string, [FooObj, Bar...], int, float...], boolean[3], string...] [[i1, [...i2], ...i3], ...i4] =
                [["Ballerina", [fooObj1, {id: 34, flag: true}, {id: 35, flag: false}], 453, 10.5, 20.5],
                [false, true, true], "Ballerina", "Hello"];
    assertEqual("typedesc Ballerina", (typeof i1).toString());
    assertEqual("typedesc [FooObj,Bar...]", (typeof i2).toString());
    assertEqual("typedesc [int,float...]", (typeof i3).toString());
    assertEqual("typedesc [boolean[3],string...]", (typeof i4).toString());
    assertEqual("Ballerina", i1);
    assertEqual(3, i2.length());
    assertEqual(fooObj1, i2[0]);
    assertEqual(34, i2[1].id);
    assertEqual(true, i2[1].flag);
    assertEqual(35, i2[2].id);
    assertEqual(false, i2[2].flag);
    assertEqual(3, i3.length());
    assertEqual(453, i3[0]);
    assertEqual(10.5, i3[1]);
    assertEqual(20.5, i3[2]);
    assertEqual(3, i4.length());
    assertEqual(3, i4[0].length());
    assertEqual(false, i4[0][0]);
    assertEqual(true, i4[0][1]);
    assertEqual(true, i4[0][2]);
    assertEqual("Ballerina", i4[1]);
    assertEqual("Hello", i4[2]);

    [[[i1, ...i2], [...i3], ...i4]] = [[["World", [fooObj1, {id: 36, flag: false}, {id: 37, flag: true},
                              {id: 38, flag: true}]], [[100, 200.5, 300.5, 400.5]], [[false, false, false],
                              "John", "Mary", "Sheldon"]]];
    assertEqual("World", i1);
    assertEqual(4, i2.length());
    assertEqual(fooObj1, i2[0]);
    assertEqual(36, i2[1].id);
    assertEqual(false, i2[1].flag);
    assertEqual(37, i2[2].id);
    assertEqual(true, i2[2].flag);
    assertEqual(38, i2[3].id);
    assertEqual(true, i2[3].flag);
    assertEqual(4, i3.length());
    assertEqual(100, i3[0]);
    assertEqual(200.5, i3[1]);
    assertEqual(300.5, i3[2]);
    assertEqual(400.5, i3[3]);
    assertEqual(4, i4.length());
    assertEqual(3, i4[0].length());
    assertEqual(false, i4[0][0]);
    assertEqual(false, i4[0][1]);
    assertEqual(false, i4[0][2]);
    assertEqual("John", i4[1]);
    assertEqual("Mary", i4[2]);
    assertEqual("Sheldon", i4[3]);
}

type ReadOnlyTuple readonly & [int[], string];

function testReadOnlyTupleWithListBindingPatternInDestructuringAssignment() {
    ReadOnlyTuple t1 = [[1, 2], "s1"];
    int[] & readonly a;
    string b;
    [a, b] = t1;
    assertEqual(<int[]> [1, 2], a);
    assertEqual("s1", b);

    readonly & [int[], ReadOnlyTuple] t2 = [[12, 34, 56], t1];
    readonly & int[] c;
    readonly & int[] d;
    string e;
    [c, [d, e]] = t2;
    assertEqual(<int[]> [12, 34, 56], c);
    assertEqual(<int[]> [1, 2], d);
    assertEqual("s1", e);

    int[] f;
    int[] g;
    string h;
    [f, [g, h]] = t2;
    assertEqual(<int[]> [12, 34, 56], f);
    assertEqual(<int[]> [1, 2], g);
    assertEqual("s1", h);
}

function assertEqual(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("expected '" + (expected is error ? expected.toString() : expected.toString()) + "', found '" +
                    (actual is error ? actual.toString() : actual.toString()) + "'");
}
