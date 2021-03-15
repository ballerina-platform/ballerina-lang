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
import configPkg.util.foo;

public function main() {

    //from imported module
    testSimpleValues();
    testArrayValues();
    testRecordValues();
    testTableValues();

    print("Tests passed");
}

function testSimpleValues() {
    test:assertEquals(foo:getAverage(), 17.6);
    test:assertEquals(foo:getString(), "world");
    test:assertEquals(foo:getBoolean(), false);

    decimal result = 5.6;
    test:assertEquals(foo:getDecimal(), result);

    byte result2 = 78;
    test:assertEquals(foo:getByte(), result2);
}

function testArrayValues() {
    test:assertEquals(foo:getIntArray(), [9, 8, 7]);
    test:assertEquals(foo:getFloatArray(), [3.2, 1.9]);
    test:assertEquals(foo:getStringArray(), ["aa", "bb", "cc"]);
    test:assertEquals(foo:getBooleanArray(), [true, false, false, true]);

    decimal[] & readonly resultArr = [9.8, 7.6, 5.4];
    test:assertEquals(foo:getDecimalArray(), resultArr);

    byte[] & readonly resultArr2 = [99, 88, 77, 66, 55, 44, 33, 22, 11];
    test:assertEquals(foo:getByteArray(), resultArr2);
}

function testRecordValues() {
    foo:Employee manager = foo:getManager();
    test:assertEquals(101, manager.id);
    test:assertEquals("John", manager.name);
    test:assertEquals(30, manager["age"]);
}

function testTableValues() {
    foo:EmployeeTable empTab = foo:getEmployees();
    foo:NonKeyTable nonKeyTab = foo:getNonKeyEmployees();
    test:assertEquals(3, empTab.length());
    test:assertEquals(3, nonKeyTab.length());

    foo:Employee emp1 = {
        id: 11,
        name: "Anna",
        age: 28
    };

    foo:Employee emp2 = {
        id: 22,
        name: "abcd",
        age: 32
    };

    foo:Employee emp3 = {
        id: 33,
        name: "Ben"
    };

    test:assertEquals(empTab.get(11), emp1);
    test:assertEquals(empTab.get(22), emp2);
    test:assertEquals(empTab.get(33), emp3);
}
