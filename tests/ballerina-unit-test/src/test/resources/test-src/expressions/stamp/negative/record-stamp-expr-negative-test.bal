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

type Student record {
    string name;
    int age;
    string batch;
    !...
};

type Person record {
    string name;
    int age;
    string batch;
    string school;
    !...
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type TeacherObj object {
    string name = "Mohan";
    int age = 30;
    string status = "Single";
    string batch = "LK2014";
    string school = "VNC";
};

function stampRecordToXML() returns xml {

    Employee employeeRecord = { name: "Raja", age: 25, salary: 20000 };

    xml xmlValue = xml.stamp(employeeRecord);
    return xmlValue;
}

function stampOpenRecordToClosedRecord() returns Employee {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Employee employee = Employee.stamp(teacher);

    return employee;
}

function stampClosedRecordToClosedRecord() returns Student {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    Student student = Student.stamp(person);

    return student;
}

function stampRecordToObject() returns TeacherObj {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    TeacherObj returnValue = TeacherObj.stamp(teacher);

    return returnValue;
}

function stampClosedRecordToMap() returns map<string> {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = map<string>.stamp(person);

    return mapValue;
}

function stampRecordToArray() returns string[] {
    Employee e1 = { name: "Raja", age: 30, salary: 10000 };
    string[] stringArray = string[].stamp(e1);

    return stringArray;
}

function stampRecordToTuple() returns (string, string) {

    Employee e1 = { name: "Raja", age: 30, salary: 10000 };
    (string, string) tupleValue = (string, string).stamp(e1);

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

function stampExtendedRecordToAnydata() returns anydata {
    Address addressObj = new Address();
    ExtendedEmployee employee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    anydata anydataValue = anydata.stamp(employee);

    return anydataValue;
}
