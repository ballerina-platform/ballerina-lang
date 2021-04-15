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
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

public type Doctor record {|
    string name = "";
    int id = 555;
|};

configurable Doctor & readonly doctor = ?;
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;
configurable mod1:Student & readonly student = ?;
configurable mod1:Officer officer = ?;
configurable mod1:Employee employee = ?;

configurable table<Doctor> & readonly doctorTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Officer> & readonly officerTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

configurable mod1:Employee & readonly employee1 = ?;
configurable table<mod1:Officer & readonly> & readonly officerTable1 = ?;
configurable table<mod1:Employee & readonly> & readonly employeeTable1 = ?;

// Complex records
configurable mod1:Person person = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable mod1:Person[] & readonly personArray = ?;

public function testRecords() {
    test:assertEquals(doctor.name, "waruna");
    test:assertEquals(doctor.id, 555);
    test:assertEquals(student.name, "riyafa");
    test:assertEquals(student.id, 444);
    test:assertEquals(employee.name, "manu");
    test:assertEquals(employee.id, 101);
    test:assertEquals(employee1.name, "waruna");
    test:assertEquals(employee1.id, 404);
    test:assertEquals(officer.name, "gabilan");
    test:assertEquals(officer.id, 101);
    test:assertEquals(manager.name, "hinduja");
    test:assertEquals(manager.id, 107);
    test:assertEquals(teacher.name, "hinduja");
    test:assertEquals(teacher.id, 11);
    test:assertEquals(farmer.name, "manu");
    test:assertEquals(farmer.id, 22);
    test:assertEquals(person.name, "hinduja");
    test:assertEquals(person.id, 100);
    test:assertEquals(person.address.city, "Kandy");
    test:assertEquals(person.address.country.name, "Sri Lanka");
}

public function testTables() {
    test:assertEquals(doctorTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(employeeTable1.toString(), "[{\"name\":\"gabilan\",\"id\":2},{\"name\":\"riyafa\",\"id\":3}]");
    test:assertEquals(officerTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(officerTable1.toString(), "[{\"name\":\"hinduja\",\"id\":7},{\"name\":\"waruna\",\"id\":8}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":101},{\"name\":\"riyafa\",\"id\":102}]");
    test:assertEquals(teacherTable.toString(), "[{\"name\":\"gabilan\",\"id\":66},{\"name\":\"riyafa\",\"id\":77}]");
    test:assertEquals(farmerTable.toString(), "[{\"name\":\"riyafa\",\"id\":555},{\"name\":\"hinduja\",\"id\":666}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"gabilan\",\"id\":133," +
        "\"address\":{\"country\":{\"name\":\"Germany\"},\"city\":\"Berlin\"}},{\"name\":\"riyafa\"," +
        "\"id\":144,\"address\":{\"country\":{\"name\":\"Spain\"},\"city\":\"Madrid\"}}]");
}

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"address\":{\"country\":{\"name\":\"UAE\"},\"city\":\"Abu Dhabi\"}," +
        "\"name\":\"waruna\",\"id\":111},{\"address\":{\"country\":{\"name\":\"India\"},\"city\":\"Mumbai\"}," +
        "\"name\":\"manu\",\"id\":122}]");
}
