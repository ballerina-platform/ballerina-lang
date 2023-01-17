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

type Student record {
    string name;
    int age;
    float gpa;
};

function testQueryExpression() {
    Student s1 = {name: "Foo", age: 1, gpa: 2.1};
    Student s2 = {name: "Bar", age: 2, gpa: 3.2};
    Student s3 = {name: "Baz", age: 3, gpa: 1.3};

    Student[] students = [s1, s2, s3];

    var x = from var st in students
        where st.name == "Foo"
        select {name: st.name};
}

function testXMLNavigation() {
    xml val = xml `<foo><bar>0</bar></foo>`;
    xml bar = val/<bar>;
}

function testMappingConstructor(string name, int age) {
    Address adrs = {city: "Colombo 3", country: "Sri Lanka"};

    Person p = {
        name: name,
        age,
        [bar()]: "BAR",
        ...adrs
    };

    var john = <Person2>{
        name: "John Doe",
        age: 25,
        address: {city: "Colombo", country: "Sri Lanka"}
    };
}

function bar() returns string => "bar";

type Person record {
    string name;
    int age;
};

type Address record {
    string city;
    string country;
};

public type Person2 record {
    string name;
    int age;

    record {|
        string city;
        string country;
    |} address;
};

const LENGTH = 5;

function testConstArrLen() {
    int[LENGTH] a;
}

function testFunction() {
    string l = "";
    var [fName, fBody] = parseField(l);
}

function parseField(string val) returns [string, int] {
    return [val, 10];
}

int[] x1 = [1, 2 , 3];
int[] y1 = [...x1, 4];

function testListConstructorSpreadOp() {
    int[] x2 = [1, 2 , 3];
    int[] y2 = [...x2, 4];
    int[] y3 = [...y1, 5];
}

function testRegexpExp() {
    string:RegExp r1 = re `[a-z]`;
    string:RegExp r2 = re `((c)(d))`;
    string:RegExp r3 = re `[bB].tt[a-z]*`;
    string:RegExp r4 = re `[bB].${r3}`;
}
