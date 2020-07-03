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

public type StudentGrade record {|
   readonly string name;
   int physics;
   int chemistry?;
|};

function tableFunc(){
    table<StudentGrade> gradesTable = table key(name)[
            { name: "Mary", physics: 90, chemistry: 87 }
        ];

    assertEquality("name=Mary physics=90 chemistry=87", gradesTable.toString());
}

function arrayFunc(string[] strs) returns Grades[] {
    Grades g = {name: strs[0], physics: 75, chemistry: 65};
    Grades g1 = {name: strs[1], physics: 75, chemistry: 65};
    Grades g2 = {name: strs[2], physics: 75, chemistry: 65};

    Grades[] grds = [g,g1,g2];
    return grds;
}

function tupleTest() returns int {
   [int, string] a = [10, "John"];

   // int aint;
   // string astr;
   // (aint, astr) = a;

   // var (aint1, astr1) = a;

   [int, int] ret = divideBy([500,20]);
   // var (_, r1) = ret;

   return 10;
}

public function testRestType() {
    [int...] x = [1, 2];
    any y = x;
    assertEquality(y is string[], false);
}

public function testEmptyArrayType() {
    var x = [];
    any a = x;
    assertEquality(a is int[2], false);
    assertEquality(a is int[], true);

    string[] sa = [];
    any arr = sa;
    assertEquality(arr is string[], true);
    assertEquality(arr is int[], false);

    int[0] ia = [];
    any iarr = ia;
    assertEquality(iarr is int[0], true);
    assertEquality(iarr is int[], true);
}

function divideBy([int,int] d) returns [int, int] {
   //  int q = d[0] / d[1];
   // int r = d[0] % d[1];
    return [100, 200];
}

function recordsTest() returns string {
   Grades g = {name: "Jbal", physics: 75, chemistry: 65};
   Grades gOptional = {name: "Jbal", physics: 75};

   g.physics = 100;

   record {|
        string city;
        string country;
        string...;
    |} adr = { city: "London", country: "UK" };

    adr["street"] = "baker";

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
   any x = g2;
   return acceptAny(x);
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

int global = 0;

function futuresTest() returns anydata {
   future<()> p = start foo("abc", 7);
   future<string> p2 = start foo2("yy");
   future<string> p3 = acceptFuture(p2);
   return global;
}

public function foo(string r, int j) {
   global = global + 100;
   int i = 0;
   while(i < 1000) {
       i += 1;
   }
}

public function foo2(string k) returns string {
   global = global + 100;
   return k;
}

public function acceptFuture(future<string> pp) returns future<string> {
   future<string> p2 = pp;
   return p2;
}

function testStringAsJsonVal () returns (json) {
    json j = "Supun";
    return j;
}

function testIntAsJsonVal () returns (json) {
    json j = 5;
    return j;
}

function testFloatAsJsonVal () returns (json) {
    json j = 7.65;
    return j;
}

function testBooleanAsJsonVal () returns (json) {
    json j = true;
    return j;
}

function testNullAsJsonVal () returns (json) {
    json j = null;
    return j;
}

function testJsonWithNull () returns [json, any|error] {
    json j = {"name":null};
    return [j, j.name];
}

function testNestedJsonInit () returns (json) {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1, 5, 7]};
    return j;
}

function testJsonArrayInit () returns (json) {
    json j1 = {name:"supun"};
    json j2 = {name:"thilina"};
    json j3 = {name:"setunga"};
    json j = [j1, j2, j3, 10, "SriLanka", true, null];
    return j;
}

function testGetString () returns [string, string] {
    json j1 = "Supun";
    json j2 = {name:"Setunga"};
    string j1String;
    string j2String;
    j1String = <string> j1;
    j2String = <string> j2.name;
    return [j1String, j2String];
}

function testGetInt () returns [int, int] {
    json j1 = 25;
    json j2 = {age:43};
    int j1Int;
    int j2Int;
    j1Int = <int>j1;
    j2Int = <int>j2.age;
    return [j1Int, j2Int];
}

function testGetFloat () returns (float) {
    json j = {score:9.73};
    float jFloat;
    jFloat = <float>j.score;
    return jFloat;
}

function testGetBoolean () returns (boolean) {
    json j = {pass:true};
    boolean jBoolean;
    jBoolean = <boolean>j.pass;
    return jBoolean;
}

function testGetJson () returns (json|error) {
    json j = {address:{city:"Colombo", "country":"SriLanka"}};
    return j.address;
}

function testGetNonExistingElement () returns (any|error) {
    json j2 = {age:43};
    return j2.name;
}

