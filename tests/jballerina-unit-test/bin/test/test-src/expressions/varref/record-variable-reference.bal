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

type Person record {|
    string name;
    boolean married;
    Age age;
    [string, int] extra;
|};

function testVariableAssignment() returns [string, boolean, int, string] {
    string fName;
    boolean married;
    int theAge;
    string format;
    map<any|error> theMap;

    {name: fName, married, age: {age: theAge, format}, ...theMap} = getPerson();
    return [fName, married, theAge, format];
}

function getPerson() returns Person {
    return {name: "Peter", married: true, age: {age:12, format: "Y"}, extra:  ["ds", 4]};
}

type Foo record {
    map<int> var1;
    Bar var2;
};

type Bar record {
    int|float var1;
    Some var2;
};

type Some record {
    string var1;
    string? var2;
};

function testRecVarRefInsideRecVarRefInsideRecVarRef() returns [map<int>, int|float, string, string?] {
    map<int> fooVar1;
    int|float barVar1;
    string someVar1;
    string? someVar2;

    {var1: fooVar1, var2: {var1: barVar1, var2: {var1: someVar1, var2: someVar2}}} = getFoo();
    return [fooVar1, barVar1, someVar1, someVar2];
}

function getFoo() returns Foo {
    return {var1: {"mKey1": 1, "mKey2": 2}, var2: {var1: 12, var2: {var1: "SomeVar1", var2: ()}}};
}

type OpenRecord record {
    string var1;
    boolean var2;
};

function testRestParam() returns map<anydata|error> {
    OpenRecord openRecord = {var1: "var1", var2: false, "var3": 12, "var4": "text"};
    string var1;
    boolean var2;
    map<anydata|error> rest = {};
    {var1, var2, ...rest} = openRecord;
    return rest;
}

function testRecordTypeInRecordVarRef() returns [map<any>, int|float, Some] {
    map<int> fooVar1;
    int|float barVar1;
    Some var2;

    {var1: fooVar1, var2: {var1: barVar1, var2}} = getFoo();
    return [fooVar1, barVar1, var2];
}

type Student record {
    string name;
    [int, int, int] dob;
    byte gender;
};

function testTupleVarRefInRecordVarRef() returns [string, [int, int, int], byte, string, int, int, int] {
    Student st1 = {name: "Mark", dob: [1, 1, 1990], gender: 1};
    string name;
    [int, int, int] dob;
    byte gender;
    string sName;
    int a;
    int b;
    int c;

    {name, dob, gender} = st1;
    {name: sName, dob: [a, b, c]} = st1;
    return [name, dob, gender, sName, a, b, c];
}

type Parent record {
    string[] namesOfChildren;
    Child[] children;
    Child child;
};

type Child record {
    string name;
    [int, Age] yearAndAge;
};

function testRecordInsideTupleInsideRecord() returns [string[], string, map<anydata|error>] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "x"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    string[] namesOfChildren;
    Child[] children;
    map<anydata|error> child = {};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    {namesOfChildren, children, ...child} = parent;
    return [namesOfChildren, children[0].name, child];
}

function testRecordInsideTupleInsideRecord2() returns [string, int, int, string] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "x"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    string[] namesOfChildren;
    Child[] children;
    string name;
    int yearInt;
    int age;
    string format;

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    {namesOfChildren, children, child: {name, yearAndAge: [yearInt, {age, format}]}} = parent;
    return [name, yearInt, age, format];
}

class Object {
    private int 'field;

    public function init() {
        self.'field = 12;
    }

    public function getField() returns int {
        return self.'field;
    }
}

type IntRestRecord record {|
    string name;
    boolean married;
    int...;
|};

type ObjectRestRecord record {|
    string name;
    boolean married;
    Object...;
|};

function testRestParameterType() returns boolean {
    string name;
    map<anydata|error> other1 = {};
    map<any> other2 = {};

    IntRestRecord rec1 = { name: "A", married: true, "age": 19, "token": 200 };
    { name, ...other1 } = rec1;

    any a1 = other1;

    return a1 is map<anydata|error>;
}

