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
import configTableType.imported_records;
import ballerina/test;

configurable table<Engineer> & readonly engineerTable = ?;
configurable table<Lecturer> & readonly lecturerTable = ?;
configurable table<Lawyer> & readonly lawyerTable = ?;
configurable StaffTable staffTable = ?;

// From non-default module
configurable table<type_defs:Student> & readonly studentTable = ?;
configurable table<type_defs:Officer> & readonly officerTable = ?;
configurable table<type_defs:Employee> & readonly employeeTable = ?;
configurable table<type_defs:Officer & readonly> & readonly officerTable1 = ?;
configurable table<type_defs:Employee & readonly> & readonly employeeTable1 = ?;
configurable table<type_defs:Lecturer> & readonly lecturerTable2 = ?;
configurable table<type_defs:Lawyer> & readonly lawyerTable2 = ?;
configurable type_defs:StaffTable staffTable1 = ?;

// Complex records
configurable table<type_defs:Person> & readonly personTable = ?;

// From non-default package
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable configLib:StaffTable staffTable2 = ?;

function testTables() {
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

    test:assertEquals(staffTable.toString(), "[{\"id\":222,\"name\":\"Default\",\"salary\":34000.0}," + 
    "{\"id\":333,\"name\":\"Jane\",\"salary\":45000.0}]");
    test:assertEquals(staffTable1.toString(), "[{\"id\":444,\"name\":\"Default\",\"salary\":56000.0}," + 
    "{\"id\":555,\"name\":\"Mary\",\"salary\":67000.0}]");
    test:assertEquals(staffTable2.toString(), "[{\"id\":666,\"name\":\"Default\",\"salary\":78000.0}," + 
    "{\"id\":777,\"name\":\"Amy\",\"salary\":89000.0}]");
}

public function main() {
    testTables();
    testTableIteration();
    imported_records:testTables();
    imported_records:testTableIteration();
    util:print("Tests passed");    
}

function testTableIteration() {
    util:testTableIterator(engineerTable);
    util:testTableIterator(studentTable);
    util:testTableIterator(employeeTable);
    util:testTableIterator(employeeTable1);
    util:testTableIterator(officerTable);
    util:testTableIterator(officerTable1);
    util:testTableIterator(managerTable);
    util:testTableIterator(teacherTable);
    util:testTableIterator(farmerTable);
    util:testTableIterator(personTable);
    util:testTableIterator(staffTable);
    util:testTableIterator(staffTable1);
    util:testTableIterator(staffTable2);
}
