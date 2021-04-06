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

configurable Engineer & readonly 'engineer = ?;
configurable configLib:Manager & readonly manager = ?;
configurable mod1:Student & readonly student = ?;
configurable mod1:Person & readonly person = ?;
configurable mod1:Employee employee = ?;

configurable table<Engineer> & readonly engineerTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

public function main() {
    testRecords();
    testTables();
    mod2:testRecords();
    mod2:testTables();
    print("Tests passed");
}
public function testRecords() {
    test:assertEquals(engineer.name, "waruna");
    test:assertEquals(engineer.id, 555);
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
    test:assertEquals(engineerTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":1001},{\"name\":\"riyafa\",\"id\":1002}]");
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
