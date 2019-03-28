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

function testIntWithoutArgs() returns int {
   int b = 7;
   return b;
}

function testIntWithArgs(int a) returns int {
   int b = 5 + a;
   return b;
}

function testStringWithoutArgs() returns string {
   string s1 = "Hello";
   return s1;
}

function testStringWithArgs(string s) returns string {
   string s1 = "Hello" + s;
   return s1;
}

int globalVar = 7;

function getGlobalVar() returns int {
    return globalVar;
}

function testByteWithoutArgs() returns int {
   int b = 7;
   return b;
}

byte globalByte = 0;

function testByteValue() returns byte {
    byte a = 0;
    a = 34;
    return a;
}

function testByteValueSpace() returns byte {
    byte a = 0;
    a = 234;
    return a;
}

function testByteDefaultValue() returns byte {
    byte a = 0;
    return a;
}

function testByteParam(byte b) returns byte {
    byte a = 0;
    a = b;
    return a;
}

function testGlobalByte(byte b) returns byte {
    globalByte = b;
    return globalByte;
}

function testIntToByteCast(int b) returns byte|error {
    byte a = <byte> b;
    return a;
}

function testByteToIntCast(byte a) returns int {
    int b = <int>a;
    return b;
}

function testIntToByteExplicitCast(int b) returns byte|error {
    byte a = <byte> b;
    return a;
}

function testByteArray() returns byte[] {
    byte[] ba = [12, 24, 7];
    return ba;
}

function testByteArrayAssignment(byte[] cArrayIn) returns byte[] {
    byte[] cArray;
    cArray = cArrayIn;
    return cArray;
}

function testArray(string str) returns int {
    int[] a = [];
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    string[3] e = ["c", "d", "e"];
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    Grades[] gs = arrayFunc(e);
    a[0] = b[2];
    return a[0];
}

public type Grades record {|
   string name;
   int physics;
   int chemistry?;
|};

function arrayFunc(string[] strs) returns Grades[] {
    Grades g = {name: strs[0], physics: 75, chemistry: 65};
    Grades g1 = {name: strs[1], physics: 75, chemistry: 65};
    Grades g2 = {name: strs[2], physics: 75, chemistry: 65};

    Grades[] grds = [g,g1,g2];
    return grds;
}

function tupleTest() returns int {
   (int, string) a = (10, "John");

   // int aint;
   // string astr;
   // (aint, astr) = a;

   // var (aint1, astr1) = a;

   (int, int) ret = divideBy((500,20));
   // var (_, r1) = ret;

   return 10;
}

function divideBy((int,int) d) returns (int, int) {
   //  int q = d[0] / d[1];
   // int r = d[0] % d[1];
    return (100, 200);
}

function recordsTest() returns string {
   Grades g = {name: "Jbal", physics: 75, chemistry: 65};
   Grades gOptional = {name: "Jbal", physics: 75};

   g.physics = 100;

   record {
        string city;
        string country;
        string...;
    } adr = { city: "London", country: "UK" };

    adr.street = "baker";

   return acceptRecord(g).name;
}

function acceptRecord(Grades g) returns Grades {
   g.name = "JBallerina";
   return g;
}

function unionTest() returns string|int|float {
   int|string uni = 10;
   uni = "abc";

   return acceptUnion(uni);
}

function acceptUnion(int|string unionParam) returns int|string|float {
   int|string|float  bigUnion =  unionParam;
   bigUnion = 800;
   bigUnion = "union";
   bigUnion = 10.5;
   return bigUnion;
}

function anyTest() returns any {
   Grades g = {name: "Jbal", physics: 75, chemistry: 65};
   any a = g;
   Grades g2 = <Grades>a;

   int[] ia = [1, 3, 5, 6];
   any ar = ia;
   return acceptAny(g2);
}

function acceptAny(any anyval) returns any {
   Grades grd = <Grades> anyval;
   grd.chemistry = 89;
   return grd;
}

function anyDataTest() returns anydata {
   anydata a = 5;
   int intVal = <int>a;
   int[] ia = [1, 3, 5, 6];
   anydata ar = ia;
   return acceptAnydata(ar);
}

function acceptAnydata(anydata data) returns anydata {
   int[] ia = <int[]> data;
   ia[1] = 1000;
   return ia[1];
}
