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

function redclaredVar() {
    int x = 5;
    int b = let int x = 4 in 2 * x;
}

function undefinedVar() {
    int b = let int x = 4*y, int y = 2 in 2 * x;
}

function wrongTypes() {
    int b = let int x = "hello" in 2 * x;
    int c = let string x = "hello" in x;
    int d = let string x = fun() in x.length();
    string p = let string x = "hello" in fun();
}

function fun() returns int {
    return 1;
}

function invalidTypesWithAnonymousRecord() {
    record {
        byte i;
        int j?;
    } rec1 = let int v = 160 in {i: v};

    record {
        string i;
        int j?;
    } rec2 = let string v = "160" in {i: v, j: "invalid"};
    
    rec2.j = 2.0;
}

class FooClass {
    public function init(int m) {
    }
}

function testLetWithClass() {
    FooClass foo = let int m = 5 in new (m, m);
}

function testSelfReferencingVarInLet() {
    string s = let string[] ar = [s] in ar[0];
    string m = let [string, string] [a, b] = [a] in a;
    string n = let [string, string] [k, l] = [n] in l;
}
