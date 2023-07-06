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


[int, string, boolean, (), float, decimal] data = [20, "string", true, (), 15.5, 20.2];
string output = "";
int sum = 0;
int negSum = 0;

function add(int i) {
    sum = sum + i;
}

function addNeg(int i) {
    negSum = negSum + i;
}

function concatInt(int index, int value) {
    output = output + index.toString() + ":" + value.toString() + " ";
}

function concatString(int index, string value) {
    output = output + index.toString() + ":" + value + " ";
}

type Person record {
    string name;
    int age;
};

class Employee {
    string name;
    int age;

    function init() {
        self.name = "John";
        self.age = 10;
    }

    function growOld() {
        self.age += 1;
    }
}

function testTupleWithBasicTypes() returns string {
    string result = "";
    foreach var i in data {
        if i is () {
            result += "()";
        }
        result += i.toString();
    }
    return result;
}

function testTupleWithBasicTypesAddingInt() returns int {
    [int, int, string, int, string] ldata = [10, 5, "string", 3, "string"];
    int i = 0;
    foreach int|string v in ldata {
        if v is int {
            i += v;
        }
    }
    return i;
}

function testIntTupleComplex() returns [int, int, string] {
    [int, int, int, int, int, int, int, int] tupleData = [1, -3, 5, -30, 4, 11, 25, 10];
    output = "";
    sum = 0;
    negSum = 0;
    int i = 0;
    foreach int v in tupleData {
        if (v > 0) {
            add(v);
        } else {
            addNeg(v);
        }
        int x = 0;
        while (x < i) {
            concatInt(i,v);
            x = x + 1;
        }
        i += 1;
    }
    return [sum, negSum, output];
}

function testTupleWithTypeAny() returns int {
    Employee e = new;
    Person p = { name: "Peter", age: 12 };
    [Person, int, Employee, int, string] ldata = [p, 5, e, 3, "string"];
    int i = 0;
    foreach any v in ldata {
        if v is int {
            i += v;
        }
    }
    return i;
}

function testTupleWithTypeAnydata() returns int {
    Person p = { name: "Peter", age: 12 };
    [Person, int, string, int, string] ldata = [p, 5, "string", 3, "string"];
    int i = 0;
    foreach anydata v in ldata {
        if v is int {
            i += v;
        }
    }
    return i;
}

