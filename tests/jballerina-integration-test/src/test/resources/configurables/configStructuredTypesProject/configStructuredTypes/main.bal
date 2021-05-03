// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import configStructuredTypes.mod1;
import ballerina/jballerina.java;
import configStructuredTypes.mod2;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

public type Engineer record {|
    string name = "";
    int id = 555;
|};

type Lecturer record {|
    string name;
    Department department1;
    Department department2?;
    readonly Department department3;
|};

type Department readonly & record {|
    string name;
|};

type Lawyer readonly & record {|
    string name;
    Address address1;
    Address address2?;
    readonly Address address3;
|};

type Address record {|
    string city;
|};

configurable Engineer & readonly 'engineer = ?;
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;
configurable mod1:Student & readonly student = ?;
configurable mod1:Officer officer = ?;
configurable mod1:Employee employee = ?;

configurable table<Engineer> & readonly engineerTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Officer> & readonly officerTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

// Readonly records
configurable mod1:Employee & readonly employee1 = ?;
configurable table<mod1:Officer & readonly> & readonly officerTable1 = ?;
configurable table<mod1:Employee & readonly> & readonly employeeTable1 = ?;

// Complex records
configurable mod1:Person person = ?;
configurable mod1:Person person2 = ?;
configurable readonly & record {
    string name;
    int id;
    mod1:Address address = { city:"Galle" };
} person3 = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable mod1:Person[] & readonly personArray = ?;
configurable (mod1:Person & readonly)[] & readonly personArray1 = ?;

configurable Lecturer & readonly lecturer = ?;
configurable Lawyer lawyer = ?;
configurable mod1:Lecturer & readonly lecturer2 = ?;
configurable mod1:Lawyer lawyer2 = ?;
configurable table<Lecturer> & readonly lecturerTable = ?;
configurable table<Lawyer> & readonly lawyerTable = ?;
configurable table<mod1:Lecturer> & readonly lecturerTable2 = ?;
configurable table<mod1:Lawyer> & readonly lawyerTable2 = ?;

type PersonArray mod1:Person;

configurable PersonArray[] & readonly personArray2 = ?;

public function main() {
    testRecords();
    testTables();
    testArrays();
    mod2:testRecords();
    mod2:testTables();
    mod2:testArrays();
    testComplexRecords();
    print("Tests passed");
}

public function testRecords() {
    test:assertEquals(engineer.name, "waruna");
    test:assertEquals(engineer.id, 555);
    test:assertEquals(student.name, "riyafa");
    test:assertEquals(student.id, 444);
    test:assertEquals(employee.name, "manu");
    test:assertEquals(employee.id, 101);
    test:assertEquals(officer.name, "gabilan");
    test:assertEquals(officer.id, 101);
    test:assertEquals(manager.name, "hinduja");
    test:assertEquals(manager.id, 107);
    test:assertEquals(teacher.name, "gabilan");
    test:assertEquals(teacher.id, 888);
    test:assertEquals(farmer.name, "waruna");
    test:assertEquals(farmer.id, 999);
    test:assertEquals(person.name, "waruna");
    test:assertEquals(person.id, 10);
    test:assertEquals(person.address.city, "San Francisco");
    test:assertEquals(person.address.country.name, "USA");
    test:assertEquals(person2.name, "manu");
    test:assertEquals(person2.id, 11);
    test:assertEquals(person2.address.city, "Nugegoda");
    test:assertEquals(person2.address.country.name, "SL");
    test:assertEquals(person3.name, "riyafa");
    test:assertEquals(person3.id, 12);
    test:assertEquals(person3.address.city, "Galle");
    test:assertEquals(person3.address.country.name, "SL");
}

public function testComplexRecords() {
    test:assertEquals(lecturer.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer.toString(), "{\"name\":\"riyafa\",\"address1\":{\"city\":\"Colombo\"}," +
    "\"address2\":{\"city\":\"Kandy\"},\"address3\":{\"city\":\"Galle\"}}");
    test:assertEquals(lecturerTable.toString(), "[{\"name\":\"manu\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Sales\"},\"department3\":{\"name\":\"HR\"}}," +
    "{\"name\":\"waruna\",\"department1\":{\"name\":\"IT\"},\"department3\":{\"name\":\"HR\"}}]");
    test:assertEquals(lawyerTable.toString(), "[{\"name\":\"gabilan\",\"address1\":{\"city\":\"Colombo\"}," +
    "\"address2\":{\"city\":\"Kandy\"},\"address3\":{\"city\":\"Galle\"}}," +
     "{\"name\":\"riyafa\",\"address1\":{\"city\":\"Matara\"},\"address3\":{\"city\":\"Gampaha\"}}]");
    test:assertEquals(lecturer2.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer2.toString(), "{\"name\":\"riyafa\",\"place1\":{\"city\":\"Colombo\"}," +
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}}");
    test:assertEquals(lecturerTable2.toString(), "[{\"name\":\"manu\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Sales\"},\"department3\":{\"name\":\"HR\"}}," +
    "{\"name\":\"waruna\",\"department1\":{\"name\":\"IT\"},\"department3\":{\"name\":\"HR\"}}]");
    test:assertEquals(lawyerTable2.toString(), "[{\"name\":\"gabilan\",\"place1\":{\"city\":\"Colombo\"}," +
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}},{\"name\":\"riyafa\"," +
    "\"place1\":{\"city\":\"Matara\"},\"place3\":{\"city\":\"Jaffna\"}}]");
}

public function testTables() {
    test:assertEquals(engineerTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(employeeTable1.toString(), "[{\"name\":\"waruna\",\"id\":2},{\"name\":\"manu\",\"id\":7}]");
    test:assertEquals(officerTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(officerTable1.toString(), "[{\"name\":\"waruna\",\"id\":4},{\"name\":\"gabilan\",\"id\":5}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":101},{\"name\":\"riyafa\",\"id\":102}]");
    test:assertEquals(teacherTable.toString(), "[{\"name\":\"manu\",\"id\":77},{\"name\":\"riyafa\",\"id\":88}]");
    test:assertEquals(farmerTable.toString(), "[{\"name\":\"waruna\",\"id\":444},{\"name\":\"hinduja\",\"id\":888}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"riyafa\",\"id\":13," +
        "\"address\":{\"city\":\"Canberra\",\"country\":{\"name\":\"Australia\"}}},{\"name\":\"gabilan\"," +
        "\"id\":14,\"address\":{\"city\":\"Paris\",\"country\":{\"name\":\"France\"}}}]");
}

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"address\":{\"country\":{\"name\":\"USA\"},\"city\":\"New York\"}," +
        "\"name\":\"manu\",\"id\":11},{\"address\":{\"country\":{\"name\":\"UK\"},\"city\":\"London\"}," +
        "\"name\":\"hinduja\",\"id\":12}]");
    test:assertEquals(personArray1.toString(), "[{\"address\":{\"country\":{\"name\":\"UAE\"}," +
        "\"city\":\"Abu Dhabi\"},\"name\":\"waruna\",\"id\":700},{\"address\":{\"country\":{\"name\":\"India\"}," +
        "\"city\":\"Mumbai\"},\"name\":\"manu\",\"id\":701}]");
    test:assertEquals(personArray2.toString(), "[{\"address\":{\"country\":{\"name\":\"UAE\"}," +
        "\"city\":\"Abu Dhabi\"},\"name\":\"gabilan\",\"id\":900},{\"address\":{\"country\":{\"name\":\"India\"}," +
        "\"city\":\"Mumbai\"},\"name\":\"hinduja\",\"id\":901}]");
}

function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
