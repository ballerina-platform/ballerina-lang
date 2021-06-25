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

import ballerina/test;
import ballerina/jballerina.java;
import main.empTable as emp;

type Employee record {|
    readonly int empId;
    string name;
    float salary = 200000.0;
    boolean isPermanent?;
|};

type EmployeeTable table<Employee> key(empId);

configurable EmployeeTable & readonly empTable = ?;

public function main() {
    testTableValues();
    testImport();
    print("Tests passed");
}

function testTableValues() {

    test:assertEquals(3, empTable.length());

    Employee & readonly emp1 = {
        empId: 101,
        name: "Alice",
        salary: 120000.0,
        isPermanent: false
    };

    Employee & readonly emp2 = {
        empId: 102,
        name: "Bob",
        salary: 200000.0,
        isPermanent: true
    };

    Employee & readonly emp3 = {
        empId: 103,
        name: "Edward",
        salary: 175000.0
    };

    test:assertEquals(empTable.get(101), emp1);
    test:assertEquals(empTable.get(102), emp2);
    test:assertEquals(empTable.get(103), emp3);
}

function testImport() {
    float gSalary = emp:calculateGrossSalary(100000.0);
    test:assertEquals(gSalary, 90000.0);
    
}

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
