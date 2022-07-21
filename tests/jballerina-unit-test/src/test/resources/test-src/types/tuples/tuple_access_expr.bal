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

import ballerina/lang.'int as ints;

function tupleAccessTest() returns string {
    [int, string, boolean, ()] tuple = [100, "string_value", true, ()];

    string finalResult = "";
    int index = 0;
    while index < 4 {
        int|string|boolean|() entry = tuple[index];
        finalResult += entry.toString() + ":";
        if entry is () {
            finalResult += "()";
        }
        if entry is int {
            finalResult += "int:";
        }
        if entry is string {
            finalResult += "string:";
        }
        if entry is boolean {
            finalResult += "boolean:";
        }
        index += 1;
    }
    return finalResult;
}

class Obj {
    private int intField;

    function init() {
        self.intField = 10;
    }

    function getIntField() returns int {
        return self.intField;
    }
}

function tupleAccessTestWithBehavioralValues() returns string {
    Obj obj = new;
    function (int) returns int aFn = x => x * 2;
    json jVal = "json";

    [Obj, function (int) returns int, string, json] tuple = [obj, aFn, "str", jVal];

    string finalResult = "";
    int index = 0;
    while index < 4 {
        Obj|(function (int) returns int)|string|json entry = tuple[index];
        if entry is Obj {
            finalResult += entry.getIntField().toString() + ":";
            finalResult += "object:";
        }
        if entry is function (int) returns int {
            finalResult += entry(4).toString() + ":";
            finalResult += "function:";
        }
        if entry is string|json {
            finalResult += entry.toString() + ":";
            finalResult += "string|json:";
        }
        index += 1;
    }
    return finalResult;
}

function tupleIndexAsFunction(string index) returns string {
    [string, float, boolean] tuple = ["string", 9.0, true];
    var result = tuple[getKey(index)];
    return result.toString();
}

function getKey(string key) returns int {
    var index = ints:fromString(key);
    if index is error {
        return -1;
    } else {
        return index;
    }
}

function tupleWithUnionType() returns anydata {
    [string|boolean, float] tuple = [true, 1.1];
    int index = 0;
    string|boolean|float result = tuple[index];
    return result;
}

function tupleInsideTupleAccess() returns [boolean, boolean] {
    [string, [string, int, [boolean, ()]], float] tuple = ["string", ["string_2", 200, [true, ()]], 9.0];
    boolean result = tuple[1][2][0];
    boolean result5 = false;
    int index1 = 1;
    int index2 = 2;
    int index3 = 0;
    string|[string, int, [boolean, ()]]|float result2 = tuple[index1];
    if result2 is [string, int, [boolean, ()]] {
        string|int|[boolean, ()] result3 = result2[index2];
        if result3 is [boolean, ()] {
            boolean? result4 = result3[index3];
            if result4 is boolean {
                result5 = result4;
            }
        }
    }
    return [result, result5];
}

function tupleIndexOutOfBoundTest1() {
    string name = "";
    [string, string] animals = ["Dog", "Mouse"];
    int index = 2;
    name = animals[index];
}

function tupleIndexOutOfBoundTest2() {
    string name = "";
    [string, string] animals = ["Dog", "Mouse"];
    int index = -1;
    name = animals[index];
}

function tupleIndexOutOfBoundTest3() {
    [string, string] animals = ["Dog", "Mouse"];
    int index = 2;
    animals[index] = "CAT";
}

function tupleIndexOutOfBoundTest4() {
    [string, string] animals = ["Dog", "Mouse"];
    int index = -1;
    animals[index] = "CAT";
}

const int INDEX_NEG_ONE = -1;
const int INDEX_ZERO = 0;
const int INDEX_ONE = 1;

type Foo record {|
    string x;
    int y;
|};

function testConstTupleIndex(int index) returns anydata {
    [Foo, boolean] tuple = [{ x: "s", y: 12 }, true];
    match index {
        0 => {
            Foo x = tuple[INDEX_ZERO];
            return x.y;
        }
        1 => {return tuple[INDEX_ONE];}
        _ => {return false;}
    }
}

