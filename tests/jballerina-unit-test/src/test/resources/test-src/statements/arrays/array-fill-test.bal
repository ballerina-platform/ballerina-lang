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

function testNilArrayFill(int index, () value) returns ()[] {
    ()[] ar = [];
    ar[index] = value;
    return ar;
}

function testBooleanArrayFill(int index, boolean value) returns boolean[] {
    boolean[] ar = [];
    ar[index] = value;
    return ar;
}

function testIntArrayFill(int index, int value) returns int[] {
    int[] ar = [];
    ar[index] = value;
    return ar;
}

function testFloatArrayFill(int index, float value) returns float[] {
    float[] ar = [];
    ar[index] = value;
    return ar;
}

function testDecimalArrayFill(int index, decimal value) returns decimal[] {
    decimal[] ar = [];
    ar[index] = value;
    return ar;
}

function testStringArrayFill(int index, string value) returns string[] {
    string[] ar = [];
    ar[index] = value;
    return ar;
}

type Person record {
    string name;
    int age;
};

function testArrayOfArraysFill(int index) returns Person[][] {
    Person p = {name: "John", age: 25};
    Person[] personArr = [p, p];
    Person[][] ar = [];
    ar[index] = personArr;
    return ar;
}

function testTupleArrayFill(int index) returns [string, int][] {
    [string, int] tup = ["Hello World!", 100];
    [string, int][] ar = [];
    ar[index] = tup;
    return ar;
}

function testTupleSealedArrayFill(int index) returns [string, int][] {
    [string, int] tup = ["Hello World!", 100];
    [string, int][251] ar = []; // 251 == index + 1
    ar[index] = tup;
    return ar;
}

function testMapArrayFill(int index, map<any> value) returns map<any>[] {
    map<any>[] ar = [];
    ar[index] = value;
    return ar;
}

type Employee record {
    readonly int id;
    string name;
    float salary;
};

function testTableArrayFill(int index) {
    table<Employee> tbEmployee = table key(id) [
                    {id: 1, name: "John", salary: 50000}
        ];

    table<Employee>[] ar = [];
    ar[index] = tbEmployee;

    string name = "";
    foreach var tab in ar {
        foreach var row in tab {
            name += row.name;
        }
    }

    assertEquality("[{\"id\":1,\"name\":\"John\",\"salary\":50000.0}]", ar[index].toString());
    assertEquality("John", name.trim());
}

function testXMLArrayFill(int index) returns xml[] {
    xml value = xml `<name>Pubudu</name>`;
    xml[] ar = [];
    ar[index] = value;
    return ar;
}

function testUnionArrayFill1(int index) returns (string|int|Person|())[] {
    (string|int|Person|()) value = "Hello World!";
    (string|int|Person|())[] ar = [];
    ar[index] = value;
    return ar;
}

function testUnionArrayFill2(int index) returns (string|string)[] {
    (string|string) value = "Hello World!";
    (string|string)[] ar = [];
    ar[index] = value;
    return ar;
}

function testUnionArrayFill3(int index) returns (Person|Person|())[] {
    (Person|Person) value = {name: "John", age: 25};
    (Person|Person|())[] ar = [];
    ar[index] = value;
    return ar;
}

// disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/13612
type LiteralsAndType 1|2|int;

function testUnionArrayFill4(int index) returns LiteralsAndType[] {
    LiteralsAndType value = 1;
    LiteralsAndType[] ar = [];
    ar[index] = value;
    return ar;
}

function testOptionalTypeArrayFill(int index) returns string?[] {
    string? value = "Hello World!";
    string?[] ar = [];
    ar[index] = value;
    return ar;
}

function testAnyArrayFill(int index) returns any[] {
    Person p = {name: "John", age: 25};
    any value = p;
    any[] ar = [];
    ar[index] = value;
    return ar;
}

function testAnySealedArrayFill(int index) returns any[] {
    Person p = {name: "John", age: 25};
    any value = p;
    any[251] ar = []; // 251 == index + 1
    ar[index] = value;
    return ar;
}

function testAnydataArrayFill(int index) returns anydata[] {
    Person p = {name: "John", age: 25};
    anydata value = p;
    anydata[] ar = [];
    ar[index] = value;
    return ar;
}

function testAnydataSealedArrayFill(int index) returns anydata[] {
    Person p = {name: "John", age: 25};
    anydata value = p;
    anydata[251] ar = []; // 251 == index + 1
    ar[index] = value;
    return ar;
}

function testByteArrayFill(int index, byte value) returns byte[] {
    byte[] ar = [];
    ar[index] = value;
    return ar;
}

function testJSONArrayFill(int index) returns json[] {
    json value = {name: "John", age: 25};
    json[] ar = [];
    ar[index] = value;
    return ar;
}

type digit 0|1|2|3|4|5|6|7|8|9;

function testFiniteTypeArrayFill1(int index) returns digit[] {
    digit value = 5;
    digit[] ar = [];
    ar[index] = value;
    return ar;
}

type FP 0.0|1.2|2.3;

function testFiniteTypeArrayFill2(int index) returns FP[] {
    FP value = 1.2;
    FP[] ar = [];
    ar[index] = value;
    return ar;
}

type bool false|true;

function testFiniteTypeArrayFill3(int index) returns bool[] {
    bool[] ar = [];
    ar[index] = true;
    return ar;
}

type state false|true|();

function testFiniteTypeArrayFill4(int index) returns state[] {
    state[] ar = [];
    ar[index] = true;
    return ar;
}

const decimal ZERO = 0.0;
const decimal ONE_TWO = 1.2;
const decimal TWO_THREE = 2.3;

type DEC ZERO|ONE_TWO|TWO_THREE;

function testFiniteTypeArrayFill5(int index) returns DEC[] {
    DEC value = 1.2;
    DEC[] ar = [];
    ar[index] = value;
    return ar;
}

type One 1;

function testSingletonTypeArrayFill(int index) returns One[] {
    One[] ar = [];
    ar[index] = 1;
    return ar;
}

type bTrue true;

function testSingletonTypeArrayFill1() returns bTrue[] {
    bTrue[] bTrueAr1 = [];
    bTrueAr1[1] = true;
    return bTrueAr1;
}

function testSingletonTypeArrayStaticFill() returns bTrue[] {
    bTrue[2] bTrueAr1 = [];
    bTrueAr1[1] = true;
    return bTrueAr1;
}

class Student {
    public string name;
    public int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }
}

function testSequentialArrayInsertion() returns Student[] {
    Student s = new("Grainier", 28);
    Student[] arr = [];
    int i = 0;
    while (i < 5) {
        arr[i] = s;
        i += 1;
    }
    return arr;
}

function testTwoDimensionalArrayFill() returns int[][] {
    int[][] x = [];
    x[1] = [1, 3];
    return x;
}

class Obj {
    int i;

    function init() {
        self.i = 1;
    }
}

function testArrayFillWithObjs() returns Obj[][] {
    Obj o = new;
    Obj[] objArray = [];
    objArray[0] = o;
    Obj[][] multiDimObjArray = [];
    multiDimObjArray[0] = objArray;
    multiDimObjArray[2] = objArray;
    return multiDimObjArray;
}

function testFiniteTypeArrayFill() returns DEC[] {
    DEC value = 1.2;
    DEC[] ar = [];
    ar[5] = value;

    LiteralsAndType value2 = 1;
    LiteralsAndType[] ar2 = [];
    ar2[5] = value2;
    return ar;
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
