// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

@test:Mock {
    functionName: "dependencyFunction1"
}
test:MockFunction mockFn = new();

function mockDependencyFunction1() returns string {
    return "Hello from dependency mocking function 1";
}

@test:Mock {
    functionName: "dependencyFunction2"
}
function mockDependencyFunction2Legacy() returns string {
    return "Hello from dependency mocking function 2";
}

@test:Config
function test1() {
    test:when(mockFn).call("mockDependencyFunction1");
    test:assertEquals(dependencyFunction1(), "Hello from dependency mocking function 1");
    test:assertEquals(dependencyFunction2(), "Hello from dependency mocking function 2");
}