function tupleIndexAccessOfSameTypeWithIndexFromMap() returns float {
    [float, float, float] floatTuple = [1.1, 2.2, 3.3];
    map<int> strMap = { x: 0, y: 1, z: 2 };
    float total = 0.0;
    total += floatTuple[<int>strMap["x"]];
    total += floatTuple[<int>strMap["y"]];
    total += floatTuple[<int>strMap["z"]];
    return total;
}

function testInvalidInsertionToTuple() {
    [string, boolean, int] tuple = ["str", true, 10];
    int index = 0;
    tuple[index] = "str2";
    tuple[index] = false;
}

function testTupleAccessToAnyAndAnydata() returns string {
    [string, boolean, int] tuple = ["str", true, 10];
    int index = 0;
    string result = "";
    any a = tuple[index];
    if a is string {
        a = a + ":";
        result += "string:" + a.toString();
    } else {
        result += "failed";
    }
    index += 1;
    anydata b = tuple[index];
    if b is boolean {
        result += "boolean:" + b.toString();
    } else {
        result += "failed";
    }
    return result;
}

type Bar record {|
    string fieldOne;
|};

type FiniteOne 0|1|2;
type FiniteTwo 0|1|4;

const SIX = 6;
type FiniteThree FiniteTwo|SIX;
type FiniteFour int|FiniteTwo|SIX;

function testTupleAccessUsingFiniteType() returns string {
    [string, boolean, Bar, float] tuple = ["string", true, { fieldOne: "string" }, 1.1];
    FiniteOne index0 = 0;
    FiniteOne index1 = 1;
    FiniteOne index2 = 2;

    string|boolean|Bar f1 = tuple[index0];
    var f2 = tuple[index1];
    string|boolean|Bar f3 = tuple[index2];

    string result = "";
    if f1 is string {
        result += f1;
    }
    if f2 is boolean {
        result += f2.toString();
    }
    if f3 is Bar {
        result += f3.fieldOne;
    }
    return result;
}

function testTupleAccessUsingUnionWithFiniteTypes() returns boolean {
    string s = "hello world";
    boolean b = true;
    Bar bar = { fieldOne: "string" };
    [string, boolean, Bar] tuple = [s, b, bar];
    FiniteFour index0 = 0;
    FiniteFour index1 = 1;
    FiniteFour index2 = 2;

    string|boolean|Bar f1 = tuple[index0];
    var f2 = tuple[index1];
    string|boolean|Bar f3 = tuple[index2];

    return f1 == s && f2 == b && f3 == bar;
}

function testTupleAccessUsingFiniteTypeNegative() returns string {
    [string, boolean, Bar, float] tuple = ["string", true, { fieldOne: "string" }, 1.1];
    FiniteTwo index0 = 0;
    FiniteTwo index1 = 4;

    string|boolean f1 = tuple[index0];
    var f2 = tuple[index1];

    return "";
}

function testTupleAccessUsingUnionWithFiniteTypesNegative() {
    [string, boolean, Bar, float] tuple = ["string", true, { fieldOne: "string" }, 1.1];
    FiniteThree index0 = 0;
    FiniteThree index1 = 6;

    string|boolean f1 = tuple[index0];
    var f2 = tuple[index1];
}

type IntTuple [int...];
type StringTuple [string...];
type BooleanTuple [boolean...];
type CustomTuple [int, string, boolean];

int index = 0;

[int...] moduleLevelIntTuple = [1, 2, 3];
[string...] moduleLevelStringTuple = ["string 1", "string 2", "string 3"];
[boolean...] moduleLevelBooleanTuple = [true, true, true];

public function testModuleLevelTupleAccessWithCustomType() {
    foreach int i in 0 ..< 3 {
        assertEquals(moduleLevelIntTuple[i], i + 1);
        assertEquals(moduleLevelStringTuple[i], string `string ${i + 1}`);
        assertEquals(moduleLevelBooleanTuple[i], true);
    }
}

