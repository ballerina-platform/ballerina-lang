// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Execute tests with Structral type value assertions

type EmployeeRow record {
    readonly int id;
    string name;
    float salary;
};

type EmployeeTable table<EmployeeRow> key(id);

@test:Config {}
function testAssertIntArrayEquals() {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertIntArrayEqualsNegative() {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 4];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertIntArrayNotEquals() {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 4];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertIntArrayNotEqualsNegative() {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringArrayEquals() {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertStringArrayEqualsNegative() {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "c"];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);

    y = ["A", "B", "D"];
    err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringArrayNotEquals() {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "c"];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertStringArrayNotEqualsNegative() {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatArrayEquals() {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertFloatArrayEqualsNegative() {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.0];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatArrayNotEquals() {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.0];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertFloatArrayNotEqualsNegative() {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertByteArrayEquals() {
    byte[] x = [1, 2, 3];
    byte[] y = [1, 2, 3];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertByteArrayEqualsNegative() {
    byte[] x = [1, 2, 3];
    byte[] y = [1, 2, 4];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertByteArrayNotEquals() {
    byte[] x = [1, 2, 3];
    byte[] y = [1, 2, 4];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertByteArrayNotEqualsNegative() {
    byte[] x = [1, 2, 3];
    byte[] y = [1, 2, 3];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTupleEquals() {
    [string|int, float, boolean] t1 = [1, 1.0, false];
    [int, float|string, boolean] t2 = [1, 1.0, false];
    test:assertEquals(t1, t2);
}

@test:Config {}
function testAssertTupleEqualsNegative() {
    [string|int, float, boolean] t1 = [1, 1.0, false];
    [int, float|string, boolean] t2 = [1, 1.1, false];
    error? err = trap test:assertEquals(t1, t2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTupleNotEquals() {
    [string|int, float, boolean] t1 = [1, 1.0, false];
    [int, float|string, boolean] t2 = [1, 1.1, false];
    test:assertNotEquals(t1, t2);
}

@test:Config {}
function testAssertTupleNotEqualsNegative() {
    [string|int, float, boolean] t1 = [1, 1.0, false];
    [int, float|string, boolean] t2 = [1, 1.0, false];
    error? err = trap test:assertNotEquals(t1, t2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTableEquals() {
    EmployeeTable employeeTab1 = table [
               {id: 1, name: "John", salary: 300.50},
               {id: 2, name: "Bella", salary: 500.50}
             ];
    EmployeeTable employeeTab2 = table [
                       {id: 1, name: "John", salary: 300.50},
                       {id: 2, name: "Bella", salary: 500.50}
                     ];
    test:assertEquals(employeeTab1, employeeTab2);
}

@test:Config {}
function testAssertTableEqualsNegative() {
    EmployeeTable employeeTab1 = table [
               {id: 1, name: "John", salary: 300.50},
               {id: 2, name: "Bella", salary: 500.50}
             ];
    EmployeeTable employeeTab2 = table [
                       {id: 1, name: "John", salary: 300.50},
                       {id: 2, name: "Greg", salary: 500.50}
                     ];
    error? err = trap test:assertEquals(employeeTab1, employeeTab2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTableNotEquals() {
    EmployeeTable employeeTab1 = table [
          {id: 1, name: "John", salary: 300.50},
          {id: 2, name: "Bella", salary: 500.50}
        ];
    EmployeeTable employeeTab2 = table [
              {id: 1, name: "John", salary: 300.50},
              {id: 3, name: "Greg", salary: 500.50}
            ];
    test:assertNotEquals(employeeTab1, employeeTab2);
}

@test:Config {}
function testAssertTableNotEqualsNegative() {
    EmployeeTable employeeTab1 = table [
              {id: 1, name: "John", salary: 300.50},
              {id: 2, name: "Bella", salary: 500.50}
            ];
    EmployeeTable employeeTab2 = table [
                      {id: 1, name: "John", salary: 300.50},
                      {id: 2, name: "Bella", salary: 500.50}
                    ];
    error? err = trap test:assertNotEquals(employeeTab1, employeeTab2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertRecordEquals() {
    EmployeeRow employee1 = {id: 1, name: "John", salary: 300.50};
    EmployeeRow employee2 = {id: 1, name: "John", salary: 300.50};
    test:assertEquals(employee1, employee2);
}

@test:Config {}
function testAssertRecordEqualsNegative() {
    EmployeeRow employee1 = {id: 1, name: "John", salary: 300.50};
    EmployeeRow employee2 = {id: 1, name: "Greg", salary: 300.50};
    error? err = trap test:assertEquals(employee1, employee2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertRecordNotEquals() {
    EmployeeRow employee1 = {id: 1, name: "John", salary: 300.50};
    EmployeeRow employee2 = {id: 1, name: "Greg", salary: 300.50};
    test:assertNotEquals(employee1, employee2);
}

@test:Config {}
function testAssertRecordNotEqualsNegative() {
    EmployeeRow employee1 = {id: 1, name: "John", salary: 300.50};
    EmployeeRow employee2 = {id: 1, name: "John", salary: 300.50};
    error? err = trap test:assertNotEquals(employee1, employee2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertMapEquals() {
    map<string> testMap1 = {code: "hello", action: "print"};
    map<string> testMap2 = {code: "hello", action: "print"};
    test:assertEquals(testMap1, testMap2);
}

@test:Config {}
function testAssertMapEqualsNegative() {
        map<string> testMap1 = {code: "hello", action: "print"};
    map<string> testMap2 = {code1: "hello", action: "print"};
    error? err = trap test:assertEquals(testMap1, testMap2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertMapNotEquals() {
    map<string> testMap1 = {code: "hello", action: "print"};
    map<string> testMap2 = {code1: "hello", action: "print"};
    test:assertNotEquals(testMap1, testMap2);
}

@test:Config {}
function testAssertMapNotEqualsNegative() {
    map<string> testMap1 = {code: "hello", action: "print"};
    map<string> testMap2 = {code: "hello", action: "print"};
    error? err = trap test:assertNotEquals(testMap1, testMap2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertErrorEquals() {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage");
    test:assertEquals(err1, err2);
}

@test:Config {}
function testAssertErrorEqualsNegative() {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage1");
    error? err = trap test:assertEquals(err1, err2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertErrorNotEquals() {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage1");
    test:assertNotEquals(err1, err2);
}

@test:Config {}
function testAssertErrorNotEqualsNegative() {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage");
    error? err = trap test:assertNotEquals(err1, err2);
    test:assertTrue(err is error);
}
