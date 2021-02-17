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

function testMapRecordVar() returns [anydata, anydata, anydata, string?, string?, string?] {
    map<anydata> m = {var1: "A", var2: true};
    map<string> m2 = {var10: "B", var11: "C"};

    var {var1, var2, var3} = m;
    var {var10, var11, var12} = m2;

    return [var1, var2, var3, var10, var11, var12];
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

function testRestParameterType() returns [boolean, boolean, boolean, boolean, boolean] {
    IntRestRecord rec1 = { name: "A", married: true, "age": 19, "token": 200 };
    IntRestRecord { name: name1, ...other1 } = rec1;
    var { name: name2, ...other2 } = rec1;

    IntRestRecord|ObjectRestRecord rec3 = rec1;
    IntRestRecord|ObjectRestRecord { name: name5, ...other5 } = rec3;

    map<string> stringMap = { a: "A", b: "B" };
    map<string> { a, ...other6 } = stringMap;

    any a1 = other1;
    any a2 = other2;
    any a5 = other5;
    any a6 = other6;

    return [a1 is map<anydata|error>, a2 is map<int>, a5 is map<any|error>,
                                                                    a5 is map<anydata>, a6 is map<anydata|error>];
}
