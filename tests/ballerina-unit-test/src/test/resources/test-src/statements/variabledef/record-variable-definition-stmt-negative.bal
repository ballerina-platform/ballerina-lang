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
    string name;
    boolean married;
};

type PersonWithAge record {
    string name;
    Age age;
};

function redeclaredSymbol() {
    Person p1 = {name: "Peter", married: true};
    Person {name: fName, married} = p1;

    PersonWithAge p2 = {name: "Peter", age: {age:29, format: "Y"}, work: "SE"};
    PersonWithAge {name: fName, age: {age: theAge, format}} = p2; // redeclared symbol 'fName'

    Person p3 = {name: "Peter", married: true};
    Person {name: fiName, married: fiName} = p3; // redeclared symbol 'fiName'
}

function bindingPatternError() {
    Person p1 = {name: "John", married: true, age: 12};
    Person {name1: fName1, married: maritalStatus1} = p1;

    Person p2 = {name1: "John", married: true, age: 12};
    Person {name1: fName2, married: maritalStatus2} = p2;

    Person p3 = {name1: "John", married: true, age: 12};
    Person {name: fName3, married: maritalStatus3} = p3;

    Person p4 = {name: "John", married: true, age: 12};
    Person {name: fName4, married: maritalStatus4, !...} = p4; // type 'Person' is not a closed record type

    Person p5 = {married: true, age: 12};
    Person {name: fName5, married: maritalStatus5} = p5;
}

function mismatchTypes() {
    PersonWithAge p = {name: "James", age: {age: 54, format: "DD"}, married: true};
    Person {name: fName, married} = p; // incompatible types: expected 'Person', found 'PersonWithAge'
}

Person gPerson = {name: "Peter", married: true, extra: "extra"};

type ClosedFoo record {
    int a;
    ClosedBar b;
    !...
};

type ClosedBar record {
    float a;
    string b;
    !...
};

function testClosedBindingPattern() {
    Person {name, married, !...} = gPerson; // type 'Person' is not a closed record type
    ClosedFoo clf = {a: 56, b: {a: 2.0, b: "A"}};
    ClosedFoo {a, b} = clf;
    ClosedFoo {a: a1, !...} = clf; // not enough fields to match to closed record type 'ClosedFoo'
    ClosedFoo {a: a2, b: {a: a3, !...}, !...} = clf; // not enough fields to match to closed record type 'ClosedBar'
    ClosedFoo {a: a3, b: {a: a4, b: b2, !...}, !...} = clf; // valid
}

type EmployeeWithAge record {
    string name;
    Age age;
    boolean married;
};

function testVariableAssignment2() {
    EmployeeWithAge person = {name: "Peter", age: {age:29, format: "Y"}, married: true, work: "SE"};
    var {name: fName, age: {age, format}, married, ...rest} = person;
    fName = 30;
    age = "N";
    format = true;
    married = "A";
}

type UnionOne record {
    boolean var1;
    int var2;
    float var3;
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
    UnionOne u1 = {var1: false, var2: 12, restP1: "stringP1", restP2: true};
    UnionThree u3 = {var1: 50, var2: 51.1, var3: u1};
    UnionThree {var1, var2, var3: {var1: var3, var2: var4}, ...rest} = u3;
}
