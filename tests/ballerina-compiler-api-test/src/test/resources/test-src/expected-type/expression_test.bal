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

function add(int x, int y) returns int {
    int sum = x + y;
    return sum;
}

function foo() {
    int x = 1;
    if (add(1,2) < )

}

function testFunc() {
    int x = 1;
    if (add(1,) < )

}

public function main() {
    map<int> map1 = {field1: 10, field2: 20};
    map<int> map2 = {field3:10, };
}

class Cl1 {
    function init(int arg1, string arg2) {
    }
}

class Cl2 {
    function init(string arg1, string arg2) {
    }
}

public function implicitExpressionTest() {
    Cl2  cl2 = new ();
    Cl1|Cl2  cl = new ();
}

function testListConstructorExpression() {
    int[] v = [1, 2, 3];
    int x = v[];
}

function ifElseTest() {
    int x = 1;
    if () {
    }
}

function whileTest() {
    int x = 1;
    while () {
    }
}

class Person {
    int id;
    string name;
    int age;

    function init(string name, int age, int rank) {
        self.name = name;
        self.age = age;
        self.rank = rank;
    }

    public function setId(int id) {
        self.id = id;
    }
}

public function testExpressions() {
    Person person1 = new Person();
    Person person2 = new Person("John", )
    Person person3 = new Person("John", 25, )
}

function mapTest() {
    int a = 5;
    map<any> b = {a};
}

type MyType record {|
    int line;
    int offset;
|};

class MyClass {
    function init(int arg1, string arg2, MyType arg3) {

    }
}

function myFunc() {
    MyClass c1 = new(arg1 = 2, , );
    MyClass c2 = new(arg1 = 2, , );
    MyClass c3 = new(arg1 = 2, , , )
}

function testListConstructorExpression() {
    int[] nums1 = [1, 2, 3, ];
    int[] nums2 = [];
    int[] nums3 = ;
}
