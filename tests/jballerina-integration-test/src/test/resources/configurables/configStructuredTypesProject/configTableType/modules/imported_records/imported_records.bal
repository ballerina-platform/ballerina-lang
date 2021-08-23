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

import configTableType.type_defs;
import testOrg/configLib.mod1 as configLib;
import testOrg/configLib.util;
import ballerina/test;

public type Doctor record {|
    string name = "";
    int id = 555;
|};

configurable table<Doctor> & readonly doctorTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable table<type_defs:Student> & readonly studentTable = ?;
configurable table<type_defs:Officer> & readonly officerTable = ?;
configurable table<type_defs:Employee> & readonly employeeTable = ?;

configurable table<type_defs:Officer & readonly> & readonly officerTable1 = ?;
configurable table<type_defs:Employee & readonly> & readonly employeeTable1 = ?;

configurable table<type_defs:Person> & readonly personTable = ?;

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
        "\"address\":{\"city\":\"Berlin\",\"country\":{\"name\":\"Germany\"}}},{\"name\":\"riyafa\"," + 
        "\"id\":144,\"address\":{\"city\":\"Madrid\",\"country\":{\"name\":\"Spain\"}}}]");
}

public function testTableIteration() {
    util:testTableIterator(doctorTable);
    util:testTableIterator(studentTable);
    util:testTableIterator(employeeTable);
    util:testTableIterator(employeeTable1);
    util:testTableIterator(officerTable);
    util:testTableIterator(officerTable1);
    util:testTableIterator(managerTable);
    util:testTableIterator(teacherTable);
    util:testTableIterator(farmerTable);
    util:testTableIterator(personTable);
}
