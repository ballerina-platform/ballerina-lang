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

type Employee record {|
    string name;
    boolean? registered = false;
    int id?;
|};

type NONE_WITHIN_RANGE 5|10;
type ALL_STRINGS "a"|"b";

function testInvalidListAccessWithNonStringIndex() {
    Employee[] a1 = [{ name: "Anne" }, { name: "John" }];
    Employee[3] a2 = [{ name: "Anne" }, { name: "John" }, { name: "Bob" }];
    [int, float] t1 = [1, 23.0];
    string s1 = "c";
    ALL_STRINGS s2 = "a";

    _ = a1["literal"];
    _ = a1[s1];
    _ = a1[s2];

    _ = a2["literal"];
    _ = a2[s1];
    _ = a2[s2];

    _ = t1["literal"];
    _ = t1[s1];
    _ = t1[s2];
}

function testInvalidMemberAccessOnNillableList() {
    int[]? a1 = [];
    Employee[3]? a2 = [{ name: "Anne" }, { name: "John" }, { name: "Bob" }];
    int[]|Employee[3]? a3 = [1, 2];

    int index = 0;

    _ = a1[0];
    _ = a2[1];
    _ = a3[index];
}

function testInvalidOutOfRangeClosedArrayMemberAccessByLiteral() {
    int[3] ia = [1, 2, 3];
    _ = ia[4];
}

function testInvalidOutOfRangeTupleMemberAccessByLiteral() {
    [int, string, int] ia = [1, "11", 3];
    _ = ia[5];
}

function testRecordMemberAccessInvalidTypes() {
    string s = "Anne";
    int i = 100;

    Employee e = { name: s, id: i };

    boolean reg = e["registered"];
    int id = e["id"];
    string s2 = e["names"];
    string s3 = e[s];
}

function testMapMemberAccessInvalidTypes() {
    string s = "Anne";
    int i = 100;

    map<int|string> e = { name: s, id: i };

    string name = e["name"];
    int id = e["id"];
    string? s2 = e["name"];
    int|string s3 = e[s];
}

function testMemberAccessOnNillableMappingInvalidType() {
    map<string>? m = { "one": "1", "two": "2" };
    string index = "two";
    string s1 = m["one"];
    string s2 = m[index];

    map<map<int>>? m2 = { one: { two: 2 } };
    map<int> in1 = m2["one"];
    int in2 = m2["one"]["two"];
    int in3 = m2[index]["three"];

    Employee? e = { name: "Bill" };
    string s3 = e["name"];
    boolean s4 = e[index];
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

function testMemberAccessOnRecordUnionInvalidType() {
    Baz b = { x: "hello", y: 11 };
    Baz|Qux bq = b;
    string index = "x";

    int x = bq["x"];
    int x2 = bq[index];
    int|boolean y = bq["y"];
    float z = bq["z"];
    string w = bq[index][index];
}

function testMemberAccessOnMapUnionInvalidType() {
    map<string> b = { x: "hello", y: "world" };
    map<string>|map<map<float>|xml> bq = b;
    string index = "x";

    string x = bq["x"];
    string x2 = bq[index];
    string w = bq[index][index];
}

function testMemberAccessOnMappingUnionInvalidType() {
    Baz b = { x: "hello", y: 11 };
    Baz|map<int>|map<map<float>> bq = b;
    string index = "x";

    string x = bq["x"];
    int x2 = bq[index];
    map<float>? y = bq["y"];
    anydata z = bq["z"][index];
}

function testInvalidStringMemberAccess() {
    string s1 = "string value";
    string s2 = "x";
    int iv = 1;
    int sv1 = s1[0];
    float sv2 = s1[iv];
    string sv3 = s1["s"];
    string sv4 = s1[s2];
}

type StrOrInt "foo"|1;

function testInvalidFiniteTypeStringMemberAccess() {
    StrOrInt s1 = "foo";
    StrOrInt s2 = 1;
    string s3 = "foo";

    string s4 = s1[0];
    string s5 = s3[s2];
}

function testStringMemberAccessForAssignment() {
    string animal = "animal";
    animal[0] = "b";
}

const I5 = 5;

function testInvalidOutOfRangeClosedArrayMemberAccessByConstant() {
    Employee[2] ia = [{ name: "abc" }, { name: "def" }];
    _ = ia[I5];
}

function testInvalidOutOfRangeTupleMemberAccessByConstant() {
    [int, Employee] ia = [1, { name: "def" }];
    _ = ia[I5];
}

const NAMEC = "name";
const AGEC = "age";

function testInvalidRecordMemberAccessByConst() {
    Employee e = { name: "abc" };
    int i1 = e[NAMEC];
    int i2 = e[AGEC];
}

function testMemberAccessWithoutIndex() {
    int[] intArray = [];
    int value = intArray[];
    intArray[]
}
