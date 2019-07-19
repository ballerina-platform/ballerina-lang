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

type Employee record {
    string name;
    int age;
    float salary;
};

type Student record {|
    string name;
    int age;
    string batch;
|};

type Person record {|
    string name;
    int age;
    string batch;
    string school;
|};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};



function stampRecordToXML() returns xml|error {

    Employee employeeRecord = { name: "Raja", age: 25, salary: 20000 };

    xml|error xmlValue = xml.constructFrom(employeeRecord);
    return xmlValue;
}

function stampOpenRecordToClosedRecord() returns Employee|error {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Employee|error employee = Employee.constructFrom(teacher);

    return employee;
}

function stampClosedRecordToClosedRecord() returns Student|error {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    Student|error student = Student.constructFrom(person);

    return student;
}

function stampClosedRecordToMap() returns map<string>|error {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    map<string>|error mapValue = map<string>.constructFrom(person);

    return mapValue;
}

function stampRecordToArray() returns string[]|error {
    Employee e1 = { name: "Raja", age: 30, salary: 10000 };
    string[]|error stringArray = string[].constructFrom(e1);

    return stringArray;
}

function stampRecordToTuple() returns [string, string]|error {

    Employee e1 = { name: "Raja", age: 30, salary: 10000 };
    [string, string]|error tupleValue = [string, string].constructFrom(e1);

    return tupleValue;
}

type ExtendedEmployee record {
    string name;
    string status;
    string batch;
    Address address;
};

type Address object {
    public int no = 10;
    public string streetName = "Palm Grove";
    public string city = "colombo";
};