// TODO: Uncomment below tests once record literal is supported with var ref

//function testVarAssignmentOfRecordLiteral() returns (string, boolean, int, string) {
//    string fName;
//    boolean married;
//    int theAge;
//    string format;
//    map theMap;
//
//    {name: fName, age: {age: theAge, format}, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, theAge, format);
//}
//
//function testVarAssignmentOfRecordLiteral2() returns (string, boolean, Age) {
//    string fName;
//    boolean married;
//    Age age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}
//
//function testVarAssignmentOfRecordLiteral3() returns (string, boolean, map) {
//    string fName;
//    boolean married;
//    map age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}
//
//function testVarAssignmentOfRecordLiteral4() returns (string, boolean, json) {
//    string fName;
//    boolean married;
//    json age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}

type EmployeeDetails record {|
    string emp\:name;
    int id;
|};

type ProductDetails record {|
    string x\:code = "default";
    int quantity;
|};

function testRecordFieldBindingPatternsWithIdentifierEscapes() {
    EmployeeDetails empDetails = {
        emp\:name: "Joy",
        id: 12
    };
    string emp\:name;
    int id;
    {emp\:name, id} = empDetails;
    assertEquality("Joy", emp\:name);
    assertEquality(12, id);

    ProductDetails prodDetails = {quantity: 1234};
    ProductDetails {x\:code, quantity} = prodDetails;
    prodDetails = {x\:code: "featured", quantity: 12345};
    {x\:code, quantity} = prodDetails;
    assertEquality("featured", x\:code);
    assertEquality(12345, quantity);

    [EmployeeDetails, ProductDetails] details = [{id: 234, emp\:name: "Amy"}, {x\:code: "basic", quantity: 324}];
    string a;
    int b;
    int c;
    string d;
    [{emp\:name: a, id: b}, {quantity: c, x\:code: d}] = details;
    assertEquality("Amy", a);
    assertEquality(234, b);
    assertEquality(324, c);
    assertEquality("basic", d);
}

type ReadOnlyRecord readonly & record {|
    int[] x;
    string y;
|};

function testReadOnlyRecordWithMappingBindingPatternInDestructuringAssignment() {
    ReadOnlyRecord f1 = {x: [1, 2], y: "s1"};
    int[] & readonly x;
    string y;

    {x, y} = f1;
    assertEquality(<int[]> [1, 2], x);
    assertEquality("s1", y);

    readonly & record {
        int[] a;
        ReadOnlyRecord b;
    } r = {a: [12, 34, 56], b: f1};
    int[] & readonly a;
    int[] & readonly x2;
    string y2;
    {a, b: {x: x2, y: y2}} = r;
    assertEquality(<int[]> [12, 34, 56], a);
    assertEquality(<int[]> [1, 2], x2);
    assertEquality("s1", y2);

    int[] c;
    int[] d;
    {a: c, b: {x: d, y: y2}} = r;
    assertEquality(<int[]> [12, 34, 56], c);
    assertEquality(<int[]> [1, 2], d);
    assertEquality("s1", y2);
}

type ClosedRec record {|
    string a;
    string b;
|};

type OpenRec record {
    string a;
    string b;
};

function testMappingBindingWithSingleNameFieldBinding() {
    record {|
        string a;
        string b;
    |} x = {a: "foo", b: "bar"};

    string a;
    string b;

    {a} = x;
    assertEquality("foo", a);

    {b} = x;
    assertEquality("bar", b);

    ClosedRec y = {a: "foo2", b: "bar2"};

    {a} = y;
    assertEquality("foo2", a);

    {b} = y;
    assertEquality("bar2", b);

    record {
        string a;
        string b;
    } v = {a: "foo3", b: "bar3"};

    {a} = v;
    assertEquality("foo3", a);

    {b} = v;
    assertEquality("bar3", b);

    OpenRec w = {a: "foo4", b: "bar4"};

    {a} = w;
    assertEquality("foo4", a);

    {b} = w;
    assertEquality("bar4", b);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toBalString() + "', found '" + actual.toBalString() + "'");
}
