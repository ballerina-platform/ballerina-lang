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

import configRecordType.type_defs;
import testOrg/configLib.mod1 as configLib;
import testOrg/configLib.util;
import ballerina/test;

public type Doctor record {|
    string name = "";
    int id = 555;
|};

configurable Doctor & readonly doctor = ?;

configurable type_defs:Student & readonly student = ?;
configurable type_defs:Officer officer = ?;
configurable type_defs:Employee employee = ?;
configurable type_defs:Employee & readonly employee1 = ?;
configurable type_defs:Person person = ?;

configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;

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

public function testRecordIteration() {
    util:testRecordIterator(doctor, 2);
    util:testRecordIterator(student, 2);
    util:testRecordIterator(employee, 2);
    util:testRecordIterator(employee1, 2);
    util:testRecordIterator(officer, 2);
    util:testRecordIterator(manager, 2);
    util:testRecordIterator(teacher, 2);
    util:testRecordIterator(farmer, 2);
    util:testRecordIterator(person, 3);
}
