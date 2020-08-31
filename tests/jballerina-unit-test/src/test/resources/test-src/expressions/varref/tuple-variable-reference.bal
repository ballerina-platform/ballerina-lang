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

