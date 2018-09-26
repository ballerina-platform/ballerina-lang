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
    !...
};

function simpleDefinition() returns (string, boolean) {
    Person {name: fName, married} = {name: "Peter", married: true};
    return (fName, married);
}

type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};

function recordVarInRecordVar() returns (string, int, string, boolean) {
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = {name: "Peter", age: {age:29, format: "Y"}, married: true, work: "SE"};
    return (fName, theAge, format, married);
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

function recordVarInRecordVarInRecordVar() returns (string, boolean, int, string, string) {
    PersonWithAddress {name: fName, married, address: {postalCode, street: {streetName: sName, city}}} =
                {name: "Peter", married: true, address: {postalCode: 1000, street: {streetName: "PG", city: "Colombo 10"}}};
    return (fName, married, postalCode, sName, city);
}

//type Employee record {
//    string name;
//    (int, string) address;
//};
//
//function tupleVarInRecordVar() returns (string, int, string) {
//    Employee {name, address: (number, street)} = {name: "John", address: (20, "PG")};
//    return (name, number, street);
//}

function defineThreeRecordVariables() returns (string, int) {
    PersonWithAge {name: fName1, age: {age: theAge1, format: format1}, married: married1} = {name: "John", age: {age:30, format: "YY"}, married: true, work: "SE"};
    PersonWithAge {name: fName2, age: {age: theAge2, format: format2}, married: married2} = {name: "Doe", age: {age:15, format: "MM"}, married: true, work: "SE"};
    PersonWithAge {name: fName3, age: {age: theAge3, format: format3}, married: married3} = {name: "Peter", age: {age:5, format: "DD"}, married: true, work: "SE"};

    string stringAddition = fName1 + fName2 + fName3 + format1 + format2 + format3;
    int intAddition = theAge1 + theAge2 + theAge3;
    return (stringAddition, intAddition);
}
function recordVariableWithRHSInvocation() returns string {
    Person {name: fName, married} = getPersonRecord();
    string name = fName + " Jill";
    return name;
}

function getPersonRecord() returns Person {
    Person p = {name: "Jack", married: true};
    return p;
}

function nestedRecordVariableWithRHSInvocation() returns string {
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = {name: "Peter", age: getAgeRecord(), married: true, work: "SE"};
    string name = fName + " Parker";
    return name;
}

function getAgeRecord() returns Age {
    Age a = {age: 99, format:"MM"};
    return a;
}
