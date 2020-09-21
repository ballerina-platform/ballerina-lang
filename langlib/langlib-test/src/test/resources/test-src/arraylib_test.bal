// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.'string as strings;
import ballerina/lang.'int as ints;

function testLength() returns int {
    int[] arr = [10, 20, 30, 40];
    return arr.length();
}

function testIterator() returns string {
    string[] arr = ["Hello", "World!", "From", "Ballerina"];
    object {
         public function next() returns record {| string value; |}?;
    } itr = arr.iterator();

    record {| string value; |}|() elem = itr.next();
    string result = "";

    while (elem is record {| string value; |}) {
        result += elem.value;
        elem = itr.next();
    }

    return result;
}

function testEnumerate() returns [int, string][] {
    string[] arr = ["Hello", "World!", "From", "Ballerina"];
    return arr.enumerate();
}

function testMap() returns int[] {
    int[] arr = [10, 20, 30, 40];
    int[] newArr = arr.'map(function (int x) returns int {
        return x/10;
    });
    return newArr;
}

function testForeach() returns string {
    string?[] arr = ["Hello", "World!", (), "from", "Ballerina"];
    string result = "";

    arr.forEach(function (string? x) {
        if (x is string) {
            result += x;
        }
    });

    return result;
}

function testSlice() returns [float[], int, float[], int, float[], int] {
    float[] arr = [12.34, 23.45, 34.56, 45.67, 56.78];
    float[] r1 = arr.slice(1, 4);
    float[] r2 = arr.slice(2);
    float[] r3 = array:slice(arr, 3);
    return [r1, r1.length(), r2, r2.length(), r3, r3.length()];
}

function testPushAfterSlice() returns [int, int, float[]] {
     float[] arr = [12.34, 23.45, 34.56, 45.67, 56.78];
     float[] s = arr.slice(1, 4);
     int sl = s.length();
     s.push(20.1);
     int slp = s.length();
     return [sl, slp, s];
}

function testPushAfterSliceFixed() returns [int, int, int[]] {
     int[5] arr = [1, 2, 3, 4, 5];
     int[] s = arr.slice(3);
     int sl = s.length();
     s.push(88);
     int slp = s.length();
     return [sl, slp, s];
}

function testSliceOnTupleWithRestDesc() {
    [int, string...] x = [1, "hello", "world"];
    (int|string)[] a = x.slice(1);
    assertValueEquality(2, a.length());
    assertValueEquality("hello", a[0]);
    assertValueEquality("world", a[1]);

    [int, int, boolean...] y = [1, 2, true, false, true];
    (int|boolean)[] b = y.slice(1, 4);
    assertValueEquality(3, b.length());
    assertValueEquality(2, b[0]);
    assertTrue(b[1]);
    assertFalse(b[2]);
}

function testRemove() returns [string, string[]] {
    string[] arr = ["Foo", "Bar", "FooFoo", "BarBar"];
    string elem = arr.remove(2);
    return [elem, arr];
}

function testReduce() returns float {
    int[] arr = [12, 15, 7, 10, 25];
    float avg = arr.reduce(function (float accum, int val) returns float {
        return accum + <float>val / arr.length();
    }, 0.0);
    return avg;
}

type Grade "A+"|"A"|"A-"|"B+"|"B"|"B-"|"C"|"F";

function testIterableOpChain() returns float {
    [Grade, int][] grades = [["A+", 2], ["A-", 3], ["B", 3], ["C", 2]];

    int totalCredits = grades.reduce(function (int accum, [Grade, int] grade) returns int {
         return accum + grade[1];
     }, 0);

    float gpa = grades.'map(gradeToValue).reduce(function (float accum, [float, int] gradePoint) returns float {
        return accum + (gradePoint[0] * gradePoint[1]) / totalCredits;
    }, 0.0);

    return gpa;
}

function gradeToValue([Grade, int] grade) returns [float, int] {
    match grade[0] {
        "A+" => {return [4.2, grade[1]];}
        "A" => {return [4.0, grade[1]];}
        "A-" => {return [3.7, grade[1]];}
        "B+" => {return [3.3, grade[1]];}
        "B" => {return [3.0, grade[1]];}
        "B-" => {return [2.7, grade[1]];}
        "C" => {return [2.0, grade[1]];}
        "F" => {return [0.0, grade[1]];}
    }
    error e = error("Invalid grade: " + <string>grade[0]);
    panic e;
}

function testIndexOf() returns [int?, int?] {
    anydata[] arr = [10, "foo", 12.34, true, <map<string>>{"k":"Bar"}];
    map<string> m = {"k":"Bar"};
    int? i1 = arr.indexOf(m);
    int? i2 = arr.indexOf(50);
    return [i1, i2];
}

