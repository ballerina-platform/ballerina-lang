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
import ballerina/jballerina.java;

configurable int intVar = 5;
configurable byte byteVar = ?;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;
configurable decimal decimalVar = 10.1;
configurable () nilVar = ();

configurable int[] & readonly intArr = ?;
configurable byte[] & readonly byteArr = ?;
configurable float[] & readonly floatArr = [9.0, 5.3, 5.6];
configurable string[] & readonly stringArr = ["apple", "orange", "banana"];
configurable boolean[] & readonly booleanArr = [true, true];
configurable decimal[] & readonly decimalArr = ?;
configurable ()[] nilArr = [];

configurable int[][] & readonly int2DArr = ?;
configurable byte[][] & readonly byte2DArr = ?;
configurable float[][] & readonly float2DArr = ?;
configurable string[][] & readonly string2DArr = ?;
configurable boolean[][] & readonly boolean2DArr = ?;
configurable decimal[][] & readonly decimal2DArr = ?;
configurable ()[][] & readonly nil2DArr = [];
configurable int[][][] & readonly int3DArr = ?;

type CustomArrayType1 int[];

type CustomArrayType2 int[][];

configurable CustomArrayType1[] & readonly customType1Array = ?;
configurable CustomArrayType2 & readonly customType2Array = ?;
configurable CustomArrayType2[] & readonly customType3DArray = ?;

type AuthInfo record {|
    readonly string username;
    int id = 100;
    string password;
    string[] scopes?;
    boolean isAdmin = false;
    () nilField = ();
|};

type Employee record {|
    readonly int id;
    readonly string name = "Default";
    readonly float salary?;
|};

type Person readonly & record {|
    readonly string name;
    string address = "default address";
    int age?;
|};

type PersonInfo readonly & record {|
    string name;
    string address = "Colombo";
    int age?;
|};

type EmployeeInfo record {|
    int id;
    string name = "test";
    float salary?;
|};

type UserTable table<AuthInfo> key(username);

type EmployeeTable table<Employee> key(id) & readonly;

type PersonTable table<Person> key(name) & readonly;

type nonKeyTable table<AuthInfo>;

type PersonInfoTable table<PersonInfo> & readonly;

type EmpInfoTable table<EmployeeInfo>;

configurable AuthInfo & readonly admin = {username: "admin", password: "1234"};
configurable UserTable & readonly users = ?;
configurable PersonInfo personInfo = ?;
configurable EmployeeInfo & readonly empInfo = ?;
configurable EmployeeTable employees = ?;
configurable PersonTable people = ?;
configurable nonKeyTable & readonly nonKeyUsers = ?;
configurable PersonInfoTable peopleInfo = ?;
configurable EmpInfoTable & readonly empInfoTab = ?;

enum Colors {
    RED,
    GREEN
}

enum CountryCodes {
    SL = "Sri Lanka",
    US = "United States"
}

configurable Colors & readonly color = ?;
configurable CountryCodes & readonly countryCode = ?;

public function main() {
    testSimpleValues();
    testArrayValues();
    testMultiDimentionalArrayValues();
    testCustomArrayTypeValues();
    testRecordValues();
    testTableValues();
    testEnumValues();

    print("Tests passed");
}

function testSimpleValues() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("abc", stringVar);
    test:assertTrue(booleanVar);
    test:assertEquals((), nilVar);

    decimal result = 24.87;
    test:assertEquals(result, decimalVar);

    byte result2 = 22;
    test:assertEquals(byteVar, result2);
}

function testArrayValues() {
    test:assertEquals([1, 2, 3], intArr);
    test:assertEquals([9.0, 5.6], floatArr);
    test:assertEquals(["red", "yellow", "green"], stringArr);
    test:assertEquals([true, false, false, true], booleanArr);
    test:assertEquals([], nilArr);

    decimal[] & readonly resultArr = [8.9, 4.5, 6.2];
    test:assertEquals(resultArr, decimalArr);

    byte[] & readonly resultArr2 = [11, 22, 33, 44, 55, 66, 77, 88, 99];
    test:assertEquals(byteArr, resultArr2);
}

