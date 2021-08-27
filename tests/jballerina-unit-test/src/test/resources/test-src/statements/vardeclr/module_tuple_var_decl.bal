//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// Test module level list binding pattern
[int, float] [a, b] = [1, 2.5];
[boolean, float, string] [c, d, e] = [true, 2.25, "Jhone"];
public function testBasic() {
    while (d < 3.0) {
        d += 1.0;
    }
    assertTrue(c);
    assertEquality(3.25, d);
    assertEquality("Jhone", e);
}

// Recursive list binding pattern with objects
Foo foo = {name:"Test", age:23};
Bar bar = {id:34, flag:true};
FooObj fooObj = new ("Fooo", 3.7, 23);
BarObj barObj = new (true, 56);
[string, [Foo, [BarObj, FooObj]], [Bar, int]] [a2, [b2, [c2, d2]], [e2, f2]] = [foo.name, [foo, [barObj, fooObj]], [bar, barObj.i]];

function testTupleBindingWithRecordsAndObjects() {
    assertEquality("Test", a2);
    assertEquality("Test", b2.name);
    assertEquality(23, b2.age);
    assertEquality(34, e2.id);
    assertTrue(e2.flag);
    assertEquality("Fooo", d2.s);
    assertEquality(3.7, d2.f);
    assertEquality(23, d2.b);
    assertTrue(c2.b);
    assertEquality(56, c2.i);
    assertEquality(56, f2);
}

// Test tuple binding pattern with rest binding pattern
[int, string...] [Id, ...FullName] = [1002, "Peter", "Parker"];
[int, string...] [Id2, ...FullName2] = [1003, "Jhone", "Anistine"];
function testTupleBindingPatternWithRestBindingPattern() {
    assertEquality(1002, Id);
    assertEquality("Peter", FullName[0]);
    assertEquality("Parker", FullName[1]);
    assertEquality(1003, Id2);
    assertEquality("Jhone", FullName2[0]);
    assertEquality("Anistine", FullName2[1]);
}

// Tuple variable declared with 'var'
[int, string] g = [1, ""];
var [h, i] = g;
function testDeclaredWithVar() {
    assertEquality(1, h);
    assertEquality("", i);
}

var [[intVar], {a: intVar2}, error(message), ...restBp] = getComplexTuple();

function getComplexTuple() returns [[int], map<int>, error, int...] => [[5], {a: 6}, error("error msg"), 12, 13];

var [{b: [intVar3, _]}] = getComplexTuple2();

function getComplexTuple2() returns [map<[int, string]>] => [{b: [20, "Ballerina"]}];

function testDeclaredWithVar2() {
    assertEquality(5, intVar);
    assertEquality(6, intVar2);
    assertEquality("error msg", message);
    assertEquality(12, restBp[0]);
    assertEquality(13, restBp[1]);
    assertEquality(20, intVar3);
}

var [[intVal], {a: intVal2}, ...otherValues] = getComplexTuple();

function testDeclaredWithVar3() {
    assertEquality(5, intVal);
    assertEquality(6, intVal2);
    int|error err = otherValues[0];
    assertEquality(true, err is error);
    error err0 = <error> err;
    assertEquality("error msg", err0.message());

    int|error val1 = otherValues[1];
    if (val1 is int) {
        assertEquality(12, val1);
    } else {
        panic getError("12", val1.toString());
    }

    int|error val2 = otherValues[2];
    if (val2 is int) {
        assertEquality(13, val2);
    } else {
        panic getError("13", val2.toString());
    }
}

// Test tuple var declaration with annotations
const annotation map<string> annot on source var;

@annot {
    value: "annotationValue"
}
[int, int] [j, k] = [1, 2];
@annot {
    value: "annotationValue"
}
var [j1, k1] = g;
public function testTupleVarWithAnnotations() {
    assertEquality(1, j);
    assertEquality(2, k);
    assertEquality(1, j1);
    assertEquality("", k1);
}

annotation record {int i;} x on function;
@x {
    i: h
}
public function testVariableDeclaredInTupleAsAnnotationValue() {
    typedesc<function ()> td = typeof testVariableDeclaredInTupleAsAnnotationValue;
    record {int i;}? xVal = td.@x;
    assertEquality(<record {int i;}>{i:1}, xVal);
}

// Test tuple variable reordering/forward referencing
[decimal, byte] [l, m] = [n, o];
[decimal, byte] [n, o] = [2.25, 20];
public function testVariableForwardReferencing() {
    assertEquality(<decimal> 2.25, l);
    assertEquality(20, m);
}

// Test rest type inference and rest param value resolution.
[[string, Bar, boolean...], string, boolean...] t1 = [["Ballerina", {id: 34, flag: true}, false], "A", true, false];
var [[q1, ...q2], ...q3] = t1;

