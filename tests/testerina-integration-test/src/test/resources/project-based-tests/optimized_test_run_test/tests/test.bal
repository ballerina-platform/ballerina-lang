// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import optimized_test_run_test.subModuleA;
import optimized_test_run_test.subModuleC;

import ballerina/test;

@test:Config {}
function simpleTest() {
    subModuleA:PublicTypeDef? importedTypeDef = ();
    _ = subModuleC:hello(());
    test:assertEquals(foo(), utilFunc().name);
}

@test:Config {}
function importUsageTest() {

}

function utilFunc() returns RecA {
    return {name: "hello"};
}
