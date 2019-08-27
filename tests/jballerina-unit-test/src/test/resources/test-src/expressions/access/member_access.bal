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

type ALL_WITHIN_RANGE 0|1;
type HALF_WITHIN_RANGE 1|2|9;

function testOpenArrayMemberAccessByLiteralPositive() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[0] == 1;
}

const I2 = 2;

function testOpenArrayMemberAccessByConstPositive() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[I2] == 3;
}

function testOpenArrayMemberAccessByVariablePositive() returns boolean {
    int[] ia = [1, 2, 3];
    int index = 1;
    return ia[index] == 2;
}

function testOpenArrayMemberAccessByFiniteTypeVariablePositive() returns boolean {
    int[] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testOpenArrayMemberAccessByLiteralIndexOutOfRange() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[4] == 1;
}

const I6 = 6;

function testOpenArrayMemberAccessByConstIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    return ia[I6] == 1;
}

function testOpenArrayMemberAccessByVariableIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    int index = 5;
    return ia[index] == 1;
}

function testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == 1;
}

function testClosedArrayMemberAccessByLiteralPositive() returns boolean {
    string[2] ia = ["one", "two"];
    return ia[0] == "one";
}

const I1 = 1;

function testClosedArrayMemberAccessByConstPositive() returns boolean {
    boolean[3] ia = [true, true, false];
    return ia[I1];
}

function testClosedArrayMemberAccessByVariablePositive() returns boolean {
    boolean[3] ia = [true, false, false];
    int index = 1;
    return ia[index] == false;
}

function testClosedArrayMemberAccessByFiniteTypeVariablePositive() returns boolean {
    int[3] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testClosedArrayMemberAccessByVariableIndexOutOfRange() returns boolean {
    int[2] ia = [1, 2];
    int index = 5;
    return ia[index] == 1;
}

function testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    int[4] ia = [1, 2, 3, 4];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == 1;
}

function testTupleMemberAccessByLiteralPositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    return ia[0] == 1;
}

function testTupleMemberAccessByConstPositive() returns boolean {
    [float, float] ia = [1.2, 32.2];
    return ia[I1] == 32.2;
}

function testTupleMemberAccessByVariablePositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    int index = 1;
    return ia[index] == 2;
}

function testTupleMemberAccessByFiniteTypeVariablePositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testTupleMemberAccessByVariableIndexOutOfRange() returns boolean {
    [string, float] ia = ["hello", 2.0];
    int index = 3;
    return ia[index] == 1.0;
}

function testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    [float, boolean, string] ia = [1.0, true, "hello"];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == false;
}

type Employee record {
    string name;
    boolean? registered = false;
    int id?;
};

function testRecordMemberAccessByLiteral() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string s2 = e["name"];
    boolean? b2 = e["registered"];
    int? i2 = e["id"];
    anydata v1 = e["other"];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}

const NAMEC = "name";
const REGISTEREDC = "registered";
const IDC = "id";
const OTHERC = "other";

function testRecordMemberAccessByConstant() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string s2 = e[NAMEC];
    boolean? b2 = e[REGISTEREDC];
    int? i2 = e[IDC];
    anydata v1 = e[OTHERC];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}


function testRecordMemberAccessByVariable() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string index = "name";
    anydata s2 = e[index];
    index = "registered";
    anydata b2 = e[index];
    index = "id";
    anydata i2 = e[index];
    index = "other";
    anydata v1 = e[index];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}

// TODO Maryam - add by finite

function testRecordMemberAccessForNonExistingKey() returns boolean {
    Employee e = { name: "Anne", id: 111, registered: true };

    anydata s2 = e["non_existing_key"];
    string index = "non_existing_key";
    anydata s4 = e[index];

    return s2 is () && s4 is ();
}

function testMapMemberAccessByLiteral() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<string|int|boolean> e = { name: s, id: i, registered: b };

    string|int|boolean? s2 = e["name"];
    string|int|boolean? b2 = e["registered"];
    string|int|boolean? i2 = e["id"];

    return s == s2 && b == b2 && i == i2;
}

function testMapMemberAccessByConstant() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<string|int|boolean> e = { name: s, id: i, registered: b };

    string|int|boolean? s2 = e[NAMEC];
    string|int|boolean? b2 = e[REGISTEREDC];
    string|int|boolean? i2 = e[IDC];

    return s == s2 && b == b2 && i == i2;
}

function testMapMemberAccessByVariable() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<anydata> e = { name: s, id: i, registered: b };

    string index = "name";
    anydata s2 = e[index];
    index = "registered";
    anydata b2 = e[index];
    index = "id";
    anydata i2 = e[index];

    return s == s2 && b == b2 && i == i2;
}

function testMapAccessForNonExistingKey() returns boolean {
    map<boolean> e = { one: true, two: false };
    anydata s2 = e["non_existing_key"];
    string index = "non_existing_key";
    anydata s4 = e[index];

    return s2 is () && s4 is ();
}

function testMemberAccessOnNillableMap1() returns boolean {
    map<string>? m = { "one": "1", "two": "2" };

    string index = "two";
    string? s1 = m["one"];
    string? s2 = m[index];
    return s1 == "1" && s2 == "2";
}

