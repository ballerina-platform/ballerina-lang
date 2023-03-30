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
    Person {name: fName1, married: maritalStatus1} = p1;

    Person p2 = {"name1": "John", married: true, "age": 12};
    Person {name: fName2, married: maritalStatus2} = p2;

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

type XY record {
    int x;
    int y;
};

// any field other than x and y
type NotXY record {
    never x?;
    never y?;
};

function testInferredType(XY xy) returns XY {
    var {x: _, y: _, ...extra} = xy;
    return extra;
}

type ClosedXY record {|
    int x;
    int y;
    string...;
|};

function testDefinedRestType() returns map<int> {
    int xx;
    int yy;
    map<int|string> extra;
    {x: xx, y: yy, ...extra} = <ClosedXY>{x:10, y:20, "foo":"bar"};
    return extra;
}

type OptionalXY record {
    int x?;
    int y?;
};

function testWithOptionalFields() returns map<int> {
    var {...extra} = <XY>{x:10, y:20, "foo":"bar"};
    return extra;
}

type PersonClosed record {|
    string name;
    int age;
    string...;
|};

function testRestFieldTypeCheck() {
    string s;
    int age;
    map<string> rest;

    PersonClosed p = {name: "Jane Doe", age: 20, "employed": "false"};
    {name: s, ...rest} = p;
}

type Employee record {|
    string name;
    int|error id;
    string...;
|};

function testRestFieldTypeCheckWithError() {
    string s;
    int id;
    map<string> rest;

    Employee emp1 = {name: "Jane Doe", id: error("custom error"), "employed": "false"};
    {name: s, ...rest} = emp1;

    Employee emp2 = {name: "Jean Doe", id: 10, "employed": "true"};
    {name: s, id, ...rest} = emp2;
}

function testDefinedRestField() {
    string s;
    int age;
    record {| never name?; never age?; (int|string)...; |} rest;

    PersonClosed p = {name: "Jane Doe", age: 20, "employed": "false"};
    {name: s,...rest} = p;

    if rest.hasKey("age") {
        panic error("Found 'age' field: " + rest.get("age").toString());
    }
}

function testTypedBinidingRestField() {
    PersonClosed p = {name: "Jane Doe", age: 20, "employed": "false"};
    PersonClosed {name,...rest} = p;

    record{|never name?; int...;|} extra = rest;
    int personAge = rest.age;
    string employed = rest.employed;
}

function DeclaredRestFieldMismach() {
    string stdName;
    int age;
    map<string> details;
    {name: stdName, ...details} = <PersonClosed>{name: "Jane Doe", age: 10, "foo": "bar"};
}

type SchemaA record {|
    string name;
    int age;
    string...;
|};

type SchemaB record {|
    string name;
    boolean age;
    boolean? married;
    int...;
|};

type SchemaC record {|
    string name;
    string age;
    int...;
|};

function testRestFieldResolving() {
    SchemaA recA = {name: "David", age:10, "foo":"bar"};
    SchemaA|SchemaB|SchemaC {name, ...rest} = recA;
    map<int|string> extra = rest;
    var extraRec = rest;
    boolean? married = extraRec.married;
}

record {|
    int i;
    error e;
|} n = {
    i: 1,
    e: error("")
};

var {i, e: _} = n;

function testWildCardBindingAssignability() {
    var {i: i2, e: _} = n;
}

type EmployeeNew record {
    string name;
};

function testInvalidFieldBindingPattern() {
    EmployeeNew e = {name: ""};
    var {name, id} = e;

    [record {int i?;}] y = [{}];
    var [{i: i2}] = y;

    record {| int i; int...; |} a = {i: 1};
    record {| int i; int...; |} {i: _, j} = a;
}

type Student1 record {
    string name;
    int id;
    int age?;
};

function testOptionalFieldsInRecordBindingPattern() {
    Student1 e = {name: "Jo", id: 1234};
    var {name: eName, id: eId, age: eAge} = e;
    int newAge = eAge; // error

    var {name, id: id, age} = e;
    newAge = age; // error
}

type Topt1 record {
    int a;
    record {
        int b?;
    }[1] c?;
};

function testInvalidOptionalFieldAssignment1() {
    Topt1 topt = {a: 4, c: [{b: 5}]};
    var {a, c} = topt;
    record {int b?;}[1] _ = c; // error
}

function testInvalidOptionalFieldAssignment2() {
    Topt1 topt = {a: 4, c: [{b: 5}]};
    var {a, c: [{b}]} = topt; // error
}

type Topt2 record {
   int? x;
   int? y;
};

function testRecordDefinitionWithOptionalFieldsNegative1() {
    int? x = 1;
    string? y = "2";
    Topt2 _ = {x, y}; // error
}

type ReadOnlyRecord readonly & record {|
    int[] x;
    string y;
|};

function testReadOnlyRecordWithMappingBindingPatternInVarDeclNegative1() {
    ReadOnlyRecord f1 = {x: [1, 2], y: "s1"};
    readonly & record {|
        string[] x;
        string y;
    |} {x, y} = f1; // error
}

function testReadOnlyRecordWithMappingBindingPatternInVarDeclNegative2() {
    ReadOnlyRecord f1 = {x: [1, 2], y: "s1"};
    ReadOnlyRecord {x, y} = f1;

    int[] a = [];
    x = a; // error
}