function testModuleLevelTupleRest1() {
    assertEquality("Ballerina", q1);
    assertEquality("typedesc [Bar,boolean...]", (typeof q2).toString());
    assertEquality(2, q2.length());
    assertEquality(34, q2[0].id);
    assertEquality(true, q2[0].flag);
    assertEquality("typedesc [string,boolean...]", (typeof q3).toString());
    assertEquality(3, q3.length());
    assertEquality("A", q3[0]);
    assertEquality(true, q3[1]);
    assertEquality(false, q3[2]);
}

FooObj fooObj1 = new ("Fooo", 3.7, 23);
BarObj barObj1 = new (true, 56);
[[string, [error, map<string>, int, (FooObj|BarObj)...], Bar, (byte|float)...], string, boolean...] t2 =
            [["Ballerina", [error("Error", detail1= 12, detail2= true), {firstName: "John", lastName: "Damon"},
            12, fooObj1, barObj1], {id: 34, flag: true}, 10.5, 20], "A", true, false];
var [[g1, [g2, ...g3], ...g4], ...g5] = t2;

function testModuleLevelTupleRest2() {
    assertEquality("typedesc Ballerina", (typeof g1).toString());
    assertEquality("typedesc error", (typeof g2).toString());
    assertEquality("typedesc [map<string>,int,(FooObj|BarObj)...]", (typeof g3).toString());
    assertEquality("typedesc [Bar,(byte|float)...]", (typeof g4).toString());
    assertEquality("typedesc [string,boolean...]", (typeof g5).toString());
    assertEquality("Ballerina", g1);
    assertEquality("Error", g2.message());
    assertEquality(12, g2.detail()["detail1"]);
    assertEquality(true, g2.detail()["detail2"]);
    assertEquality(4, g3.length());
    assertEquality("John", g3[0]["firstName"]);
    assertEquality("Damon", g3[0]["lastName"]);
    assertEquality(12, g3[1]);
    assertEquality(fooObj1, g3[2]);
    assertEquality(barObj1, g3[3]);
    assertEquality(3, g4.length());
    assertEquality(34, g4[0].id);
    assertEquality(true, g4[0].flag);
    assertEquality(10.5, g4[1]);
    assertEquality(20, g4[2]);
    assertEquality(3, g5.length());
    assertEquality("A", g5[0]);
    assertEquality(true, g5[1]);
    assertEquality(false, g5[2]);
}

int[5] t3 = [10, 20, 30, 40, 50];
var [h1, h2, ...h3] = t3;

function testModuleLevelTupleRest3() {
    assertEquality("typedesc 10", (typeof h1).toString());
    assertEquality("typedesc 20", (typeof h2).toString());
    assertEquality("typedesc [int,int,int]", (typeof h3).toString());
    assertEquality(10, h1);
    assertEquality(20, h2);
    assertEquality(3, h3.length());
    assertEquality(30, h3[0]);
    assertEquality(40, h3[1]);
    assertEquality(50, h3[2]);
}

[[string, [FooObj, Bar...], int, float...], boolean[3], string...] [[i1, [...i2], ...i3], ...i4] =
            [["Ballerina", [fooObj1, {id: 34, flag: true}, {id: 35, flag: false}], 453, 10.5, 20.5],
            [false, true, true], "Ballerina", "Hello"];

function testModuleLevelTupleRest4() {
    assertEquality("typedesc Ballerina", (typeof i1).toString());
    assertEquality("typedesc [FooObj,Bar...]", (typeof i2).toString());
    assertEquality("typedesc [int,float...]", (typeof i3).toString());
    assertEquality("typedesc [boolean[3],string...]", (typeof i4).toString());
    assertEquality("Ballerina", i1);
    assertEquality(3, i2.length());
    assertEquality(fooObj1, i2[0]);
    assertEquality(34, i2[1].id);
    assertEquality(true, i2[1].flag);
    assertEquality(35, i2[2].id);
    assertEquality(false, i2[2].flag);
    assertEquality(3, i3.length());
    assertEquality(453, i3[0]);
    assertEquality(10.5, i3[1]);
    assertEquality(20.5, i3[2]);
    assertEquality(3, i4.length());
    assertEquality(3, i4[0].length());
    assertEquality(false, i4[0][0]);
    assertEquality(true, i4[0][1]);
    assertEquality(true, i4[0][2]);
    assertEquality("Ballerina", i4[1]);
    assertEquality("Hello", i4[2]);
}

// Support codes
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
    public function init(string s, float f, byte b) {
        self.s = s;
        self.f = f;
        self.b = b;
    }
}

class BarObj {
    public boolean b;
    public int i;
    public function init(boolean b, int i) {
        self.b = b;
        self.i = i;
    }
}

function getError(string expectedVal, string actualVal) returns error {
    return error("expected " + expectedVal + " found " + actualVal);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError", message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
