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
    int id;
    string name;
    float salary;
};

type EmployeeIncompatible record {
    float id;
    string name;
    float salary;
};

type EmployeeSalary record {
    int id;
    float salary;
};

type EmployeeSalaryIncompatible record {
    float id;
    float salary;
};

function getSalaryInCompatibleInput(EmployeeIncompatible e) returns (EmployeeSalary) {
    EmployeeSalary s = { id: e.id, salary: e.salary };
    return s;
}

function getSalaryIncompatibleOutput(Employee e) returns (EmployeeSalaryIncompatible) {
    EmployeeSalaryIncompatible s = { id: e.id, salary: e.salary };
    return s;
}

function getSalaryIncompatibleInputOutput(EmployeeIncompatible e) returns (EmployeeSalaryIncompatible) {
    EmployeeSalaryIncompatible s = { id: e.id, salary: e.salary };
    return s;
}

function createTable() returns (table<Employee>) {
    table<Employee> dt = table{};

    Employee e1 = { id: 1, name: "A", salary: 100.0 };
    Employee e2 = { id: 2, name: "B", salary: 200.0 };
    Employee e3 = { id: 3, name: "C", salary: 300.0 };

    _ = dt.add(e1);
    _ = dt.add(e2);
    _ = dt.add(e3);

    return dt;
}

function testInCompatibleInput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryInCompatibleInput);
    return t;
}

function testIncompatibleOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryIncompatibleOutput);
    return t;
}

function testIncompatibleInputOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryIncompatibleInputOutput);
    return t;
}