public function testTupleAccessWithCustomType() {
    IntTuple intTuple = [1, 2, 3];
    StringTuple stringTuple = ["string 1", "string 2", "string 3"];
    BooleanTuple booleanTuple = [true, false, true];
    CustomTuple customTuple = [1, "string 1", true];

    [int...] expectedIntTuple = [1, 2, 3];
    [string...] expectedStringTuple = ["string 1", "string 2", "string 3"];
    [boolean...] expectedBooleanTuple = [true, false, true];

    assertEquals(intTuple[index], 1);
    assertEquals(intTuple[index + 1], 2);
    assertEquals(intTuple[index + 2], 3);

    assertEquals(stringTuple[index], "string 1");
    assertEquals(stringTuple[index + 1], "string 2");
    assertEquals(stringTuple[index + 2], "string 3");

    assertEquals(booleanTuple[index], true);
    assertEquals(booleanTuple[index + 1], false);
    assertEquals(booleanTuple[index + 2], true);

    assertEquals(customTuple[index], 1);
    assertEquals(customTuple[index + 1], "string 1");
    assertEquals(customTuple[index + 2], true);

    foreach var i in 0 ..< intTuple.length() {
        assertEquals(intTuple[i], expectedIntTuple[i]);
        assertEquals(stringTuple[i], expectedStringTuple[i]);
        assertEquals(booleanTuple[i], expectedBooleanTuple[i]);
    }
}

type IntTuple2 [int, int, int];
type StringTuple2 [string, string, string];
type BooleanTuple2 [boolean, boolean, boolean];
type CustomTuple2 [int, string, boolean];

public function testTupleAccessWithCustomType2() {
    IntTuple2 intTuple = [1, 2, 3];
    StringTuple2 stringTuple = ["string 1", "string 2", "string 3"];
    BooleanTuple2 booleanTuple = [true, false, true];
    CustomTuple2 customTuple = [1, "string 1", true];

    [int...] expectedIntTuple = [1, 2, 3];
    [string...] expectedStringTuple = ["string 1", "string 2", "string 3"];
    [boolean...] expectedBooleanTuple = [true, false, true];

    assertEquals(intTuple[index], 1);
    assertEquals(intTuple[index + 1], 2);
    assertEquals(intTuple[index + 2], 3);

    assertEquals(stringTuple[index], "string 1");
    assertEquals(stringTuple[index + 1], "string 2");
    assertEquals(stringTuple[index + 2], "string 3");

    assertEquals(booleanTuple[index], true);
    assertEquals(booleanTuple[index + 1], false);
    assertEquals(booleanTuple[index + 2], true);

    assertEquals(customTuple[index], 1);
    assertEquals(customTuple[index + 1], "string 1");
    assertEquals(customTuple[index + 2], true);

    foreach var i in 0 ..< intTuple.length() {
        assertEquals(intTuple[i], expectedIntTuple[i]);
        assertEquals(stringTuple[i], expectedStringTuple[i]);
        assertEquals(booleanTuple[i], expectedBooleanTuple[i]);
    }
}

type IntUnionStringTuple [int...]|[string...];
type StringUnionBooleanTuple [boolean...]|[string...];
type BooleanUnionIntTuple [boolean...]|[int...];
type CustomUnionTuple [int...]|[string...]|[int, string, boolean]|[boolean...];

