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
    functionName: "dependentFunction1"
}
test:MockFunction mockFn = new();

function mockDependentFunction1() returns string {
    return "Hello from dependent mocking function 1";
}

@test:Mock {
    functionName: "dependentFunction2"
}
function mockDependentFunction2Legacy() returns string {
    return "Hello from dependent mocking function 2";
}

@test:Config
function test1() {
    test:when(mockFn).call("mockDependentFunction1");
    test:assertEquals(dependentFunction1(), "Hello from dependent mocking function 1");
    test:assertEquals(dependentFunction2(), "Hello from dependent mocking function 2");
}
