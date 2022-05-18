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

import ballerina/test;

function testSimpleBasic2DArrays() {
    int[][] iarr = [];
    iarr[2][4] = 10;

    float[][] farr = [];
    farr[4][3] = 12.34;
    farr[2][2] = 45.67;
    farr[2][5] = 6.78;

    decimal[][] darr = [];
    darr[1][3] = 12.34;
    darr[2][2] = 45.67;
    darr[2][5] = 6.78;

    boolean[][] barr = [];
    barr[3][3] = true;

    string[][] sarr = [];
    sarr[3][3] = "foo";

    assert(<int[][]>[[], [], [0, 0, 0, 0, 10]], iarr);
    assert(<float[][]>[[], [], [0, 0, 45.67, 0, 0, 6.78], [], [0, 0, 0, 12.34]], farr);
    assert(<decimal[][]>[[], [0, 0, 0, 12.34], [0, 0, 45.67, 0, 0, 6.78]], darr);
    assert(<boolean[][]>[[], [], [], [false, false, false, true]], barr);
    assert(<string[][]>[[], [], [], ["", "", "", "foo"]], sarr);

    assert(3, iarr.length());
    assert(5, farr.length());
    assert(3, darr.length());
    assert(4, barr.length());
    assert(4, sarr.length());
}

type Person record {|
    string name = "Pubudu";
|};

function testRecordArrays() {
    Person[] arr = [];
    arr[1].name = "John Doe";

    assert(<Person[]>[{name: "Pubudu"}, {name: "John Doe"}], arr);
}

function test2DRecordArrays() {
    Person[][] arr = [];
    arr[2][1] = {name: "John Doe"};

    assert(<Person[][]>[[], [], [{name: "Pubudu"}, {name: "John Doe"}]], arr);
    assert(3, arr.length());
    assert(2, arr[2].length());
}

function testRecordsWithoutFillerValues() {
    record {| string name; |}[][] arr = [];
    arr[2][1] = {name: "Pubudu"};
}

function testRecordsWithoutFillerValues2() {
    record {| string name; |}[][2] arr = [];
    arr[0][1] = {name: "Pubudu"};
}

class PersonObj {
    string name = "John Doe";
}

function testObjectArrays() returns PersonObj[] {
    PersonObj[] arr = [];
    arr[1].name = "Pubudu";
    return arr;
}

function test2DObjectArrays() returns PersonObj[][] {
    PersonObj[][] arr = [];
    arr[2][1] = new;
    return arr;
}

class PersonObj2 {
    string name = "John Doe";

    public function init(string name = "Pubudu") {
        self.name = name;
    }
}

function test2DObjectArrays2() {
    PersonObj2[][] arr = [];
    arr[2][1] = new;

    test:assertEquals(arr[0], []);
    test:assertEquals(arr[1], []);
    test:assertEquals(arr[2].length(), 2);
    test:assertEquals(arr[2][0].name, "Pubudu");
    test:assertEquals(arr[2][1].name, "Pubudu");
}

class PersonObj3 {
    string name = "John Doe";

    public function init(string name) {
        self.name = name;
    }
}

function test2DObjectArrays3() returns PersonObj3[][] {
    PersonObj3[][] arr = [];
    arr[2][1] = new("Pubudu");
    return arr;
}

class PersonObj4 {
    int id;
    string name;

    public function init(int id, string name = "Alex") {
        self.id = id;
        self.name = name;
    }
}

function testObjectArrays2() {
    PersonObj4 p = new(1001);
    PersonObj4[] arr = [];
    arr[1] = p;
}

type Foo record {
    Person[][][] arr;
};

function testArraysInRecordFields() {
    Foo f = {arr: []};
    f.arr[1][2][3] = {name: "Fernando"};

    assert(<Foo>{arr: [[], [[], [], [{name: "Pubudu"}, {name: "Pubudu"}, {name: "Pubudu"}, {name: "Fernando"}]]]}, f);
}

class FooObj {
    Person[][][] arr = [];
}

function testArraysInObjectFields() {
    FooObj f = new;
    f.arr[1][2][3] = {name: "Fernando"};

    assert(<Person[][][]>[[], [[], [], [{name: "Pubudu"}, {name: "Pubudu"}, {name: "Pubudu"}, {name: "Fernando"}]]], f.arr);
}

function testArraysInUnionTypes() {
    string[][][]|() arr = [];

    if (arr is string[][][]) {
        arr[1][2][1] = "Foo";
    }

    assert(<string[][][]>[[], [[], [], ["", "Foo"]]], arr);
}

function testArraysOfTuples() {
    [string, int][][] arr = [];
    arr[2][2] = ["foo", 10];

    assert(<[string, int][][]>[[], [], [["", 0], ["", 0], ["foo", 10]]], arr);
}

function test2DArrayInATuple() {
    [string, int[][]] tup = ["foo", []];
    tup[1][2][1] = 100;

    assert(<[string, int[][]]>["foo", [[], [], [0, 100]]], tup);
}

type Bar 0|1|2;

function testFiniteTyped2DArrays() {
    Bar[][][] arr = [];
    arr[2][1][1] = 2;

    assert(<Bar[][][]>[[], [], [[], [0, 2]]], arr);
}

type NoDefBar 1|2;

function testNoDefFiniteTyped2DArrays() {
    NoDefBar[][][] arr = [];
    arr[2][1][1] = 2;
}

function testMapArrayAsAnLValue() {
    map<int>[] arr = [];
    arr[1]["i"] = 1;

    assert(<map<int>[]>[{}, {"i": 1}], arr);
}

type unionTypeWithNil 1|"foo"|();

function testMDArrayFillerValues() {
    int[2][102] arr1 = [[11, 22, 33]];
    test:assertEquals(arr1, [[11,22,33,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]]);

    float[2][3][4] arr2 = [[[1,2],[3]]];
    test:assertEquals(arr2, [[[1.0,2.0,0.0,0.0],[3.0,0.0,0.0,0.0],[0.0,0.0,0.0,0.0]],[[0.0,0.0,0.0,0.0],[0.0,0.0,0.0,
    0.0],[0.0,0.0,0.0,0.0]]]);

    string[2][3][4] arr3 = [[["a","b"],["c","d","e","f"],["g"]]];
    test:assertEquals(arr3, [[["a","b","",""],["c","d","e","f"],["g","","",""]],[["","","",""],["","","",""],
    ["","","",""]]]);

    unionTypeWithNil[2][4] arr4 = [[1, 1, "foo"]];
    test:assertEquals(arr4, [[1,1,"foo",null],[null,null,null,null]]);

    map<int[3]>[] arr5 = [];
    arr5[3]["i"] = [1,2];
    arr5[1]["k"] = [3];
    test:assertEquals(arr5, [{},{"k":[3,0,0]},{},{"i":[1,2,0]}]);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                        + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = msg);
}
