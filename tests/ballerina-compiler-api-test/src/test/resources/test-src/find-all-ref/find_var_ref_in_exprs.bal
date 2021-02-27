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

int z = 0;

function testBinaryExpAndVarRef() {
    int a = 10 + z;

    if (true) {
        int b = 20;
        int c = 30 + b + a;

        if (false) {
            int d = 40;

            if (true) {
                int e = 50 + a + d + b;
            }

            int f = 60 + b;
        }

        int g = 70 + a + c;
        int h = 80;
    }
}

function testFuncCall() {
    foo(10);

    if (true) {
        foo(20);
    }
}

function testFuncCall2() {
    () v = 1 is int ? foo(30) : foo(40);
}

function foo(int x) {
}

function sum(int a, int b) returns int => a + b;

function testAction() {
    var v1 = start sum(10, 20);
    int x = wait v1;

    var v2 = start sum(20, 30);
    map<int> m = wait {v1, v2};
}

class PersonObj {
    string name;
    int age;

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }
}

function testTemplates() {
    int x = 10;
    string s = string `${x}${x + 1}`;
    'object:RawTemplate rt = `Hello ${new PersonObj("John Doe", x)}`;
    xml xm = xml `<age>${x}</age>`;
}

function testStructuralConstructors(anydata x) {
    anydata[] ad = [x, x is byte];
    map<anydata> m = {a: x.toString()};
}

function testAccessExprs() {
    PersonObj pObj = new("Jane Doe", 23);
    string name = pObj.name;

    record {|
        string name;
        int age?;
    |} person = {name: "J. Doe"};
    name = person.name;
    int? age = person?.age;

    name = <string>person["name"];
}
