// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function funcWithParams(string x, int y, float f = 12.34, *Grades grades, string... rest) {
    int sum = x + y;
}

type PersonObj object {
    string name;
    int age;
    anydata[] misc;

    public function set(string name, int age = 0, anydata... other);
};

class Person {
    string name;
    int age;
    anydata[] misc;

    public function init(string name, int age = 0, anydata... other) {
        self.name = name;
        self.age = age;
        self.misc = other;
    }

    public function setName(string name) {
        self.name = name;
    }

    public function setAge(int age = 0) {
        self.age = age;
    }

    public function setMisc(anydata... misc) {
        self.misc = misc;
    }
}

function foo() {
    var fn = function (Person p, anydata... misc) {};
}

type Grades record {
    int maths;
    int physics;
    int chemistry;
};

function exprBodyScope(string myStr) returns string => myStr

function defFunc(boolean k) {
}

function funcWithFuncParam(function (int) abc, function (boolean) pqr = defFunc, function (string)... xyz) {

}