function testAddString () returns (json) {
    json jj = {fname:"Supun"};
    map<json> j = <map<json>>jj;
    j["lname"] = "Setunga";
    return j;
}

function testAddInt () returns (json) {
    json jj = {fname:"Supun"};
    map<json> j = <map<json>>jj;
    j["age"] = 25;
    return j;
}

function testAddFloat () returns (json) {
    json jj = {fname:"Supun"};
    map<json> j = <map<json>>jj;
    j["score"] = 4.37;
    return j;
}

function testAddBoolean ()returns (json) {
    json jj = {fname:"Supun"};
    map<json> j = <map<json>>jj;
    j["status"] = true;
    return j;
}

function testAddJson ()returns (json) {
    json jj = {fname:"Supun"};
    map<json> j = <map<json>>jj;
    j["address"] = {country:"SriLanka"};
    return j;
}

function testUpdateString ()returns (json) {
    json jj = {fname:"Supun", lname:"Thilina"};
    map<json> j = <map<json>>jj;
    j["lname"] = "Setunga";
    return j;
}

function testUpdateInt ()returns (json) {
    json jj = {fname:"Supun", age:30};
    map<json> j = <map<json>>jj;
    j["age"] = 25;
    return j;
}

function testUpdateFloat () returns(json) {
    json jj = {fname:"Supun", score:7.65};
    map<json> j = <map<json>>jj;
    j["score"] = 4.37;
    return j;
}

function testUpdateBoolean () returns(json) {
    json jj = {fname:"Supun", status:false};
    map<json> j = <map<json>>jj;
    j["status"] = true;
    return jj;
}

function testUpdateJson () returns(json) {
    json jj = {fname:"Supun", address:{country:"USA"}};
    map<json> j = <map<json>>jj;
    j["address"] = {country:"SriLanka"};
    return j;
}

function testUpdateStringInArray () returns(json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = "d";
    return j;
}

function testUpdateIntInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = 64;
    return j;
}

function testUpdateFloatInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = 4.72;
    return j;
}

function testUpdateBooleanInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = true;
    return j;
}

function testUpdateNullInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = null;
    return j;
}

function testUpdateJsonInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = {country:"SriLanka"};
    return j;
}

function testUpdateJsonArrayInArray () returns (json) {
    json jj = ["a", "b", "c"];
    json[] j = <json[]>jj;
    j[1] = [1, 2, 3];
    return j;
}

function testGetNestedJsonElement () returns [string, string, string, string] {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1, 5, 7]};

    string addressKey = "address";
    string cityKey = "city";
    string cityString1;
    string cityString2;
    string cityString3;
    string cityString4;
    cityString1 = <string>j.address.city;
    cityString2 = <string>j.address.city;
    cityString3 = <string>j.address.city;
    cityString4 = <string>j.address.city;
    return [cityString1, cityString2, cityString3, cityString4];
}

function testJsonExprAsIndex () returns (string) {
    json j = {name:"aaa", address:{city:"Colombo", "area":"city"}};

    string addressKey = "address";
    string cityKey = "city";

    //Moving index expression into another line since with new changes, it is a unsafe cast,
    //which returns a error value if any.
    string key;
    key = <string>j.address.area;
    string value;
    map<json> adr = <map<json>>j.address;
    value = <string>adr[key];
    return value;
}

function testSetArrayOutofBoundElement () returns (json) {
    json jj = [1, 2, 3];
    json[] j = <json[]>jj;
    j[7] = 8;
    return j;
}

function testSetToNonArrayWithIndex () returns [json, json, json] {
    json jj1 = {name:"supun"};
    json jj2 = "foo";
    json jj3 = true;
    json[] j1 = <json[]>jj1;
    json[] j2 = <json[]>jj2;
    json[] j3 = <json[]>jj3;
    j1[7] = 8;
    j2[7] = 8;
    j3[7] = 8;
    return [j1, j2, j3];
}

function testGetFromNonArrayWithIndex () returns [json, json, json] {
    json jj1 = {name:"supun"};
    json jj2 = "foo";
    json jj3 = true;
    json[] j1 = <json[]>jj1;
    json[] j2 = <json[]>jj2;
    json[] j3 = <json[]>jj3;

    return [j1[7], j2[7], j3[7]];
}

function testSetToNonObjectWithKey () returns [json, json, json] {
    json jj1 = [1, 2, 3];
    json jj2 = "foo";
    json jj3 = true;

    map<json> j1 = <map<json>>jj1;
    map<json> j2 = <map<json>>jj2;
    map<json> j3 = <map<json>>jj3;

    j1["name"] = "Supun";
    j2["name"] = "Supun";
    j3["name"] = "Supun";
    return [j1, j2, j3];
}

