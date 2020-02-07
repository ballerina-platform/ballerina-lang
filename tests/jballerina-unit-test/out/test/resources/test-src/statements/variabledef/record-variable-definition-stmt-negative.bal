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

type Age record {
    int age;
    string format;
};

type Person record {
    string name = "";
    boolean married;
};

type PersonWithAge record {
    string name;
    Age age;
};

function redeclaredSymbol() {
    Person p1 = {name: "Peter", married: true};
    Person {name: fName, married} = p1;

    PersonWithAge p2 = {name: "Peter", age: {age:29, format: "Y"}, "work": "SE"};
    PersonWithAge {name: fName, age: {age: theAge, format}} = p2; // redeclared symbol 'fName'

    Person p3 = {name: "Peter", married: true};
    Person {name: fiName, married: fiName} = p3; // redeclared symbol 'fiName'
}

function bindingPatternError() {
    Person p1 = {name: "John", married: true, "age": 12};
    Person {name1: fName1, married: maritalStatus1} = p1;

    Person p2 = {"name1": "John", married: true, "age": 12};
    Person {name1: fName2, married: maritalStatus2} = p2;

    Person p3 = {"name1": "John", married: true, "age": 12};
    Person {name: fName3, married: maritalStatus3} = p3;

    Person p4 = {name: "John", married: true, "age": 12};
    Person {name: fName4, married: maritalStatus4} = p4; // valid

    Person p5 = {married: true, "age": 12};
    Person {name: fName5, married: maritalStatus5} = p5;
}

function mismatchTypes() {
    PersonWithAge p = {name: "James", age: {age: 54, format: "DD"}, "married": true};
    Person {name: fName, married} = p; // incompatible types: expected 'Person', found 'PersonWithAge'
}

Person gPerson = {name: "Peter", married: true, "extra": "extra"};

type ClosedFoo record {|
    int a;
    ClosedBar b;
|};

type ClosedBar record {|
    float a;
    string b;
|};

function testClosedBindingPattern() {
    Person { name, married } = gPerson; // valid
    ClosedFoo clf = {a: 56, b: {a: 2.0, b: "A"}};
    ClosedFoo {a, b} = clf;
    ClosedFoo { a: a1 } = clf; // valid
    ClosedFoo { a: a2, b: { a: a3 } } = clf; // valid
    ClosedFoo { a: a4, b: { a: a5, b: b2 } } = clf; // valid
}

type EmployeeWithAge record {
    string name;
    Age age;
    boolean married;
};

function testVariableAssignment2() {
    EmployeeWithAge person = {name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE"};
    var {name: fName, age: {age, format}, married, ...rest} = person;
    fName = 30;
    age = "N";
    format = true;
    married = "A";
}

type UnionOne record {
    boolean var1;
    int var2;
    float var3 = 0;
};

type UnionTwo record {
    int var1;
    float var2;
};

type UnionThree record {
    int var1;
    float var2;
    UnionOne|UnionTwo var3;
};

function testRecordVarWithUnionType() {
    UnionOne u1 = {var1: false, var2: 12, "restP1": "stringP1", "restP2": true};
    UnionThree u3 = {var1: 50, var2: 51.1, var3: u1};
    UnionThree {var1, var2, var3: {var1: var3, var2: var4}, ...rest} = u3;
}

type UnionRec1 record {|
    string var1;
    string var2;
    string var3?;
    int...;
|};

type UnionRec2 record {|
    boolean var1;
    boolean var2;
    boolean var3;
    float...;
|};

function testUnionRecordVariable() returns [string|boolean, string|boolean, string|boolean, int|float] { // incompatible types
    UnionRec1 rec = {var1: "A", var2: "B"};
    UnionRec1|UnionRec2 {var1, var2, var3, var4} = rec;
    UnionOne|UnionTwo {var1: v1, var2: v2, var3: v3, var4: v4} = rec; // incompatible types: expected 'UnionOne|UnionTwo', found 'UnionRec1'
    return [var1, var2, var3, var4];
}

function testMapRecordVar() returns [string, anydata, any, string] { // incompatible types
    map<anydata> m = {var1: "A", var2: true};
    map<string> m2 = {var10: "B", var11: "C"};

    var {var1, var2, var3} = m;
    var {var10, var11, var12} = m2;

    return [var1, var2, var3, var10];
}

function ignoreVariables() {
    PersonWithAge p = {name: "James", age: {age: 54, format: "DD"}, "married": true};
    PersonWithAge {_: fName, _} = p; // underscore not allowed
    PersonWithAge {name: _, age: _} = p; // no new variables on left side
}
