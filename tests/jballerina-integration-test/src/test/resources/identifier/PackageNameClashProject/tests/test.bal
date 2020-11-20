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
import a_b/foo;
import a/main.b_foo;

@test:Config {
}
function testPackageIDClash() {
    int result1 = add(2,3);
    int result2 = foo:add(2,3);
    int result3 = b_foo:add(2,3);

    test:assertEquals(result1, 10);
    test:assertEquals(result2, 15);
    test:assertEquals(result3, 30);
}

@test:Config {
}
function testImmutableTypeIL() {
    b_foo:Employee emp = new({}, 1234);
    object {
        public b_foo:Department dept;
        public int id;
    } obj = emp;

    foo:Employee emp1 = new({}, 1234);
    object {
        public foo:Department dept;
        public int id;
    } obj1 = emp1;

    Employee emp2 = new({}, 1234);
    object {
        public Department dept;
        public int id;
    } obj2 = emp2;

    test:assertTrue(<any> obj.dept is readonly);
    test:assertTrue(obj.dept is b_foo:Department & readonly);
    test:assertEquals(<b_foo:Department> {name: "IT"}, obj.dept);
    test:assertEquals(1234, obj.id);

    test:assertTrue(<any> obj1.dept is readonly);
    test:assertTrue(obj1.dept is foo:Department & readonly);
    test:assertEquals(<foo:Department> {name: "IT"}, obj1.dept);
    test:assertEquals(1234, obj1.id);

    test:assertTrue(<any> obj2.dept is readonly);
    test:assertTrue(obj2.dept is Department & readonly);
    test:assertEquals(<Department> {name: "IT"}, obj2.dept);
    test:assertEquals(1234, obj2.id);
}