function testGetFromNonObjectWithKey () returns [json|error, json|error, json|error] {
    json j1 = [1, 2, 3];
    json j2 = "foo";
    json j3 = true;
    return [j1.name, j2.name, j3.name];
}

function testGetStringInArray () returns (string) {
    json jj = ["a", "b", "c"];
    string value;
    json[] j = <json[]>jj;
    value = <string>j[1];
    return value;
}

function testGetArrayOutofBoundElement () returns (string) {
    json jj = [1, 2, 3];
    json[] j = <json[]>jj;
    string value;
    value = <string>j[5];
    return value;
}

function testGetElementFromPrimitive () returns (json|error) {
    json j = {name:"Supun"};
    return j.name.fname;
}

function testUpdateNestedElement () returns (json) {
    json jjj = {details:{fname:"Supun", lname:"Thilina"}};
    map<json> jj = <map<json>>jjj;
    map<json> j = <map<json>>jj["details"];
    j["lname"] = "Setunga";
    return jjj;
}

function testJsonArrayToJsonCasting () returns (json) {
    json[][] j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];

    json j2 = j1;
    return j2;
}

function testGetFromNull () returns (string) {
    json j2 = {age:43, name:null};
    string value = <string>j2.name.fname;
    return value;
}

function testAddToNull () returns (json) {
    json jjj = {name:"Supun", address:null};
    map<json> jj = <map<json>>jjj;
    map<json> j = <map<json>>jj["address"];
    j["country"] = "SriLanka";
    return j;
}

function testJsonIntToFloat () returns (float) {
    json j = {score:4};
    float jFloat = <float>j.score;
    return jFloat;
}

function testNullJsonToInt() returns (int) {
    json j = null;
    return <int>j;
}

function testNullJsonToFloat() returns (float) {
    json j = null;
    return <float>j;
}
function testNullJsonToString() returns (string) {
    json j = null;
    return <string>j;
}

function testNullJsonToBoolean() returns (boolean) {
    json j = null;
    return <boolean>j;
}

function testNullJsonToArray() returns (int[]) {
    json j = null;
    return <int[]>j;
}

function testIntArrayToJsonAssignment() returns [json, json] {
    int[] a = [1, 5, 9];
    json[] j = <json[]>a;
    j[3] = 4;
    return [j, j[1]];
}

function testFloatArrayToJsonAssignment() returns [json, json] {
    float[] f = [1.3, 5.4, 9.4];
    json[] j = <json[]>f;
    j[3] = 4.5;
    return [j, j[1]];
}

function testStringArrayToJsonAssignment() returns [json, json] {
    string[] s = ["apple", "orange"];
    json[] j = <json[]>s;
    j[2] = "grape";
    return [j, j[1]];
}

function testBooleanArrayToJsonAssignment() returns [json, json] {
    boolean[] b = [true, true, false];
    json[] j = <json[]>b;
    j[3] = true;
    return [j, j[1]];
}

function waitTest() returns string {
   future<()> p = start foo("abc", 7);
   future<string> p2 = start foo2("wait");

   string result = wait p2;
   wait p;
   future<string> p3 = acceptFuture(p2);
   
   return result;
}

string waitMultimple = "";

function waitOnSame() returns [string,string,string] {
    future<string> p1 = start foo2("wait1");
    future<()> p = start append("00");
    
    string wait2 = waitAgain();
    string p1Result = wait p1;

    waitSame(p);
    wait p;

    future<()> ap = start append("22");
    wait ap;

    future<()> ap2 = start append("33");
    wait ap2;

    return [p1Result, wait2, waitMultimple];
}

function waitSame(future<()> f) {
    wait f;
    future<()> ap = start append("11");
    wait ap;
}

function waitAgain() returns string {
    future<string> p2 = start foo2("wait2");
    string res = wait p2;
    return res;
}

function append(string str) {
    waitMultimple = waitMultimple + str;
}

public type Foo record {
    int a = 3;
    Foo? f = ();
};

public function testSelfReferencingRecord() returns Foo {
    Foo f1 = {a:1};
    Foo f2 = {a:2, f:f1};
    return f2;
}

function testDecimalWithoutArgs() returns decimal {
   decimal b = 7;
   return b;
}

function testDecimalWithArgs(decimal a) returns decimal {
   decimal b = 5 + a;
   return b;
}

function testTupleArrayTypeToString() {
	string[] arr = ["true"];
	any a = arr;
	[float][] f = <[float][]> a;
}

function testTypeDescValuePrint() {
	map<int|string> m1 = { one: 1, two: 2 };
    typedesc<map<anydata>> t1 = typeof m1;
    io:print(t1);
}

type AssertionError error;

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