public function testTupleAccessWithCustomUnionTypes() {
    IntUnionStringTuple intUnionStringTuple = [1, 2, 3];
    StringUnionBooleanTuple stringUnionBooleanTuple = ["string 1", "string 2", "string 3"];
    BooleanUnionIntTuple booleanUnionIntTuple = [true, false, true];
    CustomUnionTuple customUnionTuple = [1, "string 1", true];

    [int...] expectedIntTuple = [1, 2, 3];
    [string...] expectedStringTuple = ["string 1", "string 2", "string 3"];
    [boolean...] expectedBooleanTuple = [true, false, true];

    assertEquals(intUnionStringTuple[index], 1);
    assertEquals(intUnionStringTuple[index + 1], 2);
    assertEquals(intUnionStringTuple[index + 2], 3);

    assertEquals(stringUnionBooleanTuple[index], "string 1");
    assertEquals(stringUnionBooleanTuple[index + 1], "string 2");
    assertEquals(stringUnionBooleanTuple[index + 2], "string 3");

    assertEquals(booleanUnionIntTuple[index], true);
    assertEquals(booleanUnionIntTuple[index + 1], false);
    assertEquals(booleanUnionIntTuple[index + 2], true);

    assertEquals(customUnionTuple[index], 1);
    assertEquals(customUnionTuple[index + 1], "string 1");
    assertEquals(customUnionTuple[index + 2], true);

    foreach var i in 0 ..< intUnionStringTuple.length() {
        assertEquals(intUnionStringTuple[i], expectedIntTuple[i]);
        assertEquals(stringUnionBooleanTuple[i], expectedStringTuple[i]);
        assertEquals(booleanUnionIntTuple[i], expectedBooleanTuple[i]);
    }
}

type IntReadonlyTuple ([int...] & readonly)|[string...];
type StringReadonlyTuple [boolean...]|([string...] & readonly);
type BooleanReadonlyTuple ([boolean...] & readonly)|[int...];
type CustomReadonlyTuple [int...]|[string...]|([int, string, boolean] & readonly)|[boolean...];

public function testTupleAccessWithCustomReadonlyUnionTypes() {
    IntReadonlyTuple intReadonlyTuple = [1, 2, 3];
    StringReadonlyTuple stringReadonlyTuple = ["string 1", "string 2", "string 3"];
    BooleanReadonlyTuple booleanReadonlyTuple = [true, false, true];
    CustomReadonlyTuple customReadonlyTuple = [1, "string 1", true];

    [int...] expectedIntTuple = [1, 2, 3];
    [string...] expectedStringTuple = ["string 1", "string 2", "string 3"];
    [boolean...] expectedBooleanTuple = [true, false, true];

    assertEquals(intReadonlyTuple[index], 1);
    assertEquals(intReadonlyTuple[index + 1], 2);
    assertEquals(intReadonlyTuple[index + 2], 3);

    assertEquals(stringReadonlyTuple[index], "string 1");
    assertEquals(stringReadonlyTuple[index + 1], "string 2");
    assertEquals(stringReadonlyTuple[index + 2], "string 3");

    assertEquals(booleanReadonlyTuple[index], true);
    assertEquals(booleanReadonlyTuple[index + 1], false);
    assertEquals(booleanReadonlyTuple[index + 2], true);

    assertEquals(customReadonlyTuple[index], 1);
    assertEquals(customReadonlyTuple[index + 1], "string 1");
    assertEquals(customReadonlyTuple[index + 2], true);

    foreach var i in 0 ..< intReadonlyTuple.length() {
        assertEquals(intReadonlyTuple[i], expectedIntTuple[i]);
        assertEquals(stringReadonlyTuple[i], expectedStringTuple[i]);
        assertEquals(booleanReadonlyTuple[i], expectedBooleanTuple[i]);
    }
}

function testTupleWithRestTypesAccess() {
    [int...] t1 = [1, 2, 3, 5];
    [int, string, boolean...] t2 = [1, "a", true, true];

    var x1 = t1[0];
    int x2 = x1;
    assertEquals(x2, 1);

    var x3 = t2[0];
    int x4 = x3;
    assertEquals(x4, 1);

    var x5 = t2[3];
    boolean x6 = x5;
    assertEquals(x6, true);

    var x7 = t1[index];
    int x8 = x7;
    assertEquals(x8, 1);

    var x9 = t2[index];
    int|string|boolean x10 = x9;
    assertEquals(x10, 1);

    var x11 = t2[index + 2];
    int|string|boolean x12 = x11;
    assertEquals(x12, true);
}

type CustomTupleWithRestTypes1 [int...];
type CustomTupleWithRestTypes2 [int, string, boolean...];

