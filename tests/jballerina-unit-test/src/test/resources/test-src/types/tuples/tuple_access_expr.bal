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

import ballerina/io;
import ballerina/lang.'int as ints;

function tupleAccessTest() returns string {
    [int, string, boolean, ()] tuple = [100, "string_value", true, ()];

    string finalResult = "";
    int index = 0;
    while index < 4 {
        int|string|boolean|() entry = tuple[index];
        finalResult += io:sprintf("%s:", entry);
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
            finalResult += io:sprintf("%s:", entry.getIntField());
            finalResult += "object:";
        }
        if entry is function (int) returns int {
            finalResult += io:sprintf("%s:", entry(4));
            finalResult += "function:";
        }
        if entry is string|json {
            finalResult += io:sprintf("%s:", entry);
            finalResult += "string|json:";
        }
        index += 1;
    }
    return finalResult;
}

function tupleIndexAsFunction(string index) returns string {
    [string, float, boolean] tuple = ["string", 9.0, true];
    var result = tuple[getKey(index)];
    return io:sprintf("%s", result);
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
        result += io:sprintf("string:%s", a);
    } else {
        result += "failed";
    }
    index += 1;
    anydata b = tuple[index];
    if b is boolean {
        result += io:sprintf("boolean:%s", b);
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
        result += io:sprintf("%s", f2);
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
