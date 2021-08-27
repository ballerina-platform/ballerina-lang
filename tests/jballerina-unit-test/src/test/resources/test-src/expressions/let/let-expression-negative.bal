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
    string x1 = let string x2 = x1 in let string[] x3 = [x2] in x3[0];
    [int, string] [y1, y2] = [let int x = y1, int z = y2 in z * x * 12, y2];
    string z1 = let map<string> z2 = {z1} in z2["ar"] ?: "nil";
    string v1 = let string v2 = "A" in let string[] v3 = [func(v3)] in v3[0];
    Person pr = new;
    string a1 = let [string, string] [a2, a3] = [pr.getName(a1)] in a2;
    string a4 = let [string, string] [a2, a3] = [pr.getName(a4), getString(a3)] in a2;
    string a5 = let [string, string] [a2, a3] = [getString(a5), getString(a3)] in a2;
    string a6 = let [string, string] a2 = [getString(a2), "A"] in a2[0];
    string a7 = let map<string> a8 = {a7} in getString(a7);
    string a9 = let map<string> a8 = {"1": a8} in pr.getName(a9);
    string a10 = let map<string> a8 = {"1": getString(a10)} in a8["1"] ?: "nil";
    string a11 = let map<string> a8 = {"1": getString(a8)} in pr.getName(a11);
}

class Person {
    string name = "sample name";

    public function getName(string a) returns string {
        return self.name + " " + a;
    }
}

function func(string[] k) returns string {
    return "CDE" + k[0];
}

function getString(string s) returns string {
    return "This is a string:" + s;
}
