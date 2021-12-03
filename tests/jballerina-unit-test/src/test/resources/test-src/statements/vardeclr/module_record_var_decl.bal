//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//Test module level mapping binding pattern
Person {name: Fname, married: Married} = {name: "Jhone", married: true};

public function testBasic() {
    assertEquality("Jhone", Fname);
    assertTrue(Married);
}

// Record var inside record var
PersonWithAge {name: fName1, age: {age: theAge1, format: format1}, married} = getPersonWithAge();

function recordVarInRecordVar() {
    assertEquality("Peter", fName1);
    assertEquality(29, theAge1);
    assertEquality("Y", format1);
    assertTrue(married);
}

function getPersonWithAge() returns PersonWithAge {
    return {name: "Peter", age: {age: 29, format: "Y"}, married: true, "work": "SE"};
}

// Tuple var inside record var
PersonWithAge2 {name: fName2, age: [age2, format2], married: married2} = getPersonWithAge2();

function tupleVarInRecordVar() {
    assertEquality("Mac", fName2);
    assertEquality(21, age2);
    assertEquality("Y", format2);
    assertFalse(married2);
}

function getPersonWithAge2() returns PersonWithAge2 {
    return {name: "Mac", age: [21, "Y"], married: false};
}


// Check annotation support
const annotation annot on source var;

@annot
Age {age, format} = {age: 24, format: "myFormat"};
public function testRecordVarWithAnnotations() {
    assertEquality(24, age);
    assertEquality("myFormat", format);
}


// Test record variable reordering/forward referencing
Person {name: name3, married: married3} = {name: name4, married: married4};
Person {name: name4, married: married4} = {name: "Nicolette", married: false};
public function testVariableForwardReferencing() {
    assertEquality("Nicolette", name3);
    assertFalse(married3);
}

// Test record variable declared with var
var {name: name5, married: married5} = {name: "Allen", married: false};
public function testVariableDeclaredWithVar() {
    assertEquality("Allen", name5);
    assertFalse(married5);
}

var {fieldA: [fieldAVar], fieldB: {a: fieldBMapVar}, fieldC: error(message), ...restInt} = foo();

type complexRecord record {|
    [int] fieldA;
    map<string> fieldB;
    error fieldC;
    int...;
|};

function foo() returns complexRecord =>
    {fieldA: [8], fieldB: {a: "Ballerina"}, fieldC: error("NullPointer"), "int1": 1, "int2": 2};

function testVariableDeclaredWithVar2() {
    assertEquality(8, fieldAVar);
    assertEquality("Ballerina", fieldBMapVar);
    assertEquality("NullPointer", message);
    assertEquality(1, restInt["int1"]);
    assertEquality(2, restInt["int2"]);
}

// Test record variable with rest binding pattern
Student {name: studentName, age: studentAge, grade: studentGrade, ...marks} = getStudentDetails();
var {name: studentName2, age: studentAge2, grade: studentGrade2, ...marks2} = getStudentDetails2();
Employee {name: eName1, id: eId1} = getEmployee1();
var  {name: eName2, id: eId2} = getEmployee2();
function getStudentDetails() returns Student {
    return {name: "Flash", age: 15, format: "Y", grade: 10, "mark1": 50, "mark2": 85, "mark3": 90};
}
function getStudentDetails2() returns Student {
    return {name: "Arrow", age: 25, format: "Y", grade: 20, "mark1": 60, "mark2": 95, "mark3": 95};
}
function getEmployee1() returns Employee {
    return {name: "Jo", id: 1234};
}
function getEmployee2() returns Employee {
    return {name: "Ray", id: 4321};
}
public function testRecordVariableWithRestBP() {
    assertEquality("Flash", studentName);
    assertEquality(15, studentAge);
    assertEquality(10, studentGrade);
    assertEquality(50, marks["mark1"]);
    assertEquality(85, marks["mark2"]);
    assertEquality(90, marks["mark3"]);

    assertEquality("Arrow", studentName2);
    assertEquality(25, studentAge2);
    assertEquality(20, studentGrade2);
    assertEquality(60, marks2["mark1"]);
    assertEquality(95, marks2["mark2"]);
    assertEquality(95, marks2["mark3"]);

    assertEquality("Jo", eName1);
    assertEquality("Ray", eName2);
    assertEquality(1234, eId1);
    assertEquality(4321, eId2);
}

annotation record {int i;} x on function;
@x {
    i: studentAge
}
public function testVariableDeclaredInRecordAsAnnotationValue() {
    typedesc<function ()> td = typeof testVariableDeclaredInRecordAsAnnotationValue;
    record {int i;}? xVal = td.@x;
    assertEquality(<record {int i;}>{i:15}, xVal);
}

// Support codes
type Age record {
    int age;
    string format;
};

type Person record {|
    string name;
    boolean married;
|};

type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};

type PersonWithAge2 record {
    string name;
    [int, string] age;
    boolean married;
};

type Student record {|
    string name;
    int grade;
    *Age;
    int...;
|};

type Employee record {
    string name;
    int id;
    Employee lead?;
};

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
