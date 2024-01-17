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
import ballerina/lang.array;

function testPushOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.push(4);
}

function testPopOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int popped = fixedLengthArray.pop();
}

function testPopOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.pop();
}

function testShift() returns [int[], int] {
    int[] s = [1, 2, 3, 4, 5];
    var e = s.shift();
    return [s, e];
}

function testShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int x = fixedLengthArray.shift();
}

function testUnShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.unshift(5);
}

function testShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.shift();
}

function testUnShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.unshift();
}

// inferred fixed length arrays
function testPushPopShiftUnshitOnInferredFixedLengthArray() {
    int[*] fixedLengthArray = [1, 2];
    fixedLengthArray.push(4);
    int x = fixedLengthArray.pop();
    x = fixedLengthArray.shift();
    fixedLengthArray.unshift();
}

function testPushOnFixedLengthArrayUnions() {
    int[1] | float[1] fixedLengthArray = <int[1]> [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTupleUnion() {
    [int, int][1] | [float, float][1] fixedLengthArray = <[float, float][1]> [[1.0, 2.3]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

function testShiftOnTupleWithStringRestParamFixedInherantShapeWithInt() {
    [int, string...] a = [1, "hello","world"];
    int | string x = a.shift();
}

function testShiftOnTupleWithIntRestParamFixedInherantShapeWithIntString() {
    [int, string, int...] a = [1, "hello", 5];
    int | string x = a.shift();
}

// run time panic no compile time error
function testPushOnFixedLengthAndDynamicTupleUnion() {
    [int, int][1] | [float, float][] fixedLengthArray = <[float, float][1]> [[1, 2]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

function testPushOnFixedLengthTupleUnions() {
    [int, int] | [float, float] fixedLengthArray = <[float, float]> [1, 2];
    fixedLengthArray.push(<float>1);
}

// run time panic no compile time error
function testShiftOnTupleWithIntRestParamFixedInherantShapeWithInt() {
    [int, int...] a = [1];
    int x = a.shift();
}

// run time panic no compile time error
function testShiftOnUnionOfDifferentTuples() {
    [string, string...]|[string, int] tuples = ["hi", 4];
    var x = tuples.shift();
}

function testShiftOnUnionOfDifferentTuplesTypeNarrowed() {
    [string, string...]|[string, int] tuples = ["hi", 4];
    if (tuples is [string, int]) {
        var x = tuples.shift();
    }
}

type Person record {|
    string name;
|};

Person person1 = {name: "Mike"};
Person person2 = {name: "John"};

function testArrSortNegativeScenarios() {
    int[] arr = [21, 76, 2, 20, 10, 5];

    int[] sortedArr = arr.sort(function(int x) returns int {
        return x;
    });

    int[] sortedArr2 = arr.sort(function(int x) returns int {
        return x;
    }, array:DESCENDING);

    (Person|int)[] arr2 = [23, person1, person2, 10, 13];

    (Person|int)[] sortedArr3 = arr2.sort();

    (Person|int)[] sortedArr4 = arr2.sort(array:DESCENDING);

    (Person|int)[] sortedArr5 = arr2.sort(array:DESCENDING, ());

    (Person|int)[] sortedArr6 = arr2.sort(array:DESCENDING, isolated function((Person|int) val) returns Person|int {
        return val;
    });

    (map<string>)?[] arr3 = [{"A": "a", "B": "b"}, (), {"X": "x", "Y": "y"}];

    (map<string>)?[] sortedArr7 = arr3.sort();

    (map<string>)?[] sortedArr8 = arr3.sort(array:ASCENDING, isolated function((map<string>)? x) returns (map<string>)? {
        return x;
    });

    int[] sortedArr9 = arr.sort(array:ASCENDING, isolated function(int x) returns int {
            return x;
    }, "descending");

    (Person|int)[] sortedArr10 = array:sort(arr2);

    (Person|int)[] sortedArr11 = array:sort(arr2, array:DESCENDING);

    (Person|int)[] sortedArr12 = array:sort(arr2, array:DESCENDING, ());

    (map<string>)?[] sortedArr13 = array:sort(arr3);

    any[] arr4 = [2, "AC", {"x":10}, 12.30];
    any[] sortedArr16 = arr4.sort(array:DESCENDING, (x) => x);

    (string|int)[] arr5 = [23, "A", "Z", 10, "D"];

    (string|int)[] sortedArr17 = arr5.sort();

    (string|int)[] sortedArr18 = arr5.sort(array:DESCENDING);

    (string|int)[] sortedArr19 = arr5.sort(array:DESCENDING, ());

    (string|int)[] sortedArr20 = arr5.sort(array:DESCENDING, isolated function((string|int) val) returns string|int {
        return val;
    });
}

function passThrough(int int1) returns int {
    return int1;
}

function testLastIndexOf() {
    (Person|error)[] personArray = [];
    int? i1 = personArray.lastIndexOf(person1);
    int? i2 = personArray.indexOf(person1);
    function[] sd = [];
    function (int) returns (int) d;
    int? i3 = sd.indexOf(d);
}

function func1(int val) returns boolean {
    return val > 2;
}

function testSomeNegative1() {
    _ = [1, 2].som(func1); // error
}

function func2(int val) returns int {
    return val + 1;
}

function testSomeNegative2() {
    _ = [1, 2].some(func2); // error
}

function testSomeNegative3() {
    int _ = [1, 2].some(func1); // error
}

function testSomeNegative4() {
    string[] arr = ["str1", "str2"];
    _ = arr.some(func1); // error
}

function testSomeNegative5() {
    string[] arr = ["str1", "str2"];
    _ = arr.some(val => val > 5); // error
}

function testEveryNegative1() {
    _ = [1, 2].ever(func1); // error
}

function testEveryNegative2() {
    _ = [1, 2].every(func2); // error
}

function testEveryNegative3() {
    int _ = [1, 2].every(func1); // error
}

function testEveryNegative4() {
    string[] arr = ["str1", "str2"];
    _ = arr.every(func1); // error
}

function testEveryNegative5() {
    string[] arr = ["str1", "str2"];
    _ = arr.every(val => val > 5); // error
}

type T string|int;

function testArrSortWithNamedArgs1() {
    [string, T][] arr = [["a", "100"], ["b", "100"], ["d", "10"], ["c", "100"], ["e", "100"]];
    [string, T][] sortedArr = arr.sort(direction = array:DESCENDING);
    sortedArr = array:sort(arr, direction = array:DESCENDING);

    sortedArr = array:sort(arr, direction = array:DESCENDING, key = isolated function([string, T] e) returns [string, T] {
        return e;
    });
    sortedArr = arr.sort(key = isolated function([string, T] e) returns [string, T] {
        return e;
    });

    sortedArr = array:sort(arr, direction = array:DESCENDING, key = ());
}

class Obj {
    int i;
    int j;

    function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

function testPushWithObjectConstructorExprNegative() {
    Obj[] arr = [];
    arr.push(
        object { // OK
            int i = 123;
            int j = 234;
        },
        object { // error
            int i = 123;
        }
    );
}

type Template object {
    *object:RawTemplate;

    public (readonly & string[]) strings;
    public int[] insertions;
};

function testPushWithRawTemplateExprNegative() {
    Template[] arr = [];
    string i = "12345";
    arr.push(
        `number ${i}`, // error
        `second number ${1 + 3} third ${i + "1"}` // error for second interpolations
    );
}

type Department record {|
    readonly string name;
    int empCount;
|};

function testPushWithTableConstructorExprNegative() {
    (table<Department> key(name))[] arr = [];
    arr.push(
        table [{name: "finance"}] // error for missing `empCount`
    );
}

function testPushWithNewExprNegative() {
    Obj[] arr = [];
    arr.push(new (1)); // error for missing argument
}

type Error error<record {| int code; |}>;

function testPushWithErrorConstructorExprNegative() {
    Error[] arr = [];
    arr.push(error("e1")); // error for missing `code`
}
