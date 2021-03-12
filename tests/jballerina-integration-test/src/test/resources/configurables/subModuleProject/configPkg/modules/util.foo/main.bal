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

public type Employee record {|
    readonly int id;
    string name = "abcd";
    int age?;
|};

public type EmployeeTable table<Employee> key(id);

public type NonKeyTable table<Employee>;

configurable int intVar = ?;
configurable float floatVar = 9.5;
configurable byte byteVar = 25;
configurable string stringVar = "hello";
configurable boolean booleanVar = ?;
configurable decimal decimalVar = 10.1;

configurable int[] & readonly intArr = ?;
configurable byte[] & readonly byteArr = ?;
configurable float[] & readonly floatArr = [9.0, 5.3, 5.6];
configurable string[] & readonly stringArr = ["apple", "orange", "banana"];
configurable boolean[] & readonly booleanArr = [true, true];
configurable decimal[] & readonly decimalArr = ?;

configurable Employee & readonly manager = ?;
configurable EmployeeTable & readonly employees = ?;
configurable NonKeyTable & readonly nonKeyEmployees = ?;

public function getAverage() returns float {
    return <float>(intVar + floatVar) / 2;
}

public function getString() returns string {
    return stringVar;
}

public function getBoolean() returns boolean {
    return booleanVar;
}

public function getDecimal() returns decimal {
    return decimalVar;
}

public function getByte() returns byte {
    return byteVar;
}

public function getIntArray() returns int[] {
    return intArr;
}

public function getFloatArray() returns float[] {
    return floatArr;
}

public function getByteArray() returns byte[] {
    return byteArr;
}

public function getStringArray() returns string[] {
    return stringArr;
}

public function getBooleanArray() returns boolean[] {
    return booleanArr;
}

public function getDecimalArray() returns decimal[] {
    return decimalArr;
}

public function getManager() returns Employee {
    return manager;
}

public function getEmployees() returns EmployeeTable {
    return employees;
}

public function getNonKeyEmployees() returns NonKeyTable {
    return nonKeyEmployees;
}
