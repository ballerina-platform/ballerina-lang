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
|};

function simpleDefinition() returns [string, boolean] {
    Person p = {name: "Peter", married: true};
    Person {name: fName, married} = p;
    return [fName, married];
}

type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};

function recordVarInRecordVar() returns [string, int, string, boolean] {
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = getPersonWithAge();
    return [fName, theAge, format, married];
}

function getPersonWithAge() returns PersonWithAge {
    return { name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE" };
}

function recordVarInRecordVar2() returns [string, Age] {
    PersonWithAge p = { name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE" };
    PersonWithAge {name: fName, age} = p;
    return [fName, age];
}

type StreetCity record {
    string streetName;
    string city;
};

type Address record {
    int postalCode;
    StreetCity street;
};

type PersonWithAddress record {
    string name;
    boolean married;
    Address address;
};

function recordVarInRecordVarInRecordVar() returns [string, boolean, int, string, string] {
    PersonWithAddress personWithAdd =  {name: "Peter", married: true, address: {postalCode: 1000, street: {streetName: "PG", city: "Colombo 10"}}};
    PersonWithAddress {name: fName, married, address: {postalCode, street: {streetName: sName, city}}} = personWithAdd;
    return [fName, married, postalCode, sName, city];
}

type Employee record {
    string name;
    [int, string] address;
};

function tupleVarInRecordVar() returns [string, int, string] {
    Employee e = {name: "John", address: [20, "PG"]};
    Employee {name, address: [number, street]} = e;
    return [name, number, street];
}

function defineThreeRecordVariables() returns [string, int] {
    PersonWithAge p1 = { name: "John", age: {age:30, format: "YY"}, married: true, "work": "SE" };
    PersonWithAge p2 = { name: "Doe", age: {age:15, format: "MM"}, married: true, "work": "SE" };
    PersonWithAge p3 = { name: "Peter", age: {age:5, format: "DD"}, married: true, "work": "SE" };
    PersonWithAge {name: fName1, age: {age: theAge1, format: format1}, married: married1} = p1;
    PersonWithAge {name: fName2, age: {age: theAge2, format: format2}, married: married2} = p2;
    PersonWithAge {name: fName3, age: {age: theAge3, format: format3}, married: married3} = p3;

    string stringAddition = fName1 + fName2 + fName3 + format1 + format2 + format3;
    int intAddition = theAge1 + theAge2 + theAge3;
    return [stringAddition, intAddition];
}

function recordVariableWithRHSInvocation() returns string {
    Person {name: fName, married} = getPersonRecord();
    string name = fName + " Jill";
    return name;
}

function getPersonRecord() returns Person {
    Person person = {name: "Jack", married: true};
    return person;
}

function nestedRecordVariableWithRHSInvocation() returns string {
    PersonWithAge person = {name: "Peter", age: getAgeRecord(), married: true, "work": "SE"};
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = person;
    string name = fName + " Parker";
    return name;
}

function getAgeRecord() returns Age {
    Age a = {age: 99, format:"MM"};
    return a;
}

function testRestParameter() returns map<anydata|error> {
    PersonWithAge p = {name: "John", age: {age:30, format: "YY"}, married: true, "work": "SE", "other": getAgeRecord()};
    PersonWithAge {name, age: {age, format}, married, ...rest} = p;
    return rest;
}

function testNestedRestParameter() returns [map<anydata|error>, map<anydata|error>] {
    PersonWithAge p = { name: "John", age: { age: 30, format: "YY", "year": 1990 }, married: true, "work": "SE" };
    PersonWithAge {name, age: {age, format, ...rest1}, married, ...rest2} = p;
    return [rest1, rest2];
}

function testVariableAssignment() returns [string, int, string, boolean, map<anydata|error>] {
    PersonWithAge person = { name: "Peter", age: { age: 29, format: "Y" }, married: true, "work": "SE" };
    var {name: fName, age: {age, format}, married, ...rest} = person;
    return [fName, age, format, married, rest];
}

function testVariableAssignment2() returns [string, int, string, boolean, map<anydata|error>] {
    PersonWithAge person = { name: "Peter", age: { age: 29, format: "Y" }, married: true, "work": "SE"};
    var {name: fName, age: {age, format}, married, ...rest} = person;
    fName = "James";
    age = 30;
    format = "N";
    married = false;
    rest["added"] = "later";
    return [fName, age, format, married, rest];
}

// -------------------------

type Student record {
    string name;
    [int, int, int] dob;
    byte gender;
};

function testTupleVarDefInRecordVarDef() returns [string, [int, int, int], byte, string, int, int, int] {
    Student st1 = {name: "Mark", dob: [1, 1, 1990], gender: 1};
    Student {name, dob, gender} = st1;
    Student {name: sName, dob: [a, b, c]} = st1;
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
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    Parent {namesOfChildren, children, ...child} = parent;
    return [namesOfChildren, children[0].name, child];
}

function testRecordInsideTupleInsideRecord2() returns [string, int, int, string] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    Parent {namesOfChildren, children, child: {name, yearAndAge: [yearInt, {age, format}]}} = parent;
    return [name, yearInt, age, format];
}

function testRecordInsideTupleInsideRecordWithVar() returns [string[], string, map<anydata|error>] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    var {namesOfChildren, children, ...child} = parent;
    return [namesOfChildren, children[0].name, child];
}