function testBreak() returns string {
    [string, string, string] sTuple = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sTuple {
        if (i == 1) {
            output = output + "break";
            break;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testContinue() returns string {
    [string, string, string] sTuple = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sTuple {
        if (i == 1) {
            output = output + "continue ";
            i += 1;
            continue;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testReturn() returns string {
    [string, string, string] sTuple = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sTuple {
        if (v == "d1") {
            return output;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testNestedWithBreakContinue() returns string {
    output = "";
    [string, string, string, string] sTuple = ["d0", "d1", "d2", "d3"];
    int i = 0;
    foreach var v in sTuple {
        concatString(i, v);
        foreach var j in 1 ... 5 {
            if (j == 4) {
                break;
            } else if (j == 2) {
                continue;
            }
            output = output + j.toString();
        }
        output = output + " ";
        i += 1;
    }
    return output;
}

function testTupleWithNullElements() returns string {
    output = "";
    [string, (), string, ()] sTuple = ["d0", (), "d2", ()];
    int i = 0;
    foreach var v in sTuple {
        if v is string {
            output = output + i.toString() + ":" + v + " ";
        } else {
           output = output + i.toString() + ": ";
        }
        i += 1;
    }
    return output;
}

function testTupleWithRestDescriptorInForeach1() {
    [[int, int, int...]] x = [[1, 2, 3, 4, 5]];
    foreach [int, int...] [a, ...b] in x {
        assertEquality(1, a);
        assertEquality(2, b[0]);
        assertEquality(3, b[1]);
        assertEquality(4, b[2]);
        assertEquality(5, b[3]);
    }
}

function testTupleWithRestDescriptorInForeach2() {
    [int[3]...] x = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
    [int, int[]][] arr = [];
    foreach [int, int...] [a, ...b] in x {
        arr.push([a, b]);
    }
    assertEquality(1, arr[0][0]);
    assertEquality(2, arr[0][1][0]);
    assertEquality(3, arr[0][1][1]);
    assertEquality(4, arr[1][0]);
    assertEquality(5, arr[1][1][0]);
    assertEquality(6, arr[1][1][1]);
    assertEquality(7, arr[2][0]);
    assertEquality(8, arr[2][1][0]);
    assertEquality(9, arr[2][1][1]);
}

function testTupleWithRestDescriptorInForeach3() {
    [[int, int...], [int, int, int], [int, int, int...]] x = [[1, 2], [3, 4, 5], [7, 8, 9, 10]];
    [int, int[]][] arr = [];
    foreach [int, int...] [a, ...b] in x {
        arr.push([a, b]);
    }
    assertEquality(1, arr[0][0]);
    assertEquality(2, arr[0][1][0]);
    assertEquality(3, arr[1][0]);
    assertEquality(4, arr[1][1][0]);
    assertEquality(5, arr[1][1][1]);
    assertEquality(7, arr[2][0]);
    assertEquality(8, arr[2][1][0]);
    assertEquality(9, arr[2][1][1]);
    assertEquality(10, arr[2][1][2]);
}

function testTupleWithRestDescriptorInForeach4() {
    [[int, int...], int[4]] x = [[1, 2, 3, 4], [7, 8, 9, 10]];
    int[][] arr = [];
    foreach [int...] [...a] in x {
        arr.push(a);
    }
    assertEquality(1, arr[0][0]);
    assertEquality(2, arr[0][1]);
    assertEquality(3, arr[0][2]);
    assertEquality(4, arr[0][3]);
    assertEquality(7, arr[1][0]);
    assertEquality(8, arr[1][1]);
    assertEquality(9, arr[1][2]);
    assertEquality(10, arr[1][3]);
}

function testTupleWithRestDescriptorInForeach5() {
    [[int, int], [int, int, int...], int[2]...] x = [[1, 2], [3, 4, 5, 6], [7, 8], [9, 10]];
    [int, int[]][] arr = [];
    foreach var [a, ...b] in x {
        arr.push([a, b]);
    }
    assertEquality(1, arr[0][0]);
    assertEquality(2, arr[0][1][0]);
    assertEquality(3, arr[1][0]);
    assertEquality(4, arr[1][1][0]);
    assertEquality(5, arr[1][1][1]);
    assertEquality(6, arr[1][1][2]);
    assertEquality(7, arr[2][0]);
    assertEquality(8, arr[2][1][0]);
    assertEquality(9, arr[3][0]);
    assertEquality(10, arr[3][1][0]);
}

type Bar record {
    int id;
    boolean flag;
};

function testTupleWithRestDescriptorInForeach6() {
    [[string, Bar, boolean...], [int, boolean[]], [float, int...]] t1 =
               [["Ballerina", {id: 34, flag: true}, false], [12, [true,false]], [1, 2, 3, 4]];
    [string|int|float, (Bar|boolean|boolean[]|int)[]][] a1 = [];
    foreach var [f1, ...f2] in t1 {
        a1.push([f1, f2]);
    }
    assertEquality("Ballerina", a1[0][0]);
    assertEquality(34, (<Bar>a1[0][1][0]).id);
    assertEquality(true, (<Bar>a1[0][1][0]).flag);
    assertEquality(false, a1[0][1][1]);
    assertEquality(12, a1[1][0]);
    assertEquality(2, (<boolean[]>a1[1][1][0]).length());
    assertEquality(true, (<boolean[]>a1[1][1][0])[0]);
    assertEquality(false, (<boolean[]>a1[1][1][0])[1]);
    assertEquality(1.0, a1[2][0]);
    assertEquality(3, (a1[2][1]).length());
    assertEquality(2, a1[2][1][0]);
    assertEquality(3, a1[2][1][1]);
    assertEquality(4, a1[2][1][2]);
}

function testIteratingEmptyTuple() {
    var a = [];
    var b = from var x in a
        select 1;
    assertEquality(0, b.length());

    var c = from var x in []
            select 1;
    assertEquality(0, c.length());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