function testMemberAccessOnNillableMap2() returns boolean {
    map<map<int>>? m = { "one": { first: 1, second: 2 }, "two": { third: 3 } };

    string index = "two";
    int? s1 = m["one"]["first"];
    int? s2 = m[index]["third"];
    return s1 == 1 && s2 == 3;
}

function testMemberAccessNilLiftingOnNillableMap1() returns boolean {
    map<string>? m = ();

    string index = "two";
    string? s1 = m["one"];
    string? s2 = m[index];
    return s1 == () && s2 == ();
}

function testMemberAccessNilLiftingOnNillableMap2() returns boolean {
    map<map<string>>? m = ();

    string index = "two";
    string? s1 = m["one"]["two"];
    string? s2 = m[index]["three"];
    return s1 == () && s2 == ();
}

type Foo record {
    Bar b = { id: 1 };
};

type Bar record {
    int id;
};

function testMemberAccessOnNillableRecord1() returns boolean {
    Employee? m = { name: "Anne", registered: true };
    string index = "registered";
    string? s1 = m["name"];
    string|boolean|int|anydata s2 = m[index];
    return s1 == "Anne" && s2 == true;
}

function testMemberAccessOnNillableRecord2() returns boolean {
    Foo? m = { b: { id: 100 } };
    string index = "registered";
    int? s1 = m["b"]["id"];
    int|anydata s2 = m["b"][index];
    return s1 == 100 && s2 == ();
}

function testMemberAccessNilLiftingOnNillableRecord1() returns boolean {
    Employee? m = ();
    string index = "registered";
    string? s1 = m["name"];
    string|boolean|int|anydata s2 = m[index];
    return s1 == () && s2 == ();
}

function testMemberAccessNilLiftingOnNillableRecord2() returns boolean {
    Foo? m = ();
    string index = "registered";
    int? s1 = m["b"]["id"];
    int|anydata s2 = m["b"][index];
    return s1 == () && s2 == ();
}

type Baz record {
    string x;
    int y?;
};

type Qux record {
    string x;
    boolean y;
    float z = 1.0;
};

function testMemberAccessOnRecordUnion() returns boolean {
    Baz b = { x: "hello", y: 11 };
    Baz|Qux bq = b;
    string index = "x";

    string x = bq["x"];
    anydata x2 = bq[index];
    int|boolean? y = bq["y"];
    anydata z = bq["z"];

    return x == "hello" && x2 == "hello" && y == 11 && z is ();
}

function testMemberAccessOnMapUnion() returns boolean {
    map<string> b = { x: "hello", y: "world" };
    map<string>|map<map<boolean>> bq = b;
    string index = "x";

    string|map<boolean>? x = bq["x"];
    anydata x2 = bq[index];
    string|map<boolean>? y = bq["z"];

    return x == "hello" && x2 == "hello" && y is ();
}

function testMemberAccessOnMappingUnion() returns boolean {
    Baz b = { x: "hello", y: 11 };
    Baz|map<int>|map<map<float>> bq = b;
    string index = "x";

    string|int|map<float>? x = bq["x"];
    anydata x2 = bq[index];
    int|map<float>? y = bq["y"];
    anydata z = bq["z"];

    return x == "hello" && x2 == "hello" && y == 11 && z is ();
}

function testVariableStringMemberAccess() returns boolean {
    string s = "hello world";
    string[] sa = [];

    foreach int i in 0 ..< s.length() {
        sa[i] = s[i];
    }

    int index = 0;
    string newSt = "";
    foreach string st in sa {
        newSt += st;
    }
    return newSt == s;
}

const CONST_STRING_1 = "string value";

function testConstStringMemberAccess1() returns boolean {
    string[] sa = [];

    foreach int i in 0 ..< CONST_STRING_1.length() {
        sa[i] = CONST_STRING_1[i];
    }

    int index = 0;
    string newSt = "";
    foreach string st in sa {
        newSt += st;
    }
    return newSt == CONST_STRING_1;
}

const CONST_STRING_2 = "abcdef value";

const IIC1 = 1;
const IIC3 = 3;

function testConstStringMemberAccess2() returns boolean {
    return CONST_STRING_2[0] == "a" && CONST_STRING_2[IIC1] == "b" && CONST_STRING_2[2] == "c" &&
            CONST_STRING_2[IIC3] == "d";
}

function testOutOfRangeStringMemberAccess1() {
    string s = "hello";
    string s2 = s[-1];
}

function testOutOfRangeStringMemberAccess2() {
    string s = "hello world";
    string s2 = s[s.length()];
}

function testOutOfRangeStringMemberAccess3() {
    int i = 25;
    string s2 = CONST_STRING_2[i];
}

type StFooBar "foo"|"bar";
type IntValues -1|0|4;

function testFiniteTypeStringMemberAccess() returns boolean {
    StFooBar s1 = "bar";
    string s2 = "bar";
    IntValues i1 = 0;
    int i2 = 1;
    return s1[i1] == "b" && s1[i2] == "a" && s2[i1] == "b" && s2[i2] == "a";
}

function testOutOfRangeFiniteTypeStringMemberAccess() {
    StFooBar s = "foo";
    IntValues i = 4;
    _ = s[i];
}