function testLastIndexOf() {
    anydata[] array = [10, 10, 10, "foo", "foo", "foo", 12.34, 12.34, true, true, <map<string>>{"k":"Bar"},
                       <map<string>>{"k":"Bar"}, [12, true], [12, true]];
    map<string> m1 = {"k":"Bar"};
    map<string> m2 = {"k":"Foo"};
    anydata[] arr1 = [12, true];
    anydata[] arr2 = [12, false];

    int? i1 = array.lastIndexOf(10);
    int? i2 = array.lastIndexOf("foo");
    int? i3 = array.lastIndexOf(12.34);
    int? i4 = array.lastIndexOf(true);
    int? i5 = array.lastIndexOf(m1);
    int? i6 = array.lastIndexOf(arr1);

    int? i7 = array.lastIndexOf(11);
    int? i8 = array.lastIndexOf("Bar");
    int? i9 = array.lastIndexOf(12.33);
    int? i10 = array.lastIndexOf(false);
    int? i11 = array.lastIndexOf(m2);
    int? i12 = array.lastIndexOf(arr2);

    if (<int>i1 != 2 && <int>i2 != 5 && <int>i3 != 7 && <int>i4 != 9 && <int>i5 != 11 && <int>i6 != 13 &&
                i7 != () && i8 != () && i9 != () && i10 != () && i11 != () && i12 != ()) {
        error err = error("'lastIndexOf' does not return correct value");
        panic err;
    }
}

function testReverse() returns [int[], int[]] {
    int[] arr = [10, 20, 30, 40, 50];
    return [arr, arr.reverse()];
}

type Person record {|
    int id;
    string name;
    int pilotingScore;
    int shootingScore;
    boolean isForceUser;
|};

// example from: https://medium.com/poka-techblog/simplify-your-javascript-use-map-reduce-and-filter-bd02c593cc2d
function testIterableOpChain2() returns int {
    Person[] personnel = [
      {
        id: 5,
        name: "Luke Skywalker",
        pilotingScore: 98,
        shootingScore: 56,
        isForceUser: true
      },
      {
        id: 82,
        name: "Sabine Wren",
        pilotingScore: 73,
        shootingScore: 99,
        isForceUser: false
      },
      {
        id: 22,
        name: "Zeb Orellios",
        pilotingScore: 20,
        shootingScore: 59,
        isForceUser: false
      },
      {
        id: 15,
        name: "Ezra Bridger",
        pilotingScore: 43,
        shootingScore: 67,
        isForceUser: true
      },
      {
        id: 11,
        name: "Caleb Dume",
        pilotingScore: 71,
        shootingScore: 85,
        isForceUser: true
      }
    ];

    int totalJediScore = personnel.filter(function (Person p) returns boolean {
        return p.isForceUser;
    }).map(function (Person jedi) returns int {
        return jedi.pilotingScore + jedi.shootingScore;
    }).reduce(function (int accum, int val) returns int {
        return accum + val;
    }, 0);

    return totalJediScore;
}

function testForEach() returns string {
    string[] days = ["Sun", "Mon", "Tues"];
    string result = "";

    foreach var day in days {
        result += day;
    }

    return result;
}

function testSetLength(int newLength) returns [int, int[], int[]] {
    int[] ar = [1, 2, 3, 4, 5, 6, 7];
    ar.setLength(newLength);
    int [] ar2 = ar.clone();
    ar2.setLength(newLength+1);
    return [ar.length(), ar, ar2];
}

function testShift() returns [int[], int] {
    int[] s = [1, 2, 3, 4, 5];
    var e = s.shift();
    return [s, e];
}

function testUnshift() returns int[] {
    int[] s = [1, 2, 3, 4, 5];
    s.unshift(8, 8);
    return s;
}

