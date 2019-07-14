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

    Employee e = { name: s, id: i, registered: b, other: 1.0 };

    string s2 = e["name"];
    boolean? b2 = e["registered"];
    int? i2 = e["id"];
    anydata v1 = e["other"];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}

function testRecordMemberAccessByVariable() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, other: 1.0 };

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
