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
configurable mod1:Student & readonly student = ?;
configurable mod1:Person & readonly person = ?;
configurable mod1:Employee employee = ?;

configurable table<Doctor> & readonly doctorTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

public function testRecords() {
    test:assertEquals(doctor.name, "waruna");
    test:assertEquals(doctor.id, 555);
    test:assertEquals(student.name, "riyafa");
    test:assertEquals(student.id, 444);
    test:assertEquals(employee.name, "manu");
    test:assertEquals(employee.id, 101);
    test:assertEquals(person.name, "gabilan");
    test:assertEquals(person.id, 101);
    test:assertEquals(manager.name, "hinduja");
    test:assertEquals(manager.id, 107);
}

public function testTables() {
    test:assertEquals(doctorTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":1001},{\"name\":\"riyafa\",\"id\":1002}]");
}