class Obj {
    int i;
    int j;
    function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

function testUnshiftTypeWithoutFillerValues () returns Obj[] {
    Obj[] arr = [];
    arr.unshift(new Obj(1, 1), new Obj(1,2));
    return arr;
}

function testRemoveAll() returns int[] {
    int[] ar = [1, 2, 3, 4, 5, 6, 7];
    ar.removeAll();
    return ar;
}

function testRemoveAllFixedLengthArray() returns int[] {
    int[7] ar = [1, 2, 3, 4, 5, 6, 7];
    ar.removeAll();
    return ar;
}

function testTupleResize() returns [int, string] {
    [int, string] t = [1, "hello"];
    t.setLength(3);
    return t;
}

function testTupleRemoveAll() returns [int, string] {
    [int, string] t = [1, "hello"];
    t.removeAll();
    return t;
}

function testTupleRemoveAllForTupleWithRestMemberType() returns [int, string] {
    [int, string, boolean...] t = [1, "hello", true];
    t.removeAll();
    return t;
}

function testTupleRemoveAllForTupleWithJustRestMemberType() returns boolean {
    [int...] t = [1, 2, 3];
    t.removeAll();
    return t.length() == 0;
}

function testTupleSetLengthLegal() returns boolean {
    [int, int, int...] t = [1, 2, 3, 4];
    t.setLength(2);
    return t.length() == 2;
}

function testTupleSetLengthIllegal() returns boolean {
    [int, int, int...] t = [1, 2, 3, 4];
    t.setLength(1);
    return t.length() == 1;
}

function testTupleSetLengthToSameAsOriginal() returns boolean {
    [int, int] t = [1, 2];
    t.setLength(2);
    return t.length() == 2;
}

function testPush() {
    testBooleanPush();
    testBytePush();
    testUnionArrayPush();
    testPushOnUnionOfSameBasicType();
    testInvalidPushOnUnionOfSameBasicType();
}

function testBooleanPush() {
    boolean[] arr = [false, true];
    boolean b = false;

    boolean[] moreBooleans = [true, true];

    arr.push(b);
    arr.push(false, false);
    arr.push(...moreBooleans);

    array:push(arr, true);
    array:push(arr, true, false);
    array:push(arr, ...moreBooleans);

    assertValueEquality(12, arr.length());

    assertFalse(arr[0]);
    assertTrue(arr[1]);
    assertFalse(arr[2]);
    assertFalse(arr[3]);
    assertFalse(arr[4]);
    assertTrue(arr[5]);
    assertTrue(arr[6]);
    assertTrue(arr[7]);
    assertTrue(arr[8]);
    assertFalse(arr[9]);
    assertTrue(arr[10]);
    assertTrue(arr[11]);
}

function testBytePush() {
    byte[] arr = [1, 2];
    byte b = 3;

    byte[] moreBytes = [0, 3, 254];

    arr.push(b);
    arr.push(255, 254);
    arr.push(...moreBytes);

    array:push(arr, 65);
    array:push(arr, 66, 67);
    array:push(arr, ...moreBytes);

    assertValueEquality(14, arr.length());

    assertValueEquality(1, arr[0]);
    assertValueEquality(2, arr[1]);
    assertValueEquality(3, arr[2]);
    assertValueEquality(<byte> 255, arr[3]);
    assertValueEquality(254, arr[4]);
    assertValueEquality(0, arr[5]);
    assertValueEquality(3, arr[6]);
    assertValueEquality(254, arr[7]);
    assertValueEquality(65, arr[8]);
    assertValueEquality(66, arr[9]);
    assertValueEquality(67, arr[10]);
    assertValueEquality(0, arr[11]);
    assertValueEquality(3, arr[12]);
    assertValueEquality(254, arr[13]);
}

function testUnionArrayPush() {
    (Foo|Bar)[] arr = [{s: "a"}];

    arr.push({s: "b"}, {i: 1});

    (Foo|Bar)[] more = [{i: 2}, {s: "c"}];
    array:push(arr, ...more);

    assertValueEquality(5, arr.length());

    Foo|Bar val = arr[0];
    assertTrue(val is Foo && val.s == "a");

    val = arr[1];
    assertTrue(val is Foo && val.s == "b");

    val = arr[2];
    assertTrue(val is Bar && val.i == 1);

    val = arr[3];
    assertTrue(val is Bar && val.i == 2);

    val = arr[4];
    assertTrue(val is Foo && val.s == "c");
}

type Foo record {
    string s;
};

type Bar record{
    int i;
};

function testPushOnUnionOfSameBasicType() {
    int[2]|int[] arr = [1, 7, 3];
    arr.push(99);
    'array:push(arr, 100, 101);

    int[] moreInts = [999, 998];
    arr.push(...moreInts);
    'array:push(arr, ...moreInts);

    assertValueEquality(10, arr.length());

    assertValueEquality(1, arr[0]);
    assertValueEquality(7, arr[1]);
    assertValueEquality(3, arr[2]);
    assertValueEquality(99, arr[3]);
    assertValueEquality(100, arr[4]);
    assertValueEquality(101, arr[5]);
    assertValueEquality(999, arr[6]);
    assertValueEquality(998, arr[7]);
    assertValueEquality(999, arr[8]);
    assertValueEquality(998, arr[9]);
}

function testInvalidPushOnUnionOfSameBasicType() {
    int[]|string[] arr = [1, 2];

    var fn = function () {
        arr.push("foo");
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertValueEquality("{ballerina/lang.array}InherentTypeViolation", err.message());
    assertValueEquality("incompatible types: expected 'int', found 'string'", err.detail()["message"].toString());

    fn = function () {
        arr.unshift("foo");
    };

    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    assertValueEquality("{ballerina/lang.array}InherentTypeViolation", err.message());
    assertValueEquality("incompatible types: expected 'int', found 'string'", err.detail()["message"].toString());
}

function testShiftOperation() {
    testShiftOnTupleWithoutValuesForRestParameter();
}

function testShiftOnTupleWithoutValuesForRestParameter() {
    [int, int...] intTupleWithRest = [0];

    var fn = function () {
        var x = intTupleWithRest.shift();
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertValueEquality("{ballerina/lang.array}OperationNotSupported", err.message());
    assertValueEquality("shift() not supported on type 'null'", err.detail()["message"].toString());
}

type Student record {|
   int id;
   string? fname;
   float? fee;
   decimal impact;
   boolean isUndergrad;
|};

function getStudentList() returns Student[] {
    Student s1 = {id: 1, fname: "Amber", fee: 10000.56, impact: 0.127, isUndergrad: true};
    Student s2 = {id: 20, fname: (), fee: 2000.56, impact: 0.45, isUndergrad: false};
    Student s3 = {id: 2, fname: "Dan", fee: (), impact: 0.3, isUndergrad: true};
    Student s4 = {id: 10, fname: "Kate", fee: (0.0/0.0), impact: 0.146, isUndergrad: false};
    Student s5 = {id: 3, fname: "Kate", fee: 5000.56, impact: 0.4, isUndergrad: false};

    Student[] studentList = [s1, s2, s3, s4, s5];

    return studentList;
}

function testSort1() {
    Student[] studentArr = getStudentList();

    Student[] sortedArr = studentArr.sort(array:DESCENDING, isolated function(Student s) returns int {
        return s.id;
    });

    assertValueEquality(sortedArr[0].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(sortedArr[1].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr[2].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr[3].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(sortedArr[4].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(studentArr, sortedArr);

    Student[] sortedArr2 = studentArr.sort(array:DESCENDING, isolated function(Student s) returns string? {
        return s.fname;
    });

    assertValueEquality(sortedArr2[0].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr2[1].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr2[2].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(sortedArr2[3].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr2[4].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(studentArr, sortedArr2);

    Student[] sortedArr3 = studentArr.sort(array:ASCENDING, isolated function(Student s) returns float? {
        return s.fee;
    });

    assertValueEquality(sortedArr3[0].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(sortedArr3[1].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr3[2].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr3[3].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr3[4].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(studentArr, sortedArr3);

    Student[] sortedArr4 = studentArr.sort(array:ASCENDING, isolated function(Student s) returns decimal {
        return s.impact;
    });

    assertValueEquality(sortedArr4[0].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr4[1].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr4[2].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(sortedArr4[3].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr4[4].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(studentArr, sortedArr4);

    Student[] sortedArr5 = studentArr.sort(array:ASCENDING, isolated function(Student s) returns boolean {
        return s.isUndergrad;
    });

    assertValueEquality(sortedArr5[0].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr5[1].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr5[2].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(sortedArr5[3].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr5[4].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(studentArr, sortedArr5);


}

function testSort2() {
    byte[] arr = base16 ` 5A B C3 4 `;

    byte[] sortedArr = arr.sort(array:DESCENDING, isolated function(byte b) returns byte {
        return b;
    });

    assertValueEquality(sortedArr[0], 188);
    assertValueEquality(sortedArr[1], 90);
    assertValueEquality(sortedArr[2], 52);
    assertValueEquality(arr, sortedArr);
}

function testSort3() returns int[] {
    int[] arr = [618917, 342612, 134235, 330412, 361634, 106132, 664844, 572601, 898935, 752462, 422849, 967630,
    261402, 947587, 818112, 225958, 625762, 979376, -374104, 194169, 306130, 930271, 579739, 4141, 391419, 529224,
    92583, 709992, 481213, 851703, 152557, 995605, 88360, 595013, 526619, 497868, -246544, 17351, 601903, 634524,
    959892, 569029, 924409, 735469, -561796, 548484, 741307, 451201, 309875, 229568, 808232, 420862, 729149, 958388,
    228636, 834740, -147418, 756897, 872064, 670287, 487870, 984526, 352034, 868342, 705354, 21468, 101992, 716704,
    842303, 463375, 796488, -45917, 74477, 111826, 205038, 267499, 381564, 311396, 627858, 898090, 66917, 119980,
    601003, 962077, 757150, 636247, 965398, 993533, 780387, 797889, 384359, -80982, 817361, 117263, 819125, 162680,
    374341, 297625, 89008, 564847];

    int[] sorted = arr.sort(array:DESCENDING, isolated function (int x) returns int {
        return x;
    });

    return sorted;
}

function testSort4() {
    [Grade, int][] grades = [["A+", 2], ["A-", 3], ["B", 3], ["C", 2]];

    [Grade, int][] sortedArr = grades.sort(array:ASCENDING, isolated function([Grade, int] val) returns float[] {
        if (val[0] == "A+") {
            return [<float>val[1], 6.5];
        }
        return [<float>val[1], 5.2];
    });

    assertValueEquality(sortedArr[0].toString(), "C 2");
    assertValueEquality(sortedArr[1].toString(), "A+ 2");
    assertValueEquality(sortedArr[2].toString(), "A- 3");
    assertValueEquality(sortedArr[3].toString(), "B 3");
    assertValueEquality(grades, sortedArr);
}

function testSort5() {
    Student[] studentArr = getStudentList();

    Student[] sortedArr = studentArr.sort(array:DESCENDING, isolated function(Student s) returns string? {
        return getFullName(s.id, s.fname);
    });

    assertValueEquality(sortedArr[0].toString(),
    "{\"id\":3,\"fname\":\"Kate\",\"fee\":5000.56,\"impact\":0.4,\"isUndergrad\":false}");
    assertValueEquality(sortedArr[1].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr[2].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(sortedArr[3].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr[4].toString(),
    "{\"id\":20,\"fname\":null,\"fee\":2000.56,\"impact\":0.45,\"isUndergrad\":false}");
    assertValueEquality(studentArr, sortedArr);
}

isolated function getFullName(int id, string? name) returns string? {
    if (name is string) {
        if (id == 10) {
            return name + " Middleton";
        }
        return name + "Rogers";
    }
    return name;
}

type StringOrStudent string|Student;

function testSort6() {
    anydata[] arr = [90, 2.0, 1, true, 32, "AA", 12.09, 100, 3, <map<string>>{"k":"Bar"}, ["BB", true]];

    anydata[] sortedArr = arr.sort(array:ASCENDING, isolated function(anydata a) returns int? {
        if (a is int) {
            return a;
        } else if (a is float) {
            return <int>a;
        } else if (a is boolean) {
            return 0;
        } else if (a is map<string>) {
            return -1;
        }
        return ();
    });

    assertValueEquality(sortedArr.toString(),
    "[{\"k\":\"Bar\"},true,1,2.0,3,12.09,32,90,100,\"AA\",[\"BB\",true]]");
    assertValueEquality(arr, sortedArr);

    string?[] arr2 = ["Hello", "World!", (), "from", "Ballerina"];

    string?[] sortedArr2 = arr2.sort();
    assertValueEquality(sortedArr2.toString(), "[\"Ballerina\",\"Hello\",\"World!\",\"from\",null]");

    Obj obj1 = new Obj(1, 1);
    Obj obj2 = new Obj(1,2);
    Obj obj3 = new Obj(1,10);
    Obj[] arr3 = [obj1, obj2, obj3];

    Obj[] sortedArr3 = arr3.sort(array:DESCENDING, isolated function(Obj obj) returns int {
        return obj.j;
    });

    assertValueEquality(sortedArr3[0].j, 10);
    assertValueEquality(sortedArr3[1].j, 2);
    assertValueEquality(sortedArr3[2].j, 1);

    int[2]|int[] arr4 = [1, 9, 3, 21, 0, 7];

    int[2]|int[] sortedArr4 = arr4.sort(array:ASCENDING, isolated function(int i) returns int {
        return i;
    });

    assertValueEquality(sortedArr4.toString(), "[0,1,3,7,9,21]");

    int[] arr5 = [2, 0, 12, 1, 23, 3, 100, 55];

    int[] sortedArr5 = arr5.sort(array:DESCENDING);
    assertValueEquality(sortedArr5.toString(), "[100,55,23,12,3,2,1,0]");

    string?[] sortedArr6 = arr2.sort(array:DESCENDING, isolated function(string? s) returns string?[]? {
        if (s is string) {
            return [s, "A"];
        }
        return ();
    });

    assertValueEquality(sortedArr6.toString(), "[\"from\",\"World!\",\"Hello\",\"Ballerina\",null]");

    string?[] sortedArr7 = arr2.sort(array:ASCENDING, isolated function(string? s) returns string?[] {
        if (s is string) {
            return [s, "A"];
        }
        return ["W", "A"];
    });

    assertValueEquality(sortedArr7.toString(), "[\"Ballerina\",\"Hello\",null,\"World!\",\"from\"]");

    int[] sortedArr8 = arr5.sort(array:ASCENDING, ());
    assertValueEquality(sortedArr8.toString(), "[0,1,2,3,12,23,55,100]");

    Grade[] arr6 = ["A+", "B+", "C", "F", "A-", "C", "A+", "B"];

    Grade[] sortedArr9 = arr6.sort(array:DESCENDING, isolated function(Grade grade) returns string {
        return grade;
    });

    assertValueEquality(sortedArr9.toString(), "[\"F\",\"C\",\"C\",\"B+\",\"B\",\"A-\",\"A+\",\"A+\"]");
    assertValueEquality(sortedArr9, arr6);

    Student s1 = {id: 1, fname: "Amber", fee: 10000.56, impact: 0.127, isUndergrad: true};
    Student s2 = {id: 2, fname: "Dan", fee: (), impact: 0.3, isUndergrad: true};
    Student s3 = {id: 10, fname: "Kate", fee: (0.0/0.0), impact: 0.146, isUndergrad: false};

    StringOrStudent[] arr7 = ["Anne", s3, s1, "James", "Frank", s2];

    StringOrStudent[] sortedArr10 = arr7.sort(array:ASCENDING, isolated function(StringOrStudent sp) returns string? {
        if (sp is Student) {
            return sp.fname;
        } else {
            return sp;
        }
    });

    assertValueEquality(sortedArr10[0].toString(),
    "{\"id\":1,\"fname\":\"Amber\",\"fee\":10000.56,\"impact\":0.127,\"isUndergrad\":true}");
    assertValueEquality(sortedArr10[1].toString(), "Anne");
    assertValueEquality(sortedArr10[2].toString(),
    "{\"id\":2,\"fname\":\"Dan\",\"fee\":null,\"impact\":0.3,\"isUndergrad\":true}");
    assertValueEquality(sortedArr10[3].toString(), "Frank");
    assertValueEquality(sortedArr10[4].toString(), "James");
    assertValueEquality(sortedArr10[5].toString(),
    "{\"id\":10,\"fname\":\"Kate\",\"fee\":NaN,\"impact\":0.146,\"isUndergrad\":false}");
    assertValueEquality(sortedArr10, arr7);

    int[] sortedArr11 = array:sort(arr5);
    assertValueEquality(sortedArr11.toString(), "[0,1,2,3,12,23,55,100]");
    assertValueEquality(sortedArr11, arr5);

    int[2]|int[] sortedArr12 = array:sort(arr4, array:DESCENDING, isolated function(int i) returns int {
        return i;
    });

    assertValueEquality(sortedArr12.toString(), "[21,9,7,3,1,0]");
    assertValueEquality(sortedArr12, arr4);

    string?[] sortedArr13 = array:sort(arr2, array:DESCENDING);
    assertValueEquality(sortedArr13.toString(), "[\"from\",\"World!\",\"Hello\",\"Ballerina\",null]");
}

function testSort7() {
    float?[][] arr= [[1.8, 2.89, 5, 70, 90], [(), 4],[1.8, 2.89, 5, 70], [(0.0/0.0), 9, 8, 10], [1.8, 2.89, 5, 70, ()
    ],[1.8, 2.89, 5],
    [1.8, 2.89, 4], [1.8, 2.89], [2.8, 2.89, 5, 70, 90],[3], [1.8, 2.89, 5, 70, (0.0/0.0)],[1.8, (0.0/0.0)]];

    float?[][] sortedArr = arr.sort(array:DESCENDING);

    assertValueEquality(sortedArr[0], [3.0]);
    assertValueEquality(sortedArr[1], [2.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr[2], [1.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr[3], [1.8, 2.89, 5.0, 70.0, (0.0/0.0)]);
    assertValueEquality(sortedArr[4], [1.8, 2.89, 5.0, 70.0, ()]);
    assertValueEquality(sortedArr[5], [1.8, 2.89, 5.0, 70.0]);
    assertValueEquality(sortedArr[6], [1.8, 2.89, 5.0]);
    assertValueEquality(sortedArr[7], [1.8, 2.89, 4.0]);
    assertValueEquality(sortedArr[8], [1.8, 2.89]);
    assertValueEquality(sortedArr[9], [1.8, (0.0/0.0)]);
    assertValueEquality(sortedArr[10], [(0.0/0.0), 9.0, 8.0, 10.0]);
    assertValueEquality(sortedArr[11], [(), 4.0]);
    assertValueEquality(sortedArr, arr);

    float?[][] sortedArr2 = arr.sort(array:ASCENDING, isolated function(float?[] x) returns float?[] {
        return x;
    });

    assertValueEquality(sortedArr2[0], [1.8, 2.89]);
    assertValueEquality(sortedArr2[1], [1.8, 2.89, 4.0]);
    assertValueEquality(sortedArr2[2], [1.8, 2.89, 5.0]);
    assertValueEquality(sortedArr2[3], [1.8, 2.89, 5.0, 70.0]);
    assertValueEquality(sortedArr2[4], [1.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr2[5], [1.8, 2.89, 5.0, 70.0, (0.0/0.0)]);
    assertValueEquality(sortedArr2[6], [1.8, 2.89, 5.0, 70.0, ()]);
    assertValueEquality(sortedArr2[7], [1.8, (0.0/0.0)]);
    assertValueEquality(sortedArr2[8], [2.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr2[9], [3.0]);
    assertValueEquality(sortedArr2[10], [(0.0/0.0), 9.0, 8.0, 10.0]);
    assertValueEquality(sortedArr2[11], [(), 4.0]);
    assertValueEquality(sortedArr2, arr);

    int[][] arr2 = [[1, 9, 2],[0, 9, 1], [1, 7, 5], [9, 8, 2], [9, 8, 1]];

    int[][] sortedArr3 = arr2.sort();

    assertValueEquality(sortedArr3[0], [0, 9, 1]);
    assertValueEquality(sortedArr3[1], [1, 7, 5]);
    assertValueEquality(sortedArr3[2], [1, 9, 2]);
    assertValueEquality(sortedArr3[3], [9, 8, 1]);
    assertValueEquality(sortedArr3[4], [9, 8, 2]);
    assertValueEquality(sortedArr3, arr2);
}

function testSort8() {
    [int...][] arr = [[10, 2, 0], [1, 2, 0, 7], [1, 2, 5], [0, 9, 5], [1, 2, 0], [1, 1, 0]];

    [int...][] sortedArr = arr.sort(array:ASCENDING, isolated function ([int...] x) returns int[] {
            return x;
    });

    assertValueEquality(sortedArr[0], [0, 9, 5]);
    assertValueEquality(sortedArr[1], [1, 1, 0]);
    assertValueEquality(sortedArr[2], [1, 2, 0]);
    assertValueEquality(sortedArr[3], [1, 2, 0, 7]);
    assertValueEquality(sortedArr[4], [1, 2, 5]);
    assertValueEquality(sortedArr[5], [10, 2, 0]);
    assertValueEquality(sortedArr, arr);

    [int?...][] arr2 = [[(), 2, 0], [1, 2, 0, 7], [1, 2, 5], [0, 9, 5], [1, 2, 0], [1, 1, 0], [0, (), 9]];

    [int?...][] sortedArr2 = arr2.sort(array:DESCENDING, isolated function (int?[] x) returns int?[] {
        return x;
    });

    assertValueEquality(sortedArr2[0], [1, 2, 5]);
    assertValueEquality(sortedArr2[1], [1, 2, 0, 7]);
    assertValueEquality(sortedArr2[2], [1, 2, 0]);
    assertValueEquality(sortedArr2[3], [1, 1, 0]);
    assertValueEquality(sortedArr2[4], [0, 9, 5]);
    assertValueEquality(sortedArr2[5], [0, (), 9]);
    assertValueEquality(sortedArr2[6], [(), 2, 0]);
    assertValueEquality(sortedArr2, arr2);

    [int, boolean...][] arr3 = [[3, true, true, true], [5, true, false, true], [1, false, false]];

    [int, boolean...][] sortedArr3 = arr3.sort(array:ASCENDING, isolated function([int, boolean...] x) returns boolean[] {
        return [x[1], x[2]];
    });

    assertValueEquality(sortedArr3[0], [1, false, false]);
    assertValueEquality(sortedArr3[1], [5, true, false, true]);
    assertValueEquality(sortedArr3[2], [3, true, true, true]);
    assertValueEquality(sortedArr3, arr3);
}

function testSort9() {
    strings:Char[] arr = ["s", "a", "b", "M", "Z"];

    strings:Char[] sortedArr = arr.sort(array:DESCENDING);
    assertValueEquality(sortedArr.toString(), "[\"s\",\"b\",\"a\",\"Z\",\"M\"]");

    int[] arr2 = [4294967295, 4194967295, 4294967290, 4284967295, 3294967295, 1294967295];

    int[] sortedArr2 = arr2.sort();
    assertValueEquality(sortedArr2.toString(),
    "[1294967295,3294967295,4194967295,4284967295,4294967290,4294967295]");

    ints:Signed32[] arr3 = [2147483647, -2147483648, 2147483637, -1147483648, -2137483648, 1147483647];

    ints:Signed32[] sortedArr3 = arr3.sort();

    assertValueEquality(sortedArr3[0], -2147483648);
    assertValueEquality(sortedArr3[1], -2137483648);
    assertValueEquality(sortedArr3[2], -1147483648);
    assertValueEquality(sortedArr3[3], 1147483647);
    assertValueEquality(sortedArr3[4], 2147483637);
    assertValueEquality(sortedArr3[5], 2147483647);
    assertValueEquality(sortedArr3, arr3);

    ints:Signed16[] arr4 = [32765, -32768, 32767, -32668, -30768, 32567];

    ints:Signed16[] sortedArr4 = arr4.sort(array:DESCENDING);

    assertValueEquality(sortedArr4[0], 32767);
    assertValueEquality(sortedArr4[1], 32765);
    assertValueEquality(sortedArr4[2], 32567);
    assertValueEquality(sortedArr4[3], -30768);
    assertValueEquality(sortedArr4[4], -32668);
    assertValueEquality(sortedArr4[5], -32768);
    assertValueEquality(sortedArr4, arr4);

    ints:Signed8[] arr5 = [-100, -123, 100, 67, -34, 52];

    ints:Signed8[] sortedArr5 = arr5.sort();

    assertValueEquality(sortedArr5[0], -123);
    assertValueEquality(sortedArr5[1], -100);
    assertValueEquality(sortedArr5[2], -34);
    assertValueEquality(sortedArr5[3], 52);
    assertValueEquality(sortedArr5[4], 67);
    assertValueEquality(sortedArr5[5], 100);
    assertValueEquality(sortedArr5, arr5);

    ints:Unsigned32[] arr6 = [50, 4294967295, 0, 4294957295, 4294967294, 123, 214967295];

    ints:Unsigned32[] sortedArr6 = arr6.sort(array:ASCENDING, isolated function(ints:Unsigned32 x) returns ints:Unsigned32 {
        return x;
    });

    assertValueEquality(sortedArr6[0], 0);
    assertValueEquality(sortedArr6[1], 50);
    assertValueEquality(sortedArr6[2], 123);
    assertValueEquality(sortedArr6[3], 214967295);
    assertValueEquality(sortedArr6[4], 4294957295);
    assertValueEquality(sortedArr6[5], 4294967294);
    assertValueEquality(sortedArr6[6], 4294967295);
    assertValueEquality(sortedArr6, arr6);

    ints:Unsigned16[] arr7 = [450, 65335, 0, 12, 65535, 12500, 4];

    ints:Unsigned16[] sortedArr7 = arr7.sort(array:DESCENDING);

    assertValueEquality(sortedArr7[0], 65535);
    assertValueEquality(sortedArr7[1], 65335);
    assertValueEquality(sortedArr7[2], 12500);
    assertValueEquality(sortedArr7[3], 450);
    assertValueEquality(sortedArr7[4], 12);
    assertValueEquality(sortedArr7[5], 4);
    assertValueEquality(sortedArr7[6], 0);
    assertValueEquality(sortedArr7, arr7);

    ints:Unsigned8[] arr8 = [221, 100, 0, 255, 24, 9, 2];

    ints:Unsigned8[] sortedArr8 = arr8.sort();

    assertValueEquality(sortedArr8[0], 0);
    assertValueEquality(sortedArr8[1], 2);
    assertValueEquality(sortedArr8[2], 9);
    assertValueEquality(sortedArr8[3], 24);
    assertValueEquality(sortedArr8[4], 100);
    assertValueEquality(sortedArr8[5], 221);
    assertValueEquality(sortedArr8[6], 255);
    assertValueEquality(sortedArr8, arr8);
}

function testSort10() {
    int[] arr = [10, 1, 3, 2, 0, 6];

    int[] sortedArr = arr.sort(array:ASCENDING, (i) => i);

    assertValueEquality(sortedArr.toString(), "[0,1,2,3,6,10]");
    assertValueEquality(sortedArr, arr);

    final int methodInt1 = 2;
    var addFunc1 = isolated function (int funcInt1) returns (int) {
        final int methodInt2 = 23;
        var addFunc2 = isolated function (int funcInt2) returns (int) {
            final int methodInt3 = 7;
            isolated function (int) returns (int) addFunc3 = funcInt3 => funcInt3 + methodInt1 + methodInt2 + methodInt3;
            return addFunc3(8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };

    int[] sortedArr2 = arr.sort(array:DESCENDING, addFunc1);

    assertValueEquality(sortedArr2.toString(), "[10,6,3,2,1,0]");
    assertValueEquality(sortedArr2, arr);

    int[] sortedArr3 = array:sort(arr, array:ASCENDING, isolated function(int x) returns string[] => [x.toString(), "World"]);

    assertValueEquality(sortedArr3.toString(), "[0,1,10,2,3,6]");
    assertValueEquality(sortedArr3, arr);

    int[] sortedArr4 = arr.sort(array:DESCENDING, (i) => i.toString());

    assertValueEquality(sortedArr4, [6,3,2,10,1,0]);
    assertValueEquality(sortedArr4, arr);

    int?[] arr2 = [(), 1, 3, 10, 0, 6];

    int?[] sortedArr5 = arr2.sort(array:DESCENDING, (i) => i);

    assertValueEquality(sortedArr5, [10,6,3,1,0,()]);
    assertValueEquality(sortedArr5, arr2);

    int[] arr3 = [];
    int[] sortedArr6 = arr3.sort(array:DESCENDING, (i) => i);

    assertValueEquality(sortedArr6, []);
    assertValueEquality(sortedArr6, arr3);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actual.toString () + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'false', found '" + actual.toString () + "'");
}


function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}


function testAsyncFpArgsWithArrays() returns [int, int[]] {
    int[] numbers = [-7, 2, -12, 4, 1];
    int count = 0;
    int[] filter = numbers.filter(function (int i) returns boolean {
        future<int> f1 = start getRandomNumber(i);
        int a = wait f1;
        return a >= 0;
    });
    filter.forEach(function (int i) {
        future<int> f1 = start getRandomNumber(i);
        int a = wait f1;
        filter[count] = i + 2;
        count = count + 1;
    });
    int reduce = filter.reduce(function (int total, int i) returns int {
        future<int> f1 = start getRandomNumber(i);
        int a = wait f1;
        return total + a;
    }, 0);
    return [reduce, filter];
}

function getRandomNumber(int i) returns int {
    return i + 2;
}
