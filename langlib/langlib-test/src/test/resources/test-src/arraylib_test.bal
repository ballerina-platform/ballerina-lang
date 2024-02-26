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
        public isolated function next() returns record {|string value;|}?;
    } itr = arr.iterator();

    record {|string value;|}|() elem = itr.next();
    string result = "";

    while (elem is record {|string value;|}) {
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
    int[] newArr = arr.'map(function(int x) returns int {
        return x / 10;
    });
    return newArr;
}

function testForeach() returns string {
    string?[] arr = ["Hello", "World!", (), "from", "Ballerina"];
    string result = "";

    arr.forEach(function(string? x) {
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

function testModificationAfterSliceOfReadonlyIntArray() {
    readonly & int[] a = [1, 2, 3, 4, 5];
    int[] b = a.slice(2, 4);
    b[0] = 7;
    assertValueEquality([7, 4], b);
}

function testModificationAfterSliceOfReadonlyStringArray() {
    readonly & string[] roNames = ["x"];
    string[] rwNames = roNames.slice(0);
    rwNames[0] = "y";
    assertValueEquality(["y"], rwNames);
}

function testModificationAfterSliceOfReadonlyBooleanArray() {
    readonly & boolean[] a = [true, false, true, true];
    boolean[] b = a.slice(2);
    b[1] = false;
    assertValueEquality([true, false], b);
}

function testModificationAfterSliceOfReadonlyByteArray() {
    readonly & byte[] a = [1, 2, 3];
    byte[] b = a.slice(1);
    b[1] = 4;
    assertValueEquality([2, 4], b);
}

function testModificationAfterSliceOfReadonlyFloatArray() {
    readonly & float[] f = [1.2, 3.4, 5, 7.3, 9.47];
    float[] g = f.slice(2, 4);
    g[2] = 6.78;
    assertValueEquality([5.0, 7.3, 6.78], g);
}

function testModificationAfterSliceOfReadonlyRecordArray() {
    readonly & Employee[] arr = [{name: "John Doe", age: 25, designation: "Software Engineer"}];
    Employee[] s = arr.slice(0);
    s[0] = {name: "Jane Doe", age: 27, designation: "UX Engineer"};
}

type Person2 record {
    int id;
    string name;
    int age;
};

type Student2 record {
    int id;
    string name;
    string school;
    float average;
};

function testSliceOfIntersectionOfReadonlyRecordArray() {
    readonly & (readonly & Person2 & Student2)[] ps = [{id: 16158, name: "Arun", age: 12, average: 89.9, school: "JHC"}];
    (readonly & Person2 & Student2)[] s = ps.slice(0);
    s[0] = {id: 175149, name: "Roy", age: 12, school: "RC", average: 84.9};
    assertValueEquality([{id: 175149, name: "Roy", age: 12, school: "RC", average: 84.9}], s);
}

function testPushAfterSlice() {
    float[] arr = [12.34, 23.45, 34.56, 45.67, 56.78];
    float[] s = arr.slice(1, 4);
    int sl = s.length();
    s.push(20.1);
    int slp = s.length();
    assertValueEquality(3, sl);
    assertValueEquality(4, slp);
    assertValueEquality([23.45, 34.56, 45.67, 20.1], s);
}

function testPushAfterSliceFixed() {
    int[5] arr = [1, 2, 3, 4, 5];
    int[] s = arr.slice(3);
    int sl = s.length();
    s.push(88);
    int slp = s.length();
    assertValueEquality(2, sl);
    assertValueEquality(3, slp);
    assertValueEquality([4, 5, 88], s);
}

function testPushAfterSliceOfReadonlyMapArray() {
    readonly & map<string>[] arr = [{x: "a"}, {y: "b"}];
    map<string>[] r = arr.slice(1);
    r.push({z: "c"});
}

function testPushAfterSliceOnTuple() {
    [int, string, string] x = [1, "hello", "world"];
    (int|string)[] a = x.slice(1);
    assertValueEquality(2, a.length());
    assertValueEquality("hello", a[0]);
    assertValueEquality("world", a[1]);
    a.push("new element");
    assertValueEquality(3, a.length());

    [int, int, boolean...] y = [1, 2, true, false, true];
    (int|boolean)[] b = y.slice(1, 4);
    assertValueEquality(3, b.length());
    assertValueEquality(2, b[0]);
    b.push(false);
    b.push(25);
    assertValueEquality(25, b.pop());
    assertFalse(b.pop());
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
    float avg = arr.reduce(function(float accum, int val) returns float {
        return accum + <float>val / <float>arr.length();
    }, 0.0);
    return avg;
}

type Grade "A+"|"A"|"A-"|"B+"|"B"|"B-"|"C"|"F";

function testIterableOpChain() returns float {
    [Grade, int][] grades = [["A+", 2], ["A-", 3], ["B", 3], ["C", 2]];

    int totalCredits = grades.reduce(function(int accum, [Grade, int] grade) returns int {
        return accum + grade[1];
    }, 0);

    float gpa = grades.'map(gradeToValue).reduce(function(float accum, [float, int] gradePoint) returns float {
        return accum + (gradePoint[0] * <float>gradePoint[1]) / <float>totalCredits;
    }, 0.0);

    return gpa;
}

function gradeToValue([Grade, int] grade) returns [float, int] {
    match grade[0] {
        "A+" => {
            return [4.2, grade[1]];
        }
        "A" => {
            return [4.0, grade[1]];
        }
        "A-" => {
            return [3.7, grade[1]];
        }
        "B+" => {
            return [3.3, grade[1]];
        }
        "B" => {
            return [3.0, grade[1]];
        }
        "B-" => {
            return [2.7, grade[1]];
        }
        "C" => {
            return [2.0, grade[1]];
        }
        "F" => {
            return [0.0, grade[1]];
        }
    }
    error e = error("Invalid grade: " + <string>grade[0]);
    panic e;
}

function testIndexOf() returns [int?, int?] {
    anydata[] arr = [10, "foo", 12.34, true, <map<string>>{"k": "Bar"}];
    map<string> m = {"k": "Bar"};
    int? i1 = arr.indexOf(m);
    int? i2 = arr.indexOf(50);
    return [i1, i2];
}

function testLastIndexOf() {
    anydata[] array = [
        10,
        10,
        10,
        "foo",
        "foo",
        "foo",
        12.34,
        12.34,
        true,
        true,
        <map<string>>{"k": "Bar"},
        <map<string>>{"k": "Bar"},
        [12, true],
        [12, true]
    ];
    map<string> m1 = {"k": "Bar"};
    map<string> m2 = {"k": "Foo"};
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

function testReverseInt() {
    int[] arr = [10, 20, 30, 40, 50];
    assertValueEquality(arr.reverse(), [50, 40, 30, 20, 10]);
}

function testReverseFloat() {
    float[] arr = [10.5, 20.6, 30.7, 40.8, 50.9];
    assertValueEquality(arr.reverse(), [50.9, 40.8, 30.7, 20.6, 10.5]);
}

function testReverseStr() {
    string[] arr = ["hello", "A", "Ballerina"];
    assertValueEquality(arr.reverse(), ["Ballerina", "A", "hello"]);
}

function testReverseBool() {
    boolean[] arr = [true, false, true, true, false];
    assertValueEquality(arr.reverse(), [false, true, true, false, true]);
}

function testReverseByte() {
    byte[] arr = [2, 4, 6, 8, 10];
    assertValueEquality(arr.reverse(), [10, 8, 6, 4, 2]);
}

function testReverseMap() {
    map<string>[] arr = [{line1: "a", line2: "b"}, {line3: "c", line4: "d"}];
    assertValueEquality(arr.reverse(), [{line3: "c", line4: "d"}, {line1: "a", line2: "b"}]);
}

type Employee record {
    string name;
    int age;
    string designation;
};

function testReverseRecord() {
    Employee[] arr = [
        {name: "John Doe", age: 25, designation: "Software Engineer"},
        {name: "Jane Doe", age: 27, designation: "UX Engineer"}
    ];
    assertValueEquality(arr.reverse(), [
        {name: "Jane Doe", age: 27, designation: "UX Engineer"},
        {name: "John Doe", age: 25, designation: "Software Engineer"}
    ]);
}

function testArrayReverseEquality() {
    int[] x = [1, 2, 3, 4, 5];
    int[] y = x.reverse();
    assertValueEquality(x == y, false);
    assertValueEquality(x === y, false);
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

    int totalJediScore = personnel.filter(function(Person p) returns boolean {
        return p.isForceUser;
    }).map(function(Person jedi) returns int {
        return jedi.pilotingScore + jedi.shootingScore;
    }).reduce(function(int accum, int val) returns int {
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
    int[] ar2 = ar.clone();
    ar2.setLength(newLength + 1);
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

function testUnshiftTypeWithoutFillerValues() returns Obj[] {
    Obj[] arr = [];
    arr.unshift(new Obj(1, 1), new Obj(1, 2));
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

function testTupleRemoveAllForTupleWithRestMemberType() returns [int, string, boolean...] {
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
    testPushWithObjectConstructorExpr();
    testPushWithRawTemplateExpr();
    testPushWithTableConstructorExpr();
    testPushWithNewExpr();
    testPushWithErrorConstructorExpr();
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
    assertValueEquality(<byte>255, arr[3]);
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

type Bar record {
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

    var fn = function() {
        arr.push("foo");
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error>res;

    var message = err.detail()["message"];
    string detailMessage = message is error ? message.toString() : message.toString();
    assertValueEquality("{ballerina/lang.array}InherentTypeViolation", err.message());
    assertValueEquality("incompatible types: expected 'int', found 'string'", detailMessage);

    fn = function() {
        arr.unshift("foo");
    };

    res = trap fn();
    assertTrue(res is error);

    err = <error>res;

    message = err.detail()["message"];
    detailMessage = message is error ? message.toString() : message.toString();
    assertValueEquality("{ballerina/lang.array}InherentTypeViolation", err.message());
    assertValueEquality("incompatible types: expected 'int', found 'string'", detailMessage);
}

function testShiftOperation() {
    testShiftOnTupleWithoutValuesForRestParameter();
}

function testShiftOnTupleWithoutValuesForRestParameter() {
    [int, int...] intTupleWithRest = [0];

    var fn = function() {
        var x = intTupleWithRest.shift();
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error>res;
    var message = err.detail()["message"];
    string detailMessage = message is error ? message.toString() : message.toString();
    assertValueEquality("{ballerina/lang.array}OperationNotSupported", err.message());
    assertValueEquality("shift() not supported on type '[int,int...]'", detailMessage);
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
    Student s4 = {id: 10, fname: "Kate", fee: (0.0 / 0.0), impact: 0.146, isUndergrad: false};
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
    assertFalse(studentArr == sortedArr);

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
    assertFalse(studentArr == sortedArr2);

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

    Student[] sortedArr5 = sortedArr4.sort(array:ASCENDING, isolated function(Student s) returns boolean {
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
    assertFalse(sortedArr4 == sortedArr5);

    assertValueEquality(getStudentList(), studentArr); // no change to original array
}

function testSort2() {
    byte[] arr = base16 ` 5A B C3 4 `;

    byte[] sortedArr = arr.sort(array:DESCENDING, isolated function(byte b) returns byte {
        return b;
    });

    assertValueEquality(sortedArr[0], 188);
    assertValueEquality(sortedArr[1], 90);
    assertValueEquality(sortedArr[2], 52);

    assertFalse(arr == sortedArr);
    assertValueEquality(base16 ` 5A B C3 4 `, arr); // no change to original array
}

function testSort3() returns int[] {
    int[] arr = [
        618917,
        342612,
        134235,
        330412,
        361634,
        106132,
        664844,
        572601,
        898935,
        752462,
        422849,
        967630,
        261402,
        947587,
        818112,
        225958,
        625762,
        979376,
        -374104,
        194169,
        306130,
        930271,
        579739,
        4141,
        391419,
        529224,
        92583,
        709992,
        481213,
        851703,
        152557,
        995605,
        88360,
        595013,
        526619,
        497868,
        -246544,
        17351,
        601903,
        634524,
        959892,
        569029,
        924409,
        735469,
        -561796,
        548484,
        741307,
        451201,
        309875,
        229568,
        808232,
        420862,
        729149,
        958388,
        228636,
        834740,
        -147418,
        756897,
        872064,
        670287,
        487870,
        984526,
        352034,
        868342,
        705354,
        21468,
        101992,
        716704,
        842303,
        463375,
        796488,
        -45917,
        74477,
        111826,
        205038,
        267499,
        381564,
        311396,
        627858,
        898090,
        66917,
        119980,
        601003,
        962077,
        757150,
        636247,
        965398,
        993533,
        780387,
        797889,
        384359,
        -80982,
        817361,
        117263,
        819125,
        162680,
        374341,
        297625,
        89008,
        564847
    ];

    int[] sorted = arr.sort(array:DESCENDING, isolated function(int x) returns int {
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

    assertValueEquality(sortedArr[0].toString(), "[\"C\",2]");
    assertValueEquality(sortedArr[1].toString(), "[\"A+\",2]");
    assertValueEquality(sortedArr[2].toString(), "[\"A-\",3]");
    assertValueEquality(sortedArr[3].toString(), "[\"B\",3]");
    assertValueEquality([["A+", 2], ["A-", 3], ["B", 3], ["C", 2]], grades); // no change to original array
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
    assertValueEquality(getStudentList(), studentArr); // no change to original array
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
    anydata[] arr = [90, 2.0, 1, true, 32, "AA", 12.09, 100, 3, <map<string>>{"k": "Bar"}, ["BB", true]];

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
    assertValueEquality([90, 2.0, 1, true, 32, "AA", 12.09, 100, 3, <map<string>>{"k": "Bar"}, ["BB", true]], arr);

    string?[] arr2 = ["Hello", "World!", (), "from", "Ballerina"];

    string?[] sortedArr2 = arr2.sort();
    assertValueEquality(sortedArr2.toString(), "[\"Ballerina\",\"Hello\",\"World!\",\"from\",null]");

    Obj obj1 = new Obj(1, 1);
    Obj obj2 = new Obj(1, 2);
    Obj obj3 = new Obj(1, 10);
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
    assertValueEquality(["A+", "B+", "C", "F", "A-", "C", "A+", "B"], arr6); // no change to arr6

    Student s1 = {id: 1, fname: "Amber", fee: 10000.56, impact: 0.127, isUndergrad: true};
    Student s2 = {id: 2, fname: "Dan", fee: (), impact: 0.3, isUndergrad: true};
    Student s3 = {id: 10, fname: "Kate", fee: (0.0 / 0.0), impact: 0.146, isUndergrad: false};

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
    assertValueEquality(["Anne", s3, s1, "James", "Frank", s2], arr7); // no change to arr7

    int[] sortedArr11 = array:sort(arr5);
    assertValueEquality(sortedArr11.toString(), "[0,1,2,3,12,23,55,100]");
    assertValueEquality([2, 0, 12, 1, 23, 3, 100, 55], arr5); // no change to arr5
    assertValueEquality([0, 1, 2, 3, 12, 23, 55, 100], sortedArr8); // no change to sortedArr8

    int[2]|int[] sortedArr12 = array:sort(arr4, array:DESCENDING, isolated function(int i) returns int {
        return i;
    });

    assertValueEquality(sortedArr12.toString(), "[21,9,7,3,1,0]");
    assertValueEquality([1, 9, 3, 21, 0, 7], arr4); // no change to arr4

    string?[] sortedArr13 = array:sort(arr2, array:DESCENDING);
    assertValueEquality(sortedArr13.toString(), "[\"from\",\"World!\",\"Hello\",\"Ballerina\",null]");
}

function testSort7() {
    float?[][] arr = [
        [1.8, 2.89, 5, 70, 90],
        [(), 4],
        [1.8, 2.89, 5, 70],
        [(0.0 / 0.0), 9, 8, 10],
        [
            1.8,
            2.89,
            5,
            70,
            ()
        ],
        [1.8, 2.89, 5],
        [1.8, 2.89, 4],
        [1.8, 2.89],
        [2.8, 2.89, 5, 70, 90],
        [3],
        [1.8, 2.89, 5, 70, (0.0 / 0.0)],
        [1.8, (0.0 / 0.0)]
    ];

    float?[][] sortedArr = arr.sort(array:DESCENDING);

    assertValueEquality(sortedArr[0], [3.0]);
    assertValueEquality(sortedArr[1], [2.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr[2], [1.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr[3], [1.8, 2.89, 5.0, 70.0, (0.0 / 0.0)]);
    assertValueEquality(sortedArr[4], [1.8, 2.89, 5.0, 70.0, ()]);
    assertValueEquality(sortedArr[5], [1.8, 2.89, 5.0, 70.0]);
    assertValueEquality(sortedArr[6], [1.8, 2.89, 5.0]);
    assertValueEquality(sortedArr[7], [1.8, 2.89, 4.0]);
    assertValueEquality(sortedArr[8], [1.8, 2.89]);
    assertValueEquality(sortedArr[9], [1.8, (0.0 / 0.0)]);
    assertValueEquality(sortedArr[10], [(0.0 / 0.0), 9.0, 8.0, 10.0]);
    assertValueEquality(sortedArr[11], [(), 4.0]);

    float?[][] sortedArr2 = arr.sort(array:ASCENDING, isolated function(float?[] x) returns float?[] {
        return x;
    });

    assertValueEquality(sortedArr2[0], [1.8, 2.89]);
    assertValueEquality(sortedArr2[1], [1.8, 2.89, 4.0]);
    assertValueEquality(sortedArr2[2], [1.8, 2.89, 5.0]);
    assertValueEquality(sortedArr2[3], [1.8, 2.89, 5.0, 70.0]);
    assertValueEquality(sortedArr2[4], [1.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr2[5], [1.8, 2.89, 5.0, 70.0, (0.0 / 0.0)]);
    assertValueEquality(sortedArr2[6], [1.8, 2.89, 5.0, 70.0, ()]);
    assertValueEquality(sortedArr2[7], [1.8, (0.0 / 0.0)]);
    assertValueEquality(sortedArr2[8], [2.8, 2.89, 5.0, 70.0, 90.0]);
    assertValueEquality(sortedArr2[9], [3.0]);
    assertValueEquality(sortedArr2[10], [(0.0 / 0.0), 9.0, 8.0, 10.0]);
    assertValueEquality(sortedArr2[11], [(), 4.0]);

    int[][] arr2 = [[1, 9, 2], [0, 9, 1], [1, 7, 5], [9, 8, 2], [9, 8, 1]];

    int[][] sortedArr3 = arr2.sort();

    assertValueEquality(sortedArr3[0], [0, 9, 1]);
    assertValueEquality(sortedArr3[1], [1, 7, 5]);
    assertValueEquality(sortedArr3[2], [1, 9, 2]);
    assertValueEquality(sortedArr3[3], [9, 8, 1]);
    assertValueEquality(sortedArr3[4], [9, 8, 2]);
}

function testSort8() {
    [int...][] arr = [[10, 2, 0], [1, 2, 0, 7], [1, 2, 5], [0, 9, 5], [1, 2, 0], [1, 1, 0]];

    [int...][] sortedArr = arr.sort(array:ASCENDING, isolated function([int...] x) returns int[] {
        return x;
    });

    assertValueEquality(sortedArr[0], [0, 9, 5]);
    assertValueEquality(sortedArr[1], [1, 1, 0]);
    assertValueEquality(sortedArr[2], [1, 2, 0]);
    assertValueEquality(sortedArr[3], [1, 2, 0, 7]);
    assertValueEquality(sortedArr[4], [1, 2, 5]);
    assertValueEquality(sortedArr[5], [10, 2, 0]);

    [int?...][] arr2 = [[(), 2, 0], [1, 2, 0, 7], [1, 2, 5], [0, 9, 5], [1, 2, 0], [1, 1, 0], [0, (), 9]];

    [int?...][] sortedArr2 = arr2.sort(array:DESCENDING, isolated function(int?[] x) returns int?[] {
        return x;
    });

    assertValueEquality(sortedArr2[0], [1, 2, 5]);
    assertValueEquality(sortedArr2[1], [1, 2, 0, 7]);
    assertValueEquality(sortedArr2[2], [1, 2, 0]);
    assertValueEquality(sortedArr2[3], [1, 1, 0]);
    assertValueEquality(sortedArr2[4], [0, 9, 5]);
    assertValueEquality(sortedArr2[5], [0, (), 9]);
    assertValueEquality(sortedArr2[6], [(), 2, 0]);

    [int, boolean...][] arr3 = [[3, true, true, true], [5, true, false, true], [1, false, false]];

    [int, boolean...][] sortedArr3 = arr3.sort(array:ASCENDING, isolated function([int, boolean...] x) returns boolean[] {
        return [x[1], x[2]];
    });

    assertValueEquality(sortedArr3[0], [1, false, false]);
    assertValueEquality(sortedArr3[1], [5, true, false, true]);
    assertValueEquality(sortedArr3[2], [3, true, true, true]);
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

    ints:Signed16[] arr4 = [32765, -32768, 32767, -32668, -30768, 32567];

    ints:Signed16[] sortedArr4 = arr4.sort(array:DESCENDING);

    assertValueEquality(sortedArr4[0], 32767);
    assertValueEquality(sortedArr4[1], 32765);
    assertValueEquality(sortedArr4[2], 32567);
    assertValueEquality(sortedArr4[3], -30768);
    assertValueEquality(sortedArr4[4], -32668);
    assertValueEquality(sortedArr4[5], -32768);

    ints:Signed8[] arr5 = [-100, -123, 100, 67, -34, 52];

    ints:Signed8[] sortedArr5 = arr5.sort();

    assertValueEquality(sortedArr5[0], -123);
    assertValueEquality(sortedArr5[1], -100);
    assertValueEquality(sortedArr5[2], -34);
    assertValueEquality(sortedArr5[3], 52);
    assertValueEquality(sortedArr5[4], 67);
    assertValueEquality(sortedArr5[5], 100);

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

    ints:Unsigned16[] arr7 = [450, 65335, 0, 12, 65535, 12500, 4];

    ints:Unsigned16[] sortedArr7 = arr7.sort(array:DESCENDING);

    assertValueEquality(sortedArr7[0], 65535);
    assertValueEquality(sortedArr7[1], 65335);
    assertValueEquality(sortedArr7[2], 12500);
    assertValueEquality(sortedArr7[3], 450);
    assertValueEquality(sortedArr7[4], 12);
    assertValueEquality(sortedArr7[5], 4);
    assertValueEquality(sortedArr7[6], 0);

    ints:Unsigned8[] arr8 = [221, 100, 0, 255, 24, 9, 2];

    ints:Unsigned8[] sortedArr8 = arr8.sort();

    assertValueEquality(sortedArr8[0], 0);
    assertValueEquality(sortedArr8[1], 2);
    assertValueEquality(sortedArr8[2], 9);
    assertValueEquality(sortedArr8[3], 24);
    assertValueEquality(sortedArr8[4], 100);
    assertValueEquality(sortedArr8[5], 221);
    assertValueEquality(sortedArr8[6], 255);
}

function testSort10() {
    int[] arr = [10, 1, 3, 2, 0, 6];

    int[] sortedArr = arr.sort(array:ASCENDING, (i) => i);

    assertValueEquality(sortedArr.toString(), "[0,1,2,3,6,10]");

    final int methodInt1 = 2;
    var addFunc1 = isolated function(int funcInt1) returns (int) {
        final int methodInt2 = 23;
        var addFunc2 = isolated function(int funcInt2) returns (int) {
            final int methodInt3 = 7;
            isolated function (int) returns (int) addFunc3 = funcInt3 => funcInt3 + methodInt1 + methodInt2 + methodInt3;
            return addFunc3(8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };

    int[] sortedArr2 = arr.sort(array:DESCENDING, addFunc1);

    assertValueEquality(sortedArr2.toString(), "[10,6,3,2,1,0]");

    int[] sortedArr3 = array:sort(arr, array:ASCENDING, isolated function(int x) returns string[] => [x.toString(), "World"]);

    assertValueEquality(sortedArr3.toString(), "[0,1,10,2,3,6]");

    int[] sortedArr4 = arr.sort(array:DESCENDING, (i) => i.toString());

    assertValueEquality(sortedArr4, [6, 3, 2, 10, 1, 0]);

    int?[] arr2 = [(), 1, 3, 10, 0, 6];

    int?[] sortedArr5 = arr2.sort(array:DESCENDING, (i) => i);

    assertValueEquality(sortedArr5, [10, 6, 3, 1, 0, ()]);

    int[] arr3 = [];
    int[] sortedArr6 = arr3.sort(array:DESCENDING, (i) => i);

    assertValueEquality(sortedArr6, []);
}

function testTupleReverse() {
    [int, string, float] tupleArr = [2, "abc", 2.4];
    anydata[] y = tupleArr.reverse();
    (int|string|float)[] expected = [2.4, "abc", 2];
    assertValueEquality(expected, y);

    [int, int, int] arr1 = [1, 2, 3];
    y = arr1.reverse();
    int[] res = [3, 2, 1];
    assertValueEquality(res, y);

    [int, int, int...] arr2 = [1, 2, 3, 4, 5];
    y = arr2.reverse();
    anydata[] res1 = [5, 4, 3, 2, 1];
    assertValueEquality(res1, y);
}

function testTupleFilter() {
    [int, string, float] tupleArr = [2, "abc", 2.4];
    anydata[] y = tupleArr.filter(function(anydata value) returns boolean {
        return (value is int);
    });

    (int|string|float)[] expected = [2];
    assertValueEquality(expected, y);

    [int, int, int] arr1 = [1, 2, 3];
    y = arr1.filter(function(int value) returns boolean {
        return value >= 2;
    });
    int[] res = [2, 3];
    assertValueEquality(res, y);

    [int, int, int...] arr2 = [1, 2, 3, 4, 5];
    y = arr2.filter(function(int value) returns boolean {
        return value > 2;
    });
    anydata[] res1 = [3, 4, 5];
    assertValueEquality(res1, y);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found '" + actualValAsString + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'false', found '" + actualValAsString + "'");
}

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

function testAsyncFpArgsWithArrays() returns [int, int[]] {
    int[] numbers = [-7, 2, -12, 4, 1];
    int count = 0;
    int[] filter = numbers.filter(function(int i) returns boolean {
        future<int> f1 = start getRandomNumber(i);
        int a = checkpanic wait f1;
        return a >= 0;
    });
    filter.forEach(function(int i) {
        future<int> f1 = start getRandomNumber(i);
        int a = checkpanic wait f1;
        filter[count] = i + 2;
        count = count + 1;
    });
    int reduce = filter.reduce(function(int total, int i) returns int {
        future<int> f1 = start getRandomNumber(i);
        int a = checkpanic wait f1;
        return total + a;
    }, 0);
    return [reduce, filter];
}

function testReadOnlyArrayFilter() {
    int[] & readonly numbers = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
    int[] evenNumbers = numbers.filter(val => val % 2 == 0);
    int count = 0;
    foreach int number in evenNumbers {
        assertValueEquality(count * 2, number);
        count += 1;
    }
    assertFalse(evenNumbers.isReadOnly());
}

function getRandomNumber(int i) returns int {
    return i + 2;
}

function testToStreamOnImmutableArray() {
    (byte[])[] & readonly arr = [[1, 2]];
    var strm = arr.toStream();

    stream<byte[] & readonly> castedstrm = <stream<byte[] & readonly>>strm;
    assertValueEquality([1, 2], castedstrm.next()?.value);
}

function testUnshiftLargeValues() {
    int[] list = [];
    foreach int i in 0 ..< 400 {
        list.unshift(i);
    }
    assertValueEquality(400, list.length());
    assertValueEquality(399, list[0]);
    assertValueEquality(199, list[200]);
    assertValueEquality(0, list[399]);
}

function func1(int i) returns boolean {
    return i > 2;
}

function testSome1() {
    assertValueEquality([1, 2].some(func1), false);
    assertValueEquality([1, 3].some(func1), true);
    assertValueEquality([].some(func1), false);

    int[] arr1 = [1, 3, 4, 5];
    assertValueEquality(arr1.some(func1), true);
    int[4] arr2 = [1, 3, 4, 5];
    assertValueEquality(arr2.some(func1), true);
    int[*] arr3 = [1, 3, 4, 5];
    assertValueEquality(arr3.some(func1), true);
}

function func2(int|string i) returns boolean {
    return i is string;
}

function testSome2() {
    assertValueEquality([1, 2].some(func2), false);
    assertValueEquality([].some(func2), false);
    assertValueEquality([1, 2, 3, "4"].some(func2), true);
    assertValueEquality([1, 2, 3, "4", 5].some(func2), true);

    (int|string)[] arr1 = [1, "str", "str"];
    assertValueEquality(arr1.some(func2), true);
}

function testSome3() {
    assertValueEquality([1, "2", 3].some(val => val is string), true);
    assertValueEquality([1, error("MSG"), 3].some(val => val is error), true);
}

function testSome4() {
    assertValueEquality([1, 2, 3, 4].filter(val => val >= 2).some(func1), true);
    assertValueEquality([1, 2, 3, 4].filter(val => val > 4).some(func1), false);
}

function testSome5() {
    [int, string] arr1 = [2, "34"];
    assertValueEquality(arr1.some(val => val is string), true);
    [int, string, int...] arr2 = [2, "23", 1, 2, 3, 4];
    assertValueEquality(arr2.some(val => val is int && val > 6), false);
}

function testSome6() {
    [int, string] & readonly arr1 = [2, "34"];
    assertValueEquality(arr1.some(val => val is string), true);
    [int, string, int...] & readonly arr2 = [2, "23", 1, 2, 3, 4];
    assertValueEquality(arr2.some(val => val is int && val > 6), false);
}

function testSome7() {
    [int, string...] arr = [2];
    assertValueEquality(arr.some(val => val is string), false);
    arr.push("str1");
    assertValueEquality(arr.some(val => val is string), true);
}

int[] globalArr = [1, 2, 3, 4];

function fun3(int val) returns boolean {
    _ = globalArr.pop();
    return val > 3;
}

function testModificationWithinSome() {
    boolean|error res = trap globalArr.some(fun3);
    assertTrue(res is error);

    error err = <error>res;
    var message = err.detail()["message"];
    string detailMessage = message is error ? message.toString() : message.toString();
    assertValueEquality("{ballerina/lang.array}IndexOutOfRange", err.message());
    assertValueEquality("array index out of range: index: 2, size: 2", detailMessage);
}

function func4(map<int> val) returns boolean {
    int? ii = val["i"];
    return ii !is () && ii > 10;
}

function testSome8() {
    map<int>[] arr = [{i: 2, j: 3}, {i: 20, j: 30}];
    assertValueEquality(arr.some(func4), true);
}

function func5(map<int> val) returns boolean {
    int? ii = val["k"];
    return ii !is () && ii > 10;
}

function testSome9() {
    map<int>[] arr = [{i: 2, j: 3}, {i: 20, j: 30}];
    assertValueEquality(arr.some(func5), false);
}

function testEvery1() {
    assertValueEquality([-1, 1].every(func1), false);
    assertValueEquality([1, 3].every(func1), false);
    assertValueEquality([5, 3].every(func1), true);
    assertValueEquality([].every(func1), true);

    int[] arr1 = [10, 3, 4, 5];
    assertValueEquality(arr1.every(func1), true);
    int[4] arr2 = [3, 2, 4, 5];
    assertValueEquality(arr2.every(func1), false);
    int[*] arr3 = [3, 2, 4, 5];
    assertValueEquality(arr3.every(func1), false);
}

function testEvery2() {
    assertValueEquality([1, 2].every(func2), false);
    assertValueEquality([].every(func2), true);
    assertValueEquality([1, 2, 3, "4"].every(func2), false);
    assertValueEquality(["1", "2", "3", "4", "5"].every(func2), true);

    (int|string)[] arr1 = ["1", 1, "str"];
    assertValueEquality(arr1.every(func2), false);
}

function testEvery3() {
    assertValueEquality(["1", "2", "3"].every(val => val is string), true);
    assertValueEquality([error("MSG1"), error("MSG2")].every(val => val is error), true);
    assertValueEquality([error("MSG1"), 23].every(val => val is error), false);
}

function testEvery4() {
    assertValueEquality([1, 2, 3, 4].filter(val => val > 2).every(func1), true);
    assertValueEquality([1, 2, 3, 4].filter(val => val > 1).every(func1), false);
}

function testEvery5() {
    [int, string] arr1 = [2, "34"];
    assertValueEquality(arr1.every(val => val is string), false);
    [int, string, int...] arr2 = [2, "23", 1, 2, 3, 4];
    assertValueEquality(arr2.every(val => val is int || val is string), true);
}

function testEvery6() {
    [int, string] & readonly arr1 = [2, "34"];
    assertValueEquality(arr1.every(val => val is string), false);
    [int, string, int...] & readonly arr2 = [2, "23", 1, 2, 3, 4];
    assertValueEquality(arr2.every(val => val is int || val is string), true);
}

function testEvery7() {
    [int, string...] arr = [2];
    assertValueEquality(arr.every(val => val is string), false);
    arr.push("str1");
    assertValueEquality(arr.every(val => val is string), false);
}

int[] globalArr1 = [1, 2, 3, 4];

function fun6(int val) returns boolean {
    _ = globalArr1.pop();
    return val > 0;
}

function testModificationWithinEvery() {
    boolean|error res = trap globalArr1.every(fun6); // runtime error
    assertTrue(res is error);

    error err = <error>res;
    var message = err.detail()["message"];
    string detailMessage = message is error ? message.toString() : message.toString();
    assertValueEquality("{ballerina/lang.array}IndexOutOfRange", err.message());
    assertValueEquality("array index out of range: index: 2, size: 2", detailMessage);
}

function testEvery8() {
    map<int>[] arr = [{i: 200, j: 300}, {i: 20, j: 30}];
    assertValueEquality(arr.every(func4), true);
}

function testEvery9() {
    map<int>[] arr = [{i: 2, j: 3}, {i: 20, j: 30}];
    assertValueEquality(arr.every(func5), false);
}

type T string|int;

function testArrSortWithNamedArgs1() {
    [string, T][] arr = [["a", "100"], ["b", "100"], ["d", "10"], ["c", "100"], ["e", "100"]];
    [string, T][] sortedArr = arr.sort(direction = array:DESCENDING, key = isolated function([string, T] e) returns string {
        return e[0];
    });
    assertValueEquality([["e","100"], ["d","10"], ["c","100"], ["b","100"], ["a","100"]], sortedArr);

    sortedArr = array:sort(arr, direction = array:DESCENDING, key = isolated function([string, T] e) returns string {
        return e[0];
    });
    assertValueEquality([["e","100"], ["d","10"], ["c","100"], ["b","100"], ["a","100"]], sortedArr);

    sortedArr = arr.sort(key = isolated function([string, T] e) returns string {
        return e[0];
    });
    assertValueEquality([["a","100"], ["b","100"], ["c","100"], ["d","10"], ["e","100"]], sortedArr);

    sortedArr = array:sort(arr, key = isolated function([string, T] e) returns string {
        return e[0];
    });
    assertValueEquality([["a","100"], ["b","100"], ["c","100"], ["d","10"], ["e","100"]], sortedArr);
}

function testArrSortWithNamedArgs2() {
    int[] arr = [1, 10, 3, 100, 0, -1, 10];
    int[] sortedArr = arr.sort(direction = array:DESCENDING);

    sortedArr = arr.sort(key = isolated function(int e) returns int {
        return e;
    });




}

function testArrSortWithNamedArgs3() {
    (int|string)[] arr = [1, "ABC", 10, "ADS", 0, "DES", "AAD"];
    (int|string)[] sortedArr = arr.sort(direction = array:ASCENDING, key = isolated function(int|string e) returns string {
        if e is int {
            return "XYZ";
        }
        return e;
    });
    assertValueEquality(["AAD", "ABC", "ADS", "DES", 1, 10, 0], sortedArr);

    sortedArr = array:sort(arr, direction = array:ASCENDING, key = isolated function(int|string e) returns string {
        if e is int {
            return "XYZ";
        }
        return e;
    });
    assertValueEquality(["AAD", "ABC", "ADS", "DES", 1, 10, 0], sortedArr);

    sortedArr = arr.sort(key = isolated function(int|string e) returns string {
        if e is int {
            return "AAA";
        }
        return e;
    });
    assertValueEquality([1, 10, 0, "AAD", "ABC", "ADS", "DES"], sortedArr);
}

function testPushWithObjectConstructorExpr() {
    Obj[] arr = [];
    arr.push(object {
        int i = 123;
        int j = 234;
    });
    assertValueEquality(1, arr.length());

    Obj ob = arr[0];
    assertValueEquality(123, ob.i);
    assertValueEquality(234, ob.j);
}

type Template object {
    *object:RawTemplate;

    public (readonly & string[]) strings;
    public int[] insertions;
};

function testPushWithRawTemplateExpr() {
    Template[] arr = [];
    int i = 12345;
    arr.push(`number ${i}`, `second number ${1 + 3} third ${i + 1}`);
    assertValueEquality(2, arr.length());

    Template temp = arr[0];
    assertValueEquality(["number ", ""], temp.strings);
    assertValueEquality([12345], temp.insertions);

    temp = arr[1];
    assertValueEquality(["second number ", " third ", ""], temp.strings);
    assertValueEquality([4, 12346], temp.insertions);
}

type Department record {|
    readonly string name;
    int empCount;
|};

function testPushWithTableConstructorExpr() {
    (table<Department> key(name))[] arr = [];
    arr.push(table [{name: "finance", empCount: 10}, {name: "legal", empCount: 5}]);
    assertValueEquality(1, arr.length());

    table<Department> key(name) tb = arr[0];
    assertValueEquality(5, tb.get("legal").empCount);
    assertValueEquality(10, tb.get("finance").empCount);
}

function testPushWithNewExpr() {
    Obj[] arr = [];
    arr.push(new (1, 2));
    assertValueEquality(1, arr.length());

    Obj ob = arr[0];
    assertValueEquality(1, ob.i);
    assertValueEquality(2, ob.j);
}

type Error distinct error;

function testPushWithErrorConstructorExpr() {
    Error[] arr = [];
    arr.push(error("e1"), error("e2"));
    assertValueEquality(2, arr.length());

    error e = arr[0];
    assertTrue(e is Error);
    assertValueEquality("e1", e.message());

    e = arr[1];
    assertTrue(e is Error);
    assertValueEquality("e2", e.message());
}

function testArrayPop() {
    int[] arr1 = [1, 2];
    assertValueEquality(arr1.pop(), 2);
    assertValueEquality(arr1, [1]);

    int[2] arr2 = [1, 2];
    int|error result = trap array:pop(arr2);
    assertTrue(result is error);
    if (result is error) {
        assertValueEquality("{ballerina/lang.array}OperationNotSupported", result.message());
        assertValueEquality("pop() not supported on type 'int[2]'",
        <string> checkpanic result.detail()["message"]);
    }

    int[] arr = arr2;
    result = trap arr.pop();
    assertTrue(result is error);
    if (result is error) {
        assertValueEquality("{ballerina/lang.array}OperationNotSupported", result.message());
        assertValueEquality("pop() not supported on type 'int[2]'",
        <string> checkpanic result.detail()["message"]);
    }
}

function testSetLengthNegative() {
    string:Char[] arr = ["a","b"];
    error? result = trap arr.setLength(5);
    assertTrue(result is error);
    if (result is error) {
        assertValueEquality("{ballerina/lang.array}IllegalListInsertion", result.message());
        assertValueEquality("array of length 2 cannot be expanded into array of length 5 without filler values",
        <string> checkpanic result.detail()["message"]);
    }

    [string:Char...] tup = ["a","b"];
    result = trap tup.setLength(10);
    assertTrue(result is error);
    if (result is error) {
        assertValueEquality("{ballerina/lang.array}IllegalListInsertion", result.message());
        assertValueEquality("tuple of length 2 cannot be expanded into tuple of length 10 without filler values",
        <string> checkpanic result.detail()["message"]);
    }
}

function testArrayFilterWithEmptyArrayAndTypeBinding() {
    var x = [];
    anydata[] y = x;
    anydata[] z = y.filter(v => v is int);
    assertValueEquality(z, []);
    assertTrue(z is never[]);
}

function testArrayReverseWithEmptyArrayAndTypeBinding() {
    var x = [];
    anydata[] y = x;
    anydata[] z = y.reverse();
    assertValueEquality(z, []);
    assertTrue(z is never[]);
}
