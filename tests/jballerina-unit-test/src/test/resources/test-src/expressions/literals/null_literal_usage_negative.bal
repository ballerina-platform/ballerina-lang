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

int? gVar = null;

function testNullLiteralUsage() {
    string? s1 = null;

    if (s1 == null) {
        s1 = "Hello World!";
    }

    json j = {name:"John", age:25, location:null};
    string? l = j.location == null ? null : <string>j.location;

    int?[] ar = [10, null, 30];

    callFnWithNullLiteral(null, 10, null, 30);
    callFnWithNullLiteral(null, ...ar);

    () nil = null;
}

function returnNull() returns () {
    return null;
}

function multiBinaryExpression(string? s, boolean one, boolean two, boolean three) returns boolean {
    return (!one || (two && three)) || s != null || (!three || (one && two));
}

function callFnWithNullLiteral(string? s, int?... nums) {
    // Empty function
}

type Person record {
    string? name = null;
};

class Foo {
    string? s = null;
}

function testNullWithMatch() returns string {
    int? x = 10;

    match x {
        0 => {return "0";}
        null => {return "null";}
    }

    return "";
}

type Foo2 1|2|null|3|4;

type Foo3 ();

function testNullWithFiniteType() {
    Foo3 f = null;
}

function testNestedTernaryExpr() {
    json j = {name:"John", age:25, location:null};
    string? l = (j.location == null ? () : <string>j.location) == null ? null : null;
}
