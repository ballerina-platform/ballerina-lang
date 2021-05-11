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

import a_b/foo;
import b_foo.b_foo;
import ballerina/test;
import ballerina/jballerina.java;

public function add(int a, int b) returns int {
    return a + b + 5;
}

public type Department record {
    string name = "IT";
};

public readonly class Employee {
    public Department dept;
    public int id;

    public function init(Department & readonly dept, int id) {
        self.dept = dept;
        self.id = id;
    }
}

function testPackageIDClash() {
    int result1 = add(2, 3);
    int result2 = foo:add(2, 3);
    int result3 = b_foo:add(2, 3);

    test:assertEquals(result1, 10);
    test:assertEquals(result2, 15);
    test:assertEquals(result3, 30);
}

function testImmutableTypeIL() {
    Employee emp = new ({}, 1234);
    object {
        public Department dept;
        public int id;
    } obj = emp;

    foo:Employee emp1 = new ({}, 1234);
    object {
        public foo:Department dept;
        public int id;
    } obj1 = emp1;

    Employee emp2 = new({}, 1234);
    object {
        public Department dept;
        public int id;
    } obj2 = emp2;

    test:assertTrue(<any>obj.dept is readonly);
    test:assertTrue(obj.dept is Department & readonly);
    test:assertEquals(<Department>{name: "IT"}, obj.dept);
    test:assertEquals(1234, obj.id);

    test:assertTrue(<any>obj1.dept is readonly);
    test:assertTrue(obj1.dept is foo:Department & readonly);
    test:assertEquals(<foo:Department>{name: "IT"}, obj1.dept);
    test:assertEquals(1234, obj1.id);

    test:assertTrue(<any> obj2.dept is readonly);
    test:assertTrue(obj2.dept is Department & readonly);
    test:assertEquals(<Department> {name: "IT"}, obj2.dept);
    test:assertEquals(1234, obj2.id);
}

public function main() {
    testPackageIDClash();
    testImmutableTypeIL();
    print("Tests passed");
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