function testRecordInsideTupleInsideRecord2WithVar() returns [string, int, int, string] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1998, {age: 20, format: "A"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "D", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    var {namesOfChildren, children, child: {name, yearAndAge: [yearInt, {age, format}]}} = parent;
    return [name, yearInt, age, format];
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

function testRecordVarWithUnionType() returns [int, float, UnionOne|UnionTwo] {
    UnionOne u1 = { var1: false, var2: 12, "restP1": "stringP1", "restP2": true };
    UnionThree u3 = {var1: 50, var2: 51.1, var3: u1};
    UnionThree {var1, var2, var3, ...rest} = u3;
    return [var1, var2, var3];
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

function testUnionRecordVariable() returns [string|boolean, string|boolean, string|boolean?, int|float?] {
    UnionRec1 rec = {var1: "A", var2: "B"};
    UnionRec1|UnionRec2 {var1, var2, var3, var4} = rec;

    return [var1, var2, var3, var4];
}

function testIgnoreVariable() returns [string, int] {
    PersonWithAge p = { name: "John", age: { age:30, format: "YY", "year": 1990 }, married: true, "work": "SE" };
    PersonWithAge {name, age: {age, format: _, ...rest1}, married: _, ...rest2} = p;
    return [name, age];
}

function testRecordVariableWithOnlyRestParam() returns map<anydata|error> {
    PersonWithAge p = { name: "John", age: { age:30, format: "YY", "year": 1990}, married: true, "work": "SE" };
    PersonWithAge { ...rest } = p;
    return rest;
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

type IntStringMap map<int|string>;

type ObjectMap map<Object>;

function testRestParameterType() returns [boolean, boolean, boolean, boolean, boolean] {
    IntRestRecord rec1 = { name: "A", married: true, "age": 19, "token": 200 };
    IntRestRecord { name: name1, ...other1 } = rec1;
    var { name: name2, ...other2 } = rec1;

    IntRestRecord|ObjectRestRecord rec3 = rec1;
    IntRestRecord|ObjectRestRecord { name: name5, ...other5 } = rec3;

    IntStringMap map1 = { name: "A", "age": 19, "token": 200 };

    IntStringMap|ObjectMap map2 = map1;
    IntStringMap|ObjectMap { name: name8, ...other9 } = map2;

    any a1 = other1;
    any a2 = other2;
    any a5 = other5;
    any a9 = other9;

    return [a1 is record{|never name?; boolean married; int...;|}, a2 is record{|int...;|},
    a5 is record{|never name?; boolean married; int|Object...;|}, a5 is map<anydata>,
    a9 is record {| never name?; (int|string|Object)...; |}];
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

function testInferredType(XY xy) returns NotXY {
    var {x: _, y: _, ...extra} = xy;
    return extra;
}

function testInferredResType() {
    NotXY extra = testInferredType({x: 10, y: 20, "foo": "bar"});
    assertEquality(extra["foo"], "bar");
}

function testRecordDestructuring1() {
    var {s: s, i: i, ...rest} = recordReturningFunc(10);
    int num1 = <int>rest.get("f");
    assertEquality(num1, 10);
    {s: s, i: i, ...rest} = recordReturningFunc((20));
    int num2 = <int>rest["f"];
    assertEquality(num2, 20);
}

function recordReturningFunc(int num) returns record { string s; int? i; } {
    return {s: "hello", i: 10, "f": num};
}

type Emp record {|
    string name;
    int age;
    string...;
|};

function testRecordDestructuring2() {
    string stdName;
    int age;
    map<string> details;
    {name: stdName, age, ...details} = <Emp>{name: "Jane Doe", age: 10, "foo": "bar"};
    assertEquality("bar", details["foo"]);
}

type StudentRecord record {
    int? Id;
    string studentName;
};

public record {
    int Id;
    string studentName;
} {Id, ...studentDetail} = {Id: 1001, studentName: "John", "Age": 24, "surName": "Paker"};

public function testRecordDestructuring3() {
    record {
        int? Id;
        string studentName;
    } {Id, ...studentDetail} = getStudentRecord(1);

    assertEquality("John", <string>studentDetail["studentName"]);
    assertEquality(24, <int>studentDetail["Age"]);
    assertEquality("Paker", <string>studentDetail["surName"]);
}

function getStudentRecord(int? id) returns record { int? Id; string studentName; } {
    return {Id: id, studentName: "John", "Age": 24, "surName": "Paker"};
}

type SchemaA record {|
    string name;
    int age;
    string...;
|};

type SchemaB record {|
    string name;
    boolean age;
    boolean married;
    int...;
|};

function testRestFieldResolvingWithUnion() {
    SchemaA recA = {name: "David", age:10, "foo":"bar"};
    SchemaA|SchemaB {name, ...rest} = recA;

    var age = rest.age;
    var fooVal = rest["foo"];
    assertEquality(10, age);
    assertEquality("bar", fooVal);

    rest.age = true;
    rest["foo"] = 100;
    age = rest.age;
    fooVal = rest["foo"];
    assertEquality(true, age);
    assertEquality(100, fooVal);

    rest.married = true;
    assertEquality(true, rest?.married);
}

public function testClosedRecordDefinedRestField() {
    string fullName;
    boolean isMarried;
    map<string> rest1;
    map<never> rest2;
    record {| never name?; boolean married; never...; |} rest3;

    Person p = {name: "Jane Doe", married: false};
    {name: fullName, married: isMarried, ...rest1} = p;

    record {| never name?; never married?; |} rec = {};
    rest2 = rec;
    {name: fullName, married: isMarried, ...rest2} = p;
    {name: fullName, ...rest3} = p;
    assertEquality(false, rest3.married);
}

function testRestFieldResolving() {
    testInferredResType();
    testRecordDestructuring1();
    testRecordDestructuring2();
    testRecordDestructuring3();
    testRestFieldResolvingWithUnion();
    testClosedRecordDefinedRestField();
}

//////////////////////////////////////////////////////////////////////////////////////

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
