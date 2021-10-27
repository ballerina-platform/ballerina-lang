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

configurable Engineer & readonly 'engineer = ?;
configurable Lecturer & readonly lecturer = ?;
configurable Lawyer lawyer = ?;
configurable readonly & record {
    string name;
    int id;
    type_defs:Address address = {city: "Galle"};
} person3 = ?;

//From non-default modules
configurable type_defs:Student & readonly student = ?;
configurable type_defs:Officer officer = ?;
configurable type_defs:Employee employee = ?;
configurable type_defs:Employee & readonly employee1 = ?;
configurable type_defs:Lecturer & readonly lecturer2 = ?;
configurable type_defs:Lawyer lawyer2 = ?;

// Complex records
configurable type_defs:Person person = ?;
configurable type_defs:Person person2 = ?;

//From non-default packages
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;

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

public function testRecordIteration() {
    util:testRecordIterator(engineer, 2);
    util:testRecordIterator(student, 2);
    util:testRecordIterator(officer, 2);
    util:testRecordIterator(manager, 2);
    util:testRecordIterator(teacher, 2);
    util:testRecordIterator(farmer, 2);
    util:testRecordIterator(person, 3);
    util:testRecordIterator(person2, 3);
    util:testRecordIterator(person3, 3);
}

public function testComplexRecords() {
    test:assertEquals(lecturer.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," + 
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer.toString(), "{\"name\":\"riyafa\",\"address1\":{\"city\":\"Colombo\"}," + 
    "\"address2\":{\"city\":\"Kandy\"},\"address3\":{\"city\":\"Galle\"}}");
    test:assertEquals(lecturer2.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," + 
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer2.toString(), "{\"name\":\"riyafa\",\"place1\":{\"city\":\"Colombo\"}," + 
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}}");
 }

public function testComplexRecordIteration() {
    util:testRecordIterator(lecturer, 4);
    util:testRecordIterator(lecturer2, 4);
    util:testRecordIterator(lawyer, 4);
    util:testRecordIterator(lawyer2, 4);
}
