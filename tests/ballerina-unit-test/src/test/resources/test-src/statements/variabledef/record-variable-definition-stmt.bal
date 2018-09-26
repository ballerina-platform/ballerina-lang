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
//
//    Employee {name, address: (number, street)} = {name: "John", address: (20, "PG")};
//
//    return (name, number, street);
//}