function testMultiDimentionalArrayValues() {
    test:assertEquals([[1, 2], [3, 4]], int2DArr);
    test:assertEquals([[9.0, 5.6], [4.1, 56.7]], float2DArr);
    test:assertEquals([["red", "yellow", "green"], ["white", "purple"]], string2DArr);
    test:assertEquals([[true, false], [false, true]], boolean2DArr);
    test:assertEquals([], nil2DArr);

    decimal[][] & readonly resultArr = [[8.9, 4.5, 6.2], [5.4, 8.5]];
    test:assertEquals(resultArr, decimal2DArr);

    byte[][] & readonly resultArr2 = [[11, 22, 33, 44], [55, 66, 77, 88, 99]];
    test:assertEquals(byte2DArr, resultArr2);
}

function testCustomArrayTypeValues() {
    test:assertEquals([[[1, 2], [3, 4]], [[1, 2], [3, 4]]], int3DArr);
    test:assertEquals([[1, 2], [3, 4]], customType1Array);
    test:assertEquals([[5, 6], [7, 8]], customType2Array);
    test:assertEquals([[[5, 6], [7, 8]], [[5, 6], [7, 8]]], customType3DArray);
}

function testRecordValues() {
    test:assertEquals("jack", admin.username);
    test:assertEquals("password", admin.password);
    test:assertEquals(["write", "read", "execute"], admin["scopes"]);
    test:assertEquals((), admin.nilField);
    test:assertTrue(admin.isAdmin);

    test:assertEquals("harry", personInfo.name);
    test:assertEquals("Colombo", personInfo.address);
    test:assertEquals(28, personInfo["age"]);

    test:assertEquals(34, empInfo.id);
    test:assertEquals("test", empInfo.name);
    test:assertEquals(75000.0, empInfo["salary"]);
}

function testTableValues() {

    test:assertEquals(3, users.length());
    test:assertEquals(3, nonKeyUsers.length());
    test:assertEquals(3, employees.length());
    test:assertEquals(3, people.length());
    test:assertEquals(3, peopleInfo.length());
    test:assertEquals(3, empInfoTab.length());

    AuthInfo & readonly user1 = {
        username: "alice",
        id: 11,
        password: "password1",
        scopes: ["write"]
    };

    AuthInfo & readonly user2 = {
        username: "bob",
        id: 22,
        password: "password2",
        scopes: ["write", "read"]
    };

    AuthInfo & readonly user3 = {
        username: "john",
        id: 33,
        password: "password3"
    };

    test:assertEquals(user1, users.get("alice"));
    test:assertEquals(user2, users.get("bob"));
    test:assertEquals(user3, users.get("john"));

    Employee emp1 = {
        id: 111,
        name: "anna"
    };

    Employee emp2 = {
        id: 222,
        name: "elsa",
        salary: 25000.0
    };

    Employee emp3 = {
        id: 333,
        name: "tom"
    };

    test:assertEquals(emp1, employees.get(111));
    test:assertEquals(emp2, employees.get(222));
    test:assertEquals(emp3, employees.get(333));

    Person person1 = {
        name: "alice",
        address: "London",
        age: 22
    };

    Person person2 = {name: "bob"};

    Person person3 = {
        name: "john",
        age: 25
    };

    test:assertEquals(person1, people.get("alice"));
    test:assertEquals(person2, people.get("bob"));
    test:assertEquals(person3, people.get("john"));

    testTableIterator(users);
    testTableIterator(nonKeyUsers);
    testTableIterator(employees);
    testTableIterator(people);
    testTableIterator(peopleInfo);
    testTableIterator(empInfoTab);
}

function testEnumValues() {
    test:assertEquals(color, GREEN);
    test:assertEquals(countryCode, US);
}

function testTableIterator(table<map<anydata>> tab) {
    int count = 0;
    foreach var entry in tab {
        count += 1;
    }
    test:assertEquals(3, count);
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
