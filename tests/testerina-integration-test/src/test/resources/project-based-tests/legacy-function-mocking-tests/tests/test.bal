// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import function_mocking_legacy.moduleA;
import ballerina/test;

@test:Mock {
    moduleName: "function_mocking_legacy",
    functionName: "intAdd"
}
function intAddMock(int a, int b) returns int {
    return 5;
}

@test:Mock {
    moduleName: "function_mocking_legacy.moduleA",
    functionName: "intSub"
}
function intSubMock(int a, int b) returns int {
    return 0;
}

@test:Mock {
    moduleName: "function_mocking_legacy.moduleB",
    functionName: "intMul"
}
function intMulMock(int a, int b) returns int {
    return 1;
}

@test:Config {}
function testIntAdd() {
    test:assertEquals(intAdd(3, 7), 5);
    test:assertEquals(moduleA:intSub(3, 7), 0);
}

@test:Config {}
function testIntMul() {
    test:assertEquals(intMul3Num(3, 7, 2), 1);
}
