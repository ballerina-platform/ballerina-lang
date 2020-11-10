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

class PersonObj {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
}

class EmployeeObj {
    public int age = 10;
    public string name = "raj";

}

type Employee record {
    string name;
    int age;
};

class EmployeeObject {
    string name = "Mohan";
    string status = "Single";
    string batch = "LK2014";
}

class IntObject {
    string firstName = "Mohan";
    string lastName = "Raj";
}

class TeacherObj {
    string name = "Mohan";
    int age = 30;
    string status = "Single";
    string batch = "LK2014";
    string school = "VNC";
}

class BookObject {
    string book = "XYZ";
}


type ExtendedEmployee record {
    string name;
    string status;
    string batch;
    Address address;
};

class Address {
    public int no = 10;
    public string streetName = "Palm Grove";
    public string city = "colombo";
}

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

//----------------------------Object Stamp Negative Test Cases -------------------------------------------------------------

function stampObjectsToRecord() returns Employee|error {
    PersonObj p = new PersonObj();
    Employee|error employee = p.cloneWithType(Employee);

    return employee;
}

function stampObjectsToJSON() returns json|error {
    PersonObj p = new PersonObj();
    json|error jsonValue = p.cloneWithType(json);

    return jsonValue;
}

function stampObjectsToXML() returns xml|error {
    PersonObj p = new PersonObj();
    xml|error xmlValue = p.cloneWithType(xml);

    return xmlValue;
}

function stampObjectsToMap() returns map<any>|error {
    PersonObj p = new PersonObj();
    map<any>|error mapValue = p.cloneWithType(AnyMap);

    return mapValue;
}

function stampObjectsToArray() returns any[]|error {
    PersonObj p = new PersonObj();
    any[]|error anyValue = p.cloneWithType(AnyArray);

    return anyValue;
}

function stampObjectsToTuple() returns [int,string]|error {
    PersonObj p = new PersonObj();
    [int, string]|error tupleValue = p.cloneWithType(IntString);

    return tupleValue;
}

function stampAnyToObject() returns PersonObj|error {

    anydata anydataValue = new PersonObj();
    PersonObj|error personObj = anydataValue.cloneWithType(PersonObj);

    return personObj;
}

function stampAnyArrayToObject() returns EmployeeObject|error {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    EmployeeObject|error objectValue = anyArray.cloneWithType(EmployeeObject);

    return objectValue;
}

function stampJSONToObject() returns EmployeeObj|error {

    json employee = { name: "John", status: "Single", batch: "LK2014" };
    EmployeeObject|error employeeObj = employee.cloneWithType(EmployeeObject);
    return employeeObj;
}

function stampXMLToObject() returns BookObject|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookObject|error objectValue = xmlValue.cloneWithType(BookObject);
    return objectValue;
}

function stampMapToObject() returns IntObject|error {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    IntObject|error objectValue = m.cloneWithType(IntObject);

    return objectValue;
}

function stampRecordToObject() returns TeacherObj|error {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    TeacherObj|error returnValue = teacher.cloneWithType(TeacherObj);

    return returnValue;
}

function stampTupleToObject() returns EmployeeObj|error {
    [string, int] tupleValue = ["Mohan", 30];

    EmployeeObj|error objectValue = tupleValue.cloneWithType(EmployeeObj);
    return objectValue;
}

type AnyMap map<any>;
type IntString [int, string];
type AnyArray any[];
