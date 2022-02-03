// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string aString = "foo";
int anInt = 10;

function test() {
    string greet = "Hello " + aString;

    var greetFn = function (string name) returns string => HELLO + " " + name;
    greet = greetFn("Pubudu");

    if (true) {
        int a = 20;

        if (true) {
            var x = 0;
            // cursor pos
        }

        int y = 10;
    } else {
        int b = 40;
    }

    int z = 20;
}

function test2() {
    xmlns "https://ballerina.io" as b7a;

    int x;
    x = 10;
    x += 30;

    fail x;
}

const HELLO = "Hello";

function testLetExp1(float x) returns float {
    return let float x1 = x*x, float x2 = x + 5 in ;
}

function testLetExp2(float y) returns float =>
    let float x3 = y+y, float x4 = y*y in ;

function testVarFollowingIf() {
    int? a = 10;
    string b = "ABC";
    if a is () {
        return;
    }
    string c = b;
    int d = a;
    // cursor pos
}
