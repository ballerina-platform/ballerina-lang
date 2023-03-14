// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public function functionTest() {
    int i = 3;
    string s = "s";

    function (int i, string s) returns int testFunc2 = function(int i, string s) returns int {
        return 3;
    };

    function (int i) returns function (int i, string s) returns int testFunc = intVal =>
}

public type FT function(int, int, string, float...) returns R1;

type R1 record {
    int field1?;
    int field2;
};

public function functionTest2() {
    FT x = function(int a, int b, string c, float... d) returns R1 {
        R1 rec = {field2: 0};
        return };
}

function arrTest() {
    int[] arr = [1];
    arr.push();
    int len = arr.length();
    string s = "abc".substring(1, );
}

function namedargTest(int x, string y) returns int{
    return x;
}

function namedArgTest() {
    int x = namedargTest(x = 2, y = );
}

type Coord record {
    int x;
    string y;
};

function argumentTest() {
    string ccc = "";
    string|Coord str = nameLen2(str = ccc, coord = );
}

function nameLen(string str, int len = str.length()) returns int {
    return len;
}

function nameLen2(string str, Coord coord) returns string|NewType {
    return str;
}

type NewType typedesc<int|string>;

service on new http:Listener(9090) {
    resource function get hi (http:Caller caller) {
        string ccc = "";
        string|NewType str = nameLenTest2(str = ccc, newType = );
    }
}

function nameLenTest(string str, int len = str.length()) returns int {
    return len;
}

function nameLenTest2(string str, NewType newType) returns string|NewType {
    return str;
}

function getIntValue(int x, string y) returns int {
    return x;
}

function functionCallTest() {
    string s = getIntValue(2, "a");
    string s = getIntValue( , );
    var s = getIntValue(2, "a");
    any s = getIntValue(2, "a");
    int|string a;
    int s = getIntValue(2, "a") + a;
    int s = getIntValue(a, "a");
}

type Type record {|
    string name;
|};

function testPositionalArgs() {
    json myJson;
    myJson.cloneWithType();
    myJson.cloneWithType(Ty);
    
    string s = "1abc";
    s.includes("abc");
    s.includes("abc",);
    s.includes("abc",2);
}

public function testRestParams() {
    add();
    add(1,2,);
    calculate("add");
    calculate("sub",);
    calculate("div",1,);
}

function add(int... vals) {

}

function calculate(string ops, int... vals) {

}

public function main() {
    myFunction("abc", arg3 = 10,)
}

function myFunction(string arg1, string arg2, int arg3 = 101) {

}
