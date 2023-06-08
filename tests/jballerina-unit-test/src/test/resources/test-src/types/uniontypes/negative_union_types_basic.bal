// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type ClosedFoo record {|
    string var1;
|};

public type OpenFoo record {
    string var1;
};

public type ClosedBar record {|
    string var1 = "";
|};

public type OpenBar record {
    string var1 = "";
};

public type Bar2 record {|
    int var1;
|};

public type Foo2 record {|
    string var2;
|};

function testAmbiguousAssignment() {
    OpenBar|OpenFoo x1 = {name:"John", id:12}; // Not ambiguous, only `OpenBar` can be created without a `var1` field
    ClosedBar|ClosedFoo x2 = {var1:"John"}; // Ambiguous since closed records and both have var1 field
    ClosedBar|OpenBar x3 = {var1:"xxx"}; // Ambigous since var1 is in closed record and other is open record
    ClosedBar|OpenBar x4 = {"var2":12}; // Not Ambigous since var2 is not in the closed record
    ClosedFoo|Foo2 x5 = {var2:"John"}; // Match to Foo2
    ClosedFoo|Foo2 x6 = {var2:12}; // Incompatible
}

type SomeTypes int:Signed8|object{};
type SomeTypes2 string:Char|object{};

function testIncompatibleAssignment() {
    int:Signed8|object{} v1 = 255;
    SomeTypes v2 = 255;
    string:Char|int v3 = "ABC";
    SomeTypes2 v4 = "ABC";
}

function testNullAsUnionMember() {
    boolean|null _ = "null"; // error
}

function testInvalidUsageOfListBPInUnion() {
    [string, boolean]|int [name1, married1] = fn1();
    [string, boolean]|[int] [name2, married2] = fn2();
    [string, boolean]|[int] [name3, married3] = fn3();
    [string, boolean]|[int] [name4, married4] = fn4();
    [string, boolean]|[int] [name5, married5] = fn5();
    [string, boolean]|error [name6, married6] = fn6();
    [string, int, boolean...]|int [name7, age, ...status] = fn7();
    [boolean, int]|[string, float] [m1, m2] = fn9();
}

function fn1() returns [string, boolean]|int => 1;
function fn2() returns [string, boolean]|[int] => [1];
function fn3() returns [int] => [1];
function fn4() returns [string, boolean]|[int] => ["Anne"];
function fn5() returns [string, boolean]|[int] => ["Anne", true];
function fn6() returns [string, boolean]|error => error("Oops!");
function fn7() returns [string, int, boolean...]|int => ["Anne", 40, true];
function fn9() returns [int, boolean]|[string, float] => [1, true];

type Person1 record {|
    int id;
    string fname;
|};

type Person2 record {|
    int id;
|};

type Person3 record {|
    int id;
    boolean...;
|};

type Person4 record {|
    int id;
    string fname?;
|};

function testInvalidUsageOfRecordBPInUnion() {
    Person1|error  {id: pId1, fname: pFname1} = fn10();
    Person2|Person1  {id: pId2, fname: pFname2} = fn11();
    Person1|Person2  {id: pId4, fname: pFname4} = fn12();
    Person1|int  {id: pId3, fname: pFname3} = fn13();
    Person1|Person2 {id: pId5, fname: pFname5} = fn14();
    Person3|int {id: pId6} = fn15();
    Person3|int {id: pId7, ...otherDetails} = fn15();
    Person4|Person1 {id: pId8, fname:pFname6} = fn16();
    record {|int empId;|}|Person1 {id: pId9, fname:pFname9} = fn17();
    Person1|record {|string id; byte fname;|} {id: pId10, fname: pFname10} = fn18();
}

function fn10() returns Person1|error => error("Oops!");
function fn11() returns Person1|Person2 => {id: 12};
function fn12() returns Person1|Person2 => {id: 12, fname: "Deelaka"};
function fn13() returns Person1|int => 1;
function fn14() returns Person2 => {id: 12};
function fn15() returns Person3|int => {id:12, "status": true};
function fn16() returns Person4|Person1 => {id:12};
function fn17() returns Person1|record {|int empId;|} => {empId: 12};
function fn18() returns Person1|record {|byte id; boolean fname;|} => {id: 1, fname: true};

function testInvalidUsageOfErrorBPInUnion() {
    int|error error(msg) = fn19();
}

function fn19() returns int|error => error("Oops!");