function testCustomTupleWithRestTypesAccess() {
    CustomTupleWithRestTypes1 t1 = [1, 2, 3, 5];
    CustomTupleWithRestTypes2 t2 = [1, "a", true, true];

    var x1 = t1[0];
    int x2 = x1;
    assertEquals(x2, 1);

    var x3 = t2[0];
    int x4 = x3;
    assertEquals(x4, 1);

    var x5 = t2[3];
    boolean x6 = x5;
    assertEquals(x6, true);

    var x7 = t1[index];
    int x8 = x7;
    assertEquals(x8, 1);

    var x9 = t2[index];
    int|string|boolean x10 = x9;
    assertEquals(x10, 1);

    var x11 = t2[index + 2];
    int|string|boolean x12 = x11;
    assertEquals(x12, true);
}

function testTupleAccessWithByteType() {
    [int...] t1 = [1, 2, 3, 5];
    [int, string, boolean...] t2 = [1, "a", true, true];
    [int, string, boolean] t3 = [1, "a", true];

    CustomTupleWithRestTypes1 t4 = [1, 2, 3, 5];
    CustomTupleWithRestTypes2 t5 = [1, "a", true, true];
    CustomTuple t6 = [1, "a", true];

    byte byteIndex = 0;

    var x1 = t1[byteIndex];
    int x2 = x1;
    assertEquals(x2, 1);

    var x3 = t2[byteIndex];
    int|string|boolean x4 = x3;
    assertEquals(x4, 1);

    var x5 = t2[byteIndex + 2];
    int|string|boolean x6 = x5;
    assertEquals(x6, true);

    var x7 = t3[byteIndex];
    int|string|boolean x8 = x7;
    assertEquals(x8, 1);

    var x9 = t3[byteIndex + 2];
    int|string|boolean x10 = x9;
    assertEquals(x10, true);

    var x11 = t4[byteIndex];
    int x12 = x11;
    assertEquals(x12, 1);

    var x13 = t5[byteIndex];
    int|string|boolean x14 = x13;
    assertEquals(x14, 1);

    var x15 = t5[byteIndex + 2];
    int|string|boolean x16 = x15;
    assertEquals(x16, true);

    var x17 = t6[byteIndex];
    int|string|boolean x18 = x17;
    assertEquals(x18, 1);

    var x19 = t6[byteIndex + 2];
    int|string|boolean x20 = x19;
    assertEquals(x20, true);
}

const int constantIndex = 0;

function testTupleAccessWithConstantType() {
    [int...] t1 = [1, 2, 3, 5];
    [int, string, boolean...] t2 = [1, "a", true, true];
    [int, string, boolean] t3 = [1, "a", true];

    CustomTupleWithRestTypes1 t4 = [1, 2, 3, 5];
    CustomTupleWithRestTypes2 t5 = [1, "a", true, true];
    CustomTuple t6 = [1, "a", true];

    var x1 = t1[constantIndex];
    int x2 = x1;
    assertEquals(x2, 1);

    var x3 = t2[constantIndex];
    int|string|boolean x4 = x3;
    assertEquals(x4, 1);

    var x5 = t2[constantIndex + 2];
    int|string|boolean x6 = x5;
    assertEquals(x6, true);

    var x7 = t3[constantIndex];
    int|string|boolean x8 = x7;
    assertEquals(x8, 1);

    var x9 = t3[constantIndex + 2];
    int|string|boolean x10 = x9;
    assertEquals(x10, true);

    var x11 = t4[constantIndex];
    int x12 = x11;
    assertEquals(x12, 1);

    var x13 = t5[constantIndex];
    int|string|boolean x14 = x13;
    assertEquals(x14, 1);

    var x15 = t5[constantIndex + 2];
    int|string|boolean x16 = x15;
    assertEquals(x16, true);

    var x17 = t6[constantIndex];
    int|string|boolean x18 = x17;
    assertEquals(x18, 1);

    var x19 = t6[constantIndex + 2];
    int|string|boolean x20 = x19;
    assertEquals(x20, true);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(string `expected [${expected.toString()}], found [${actual.toString()}]`);
}
