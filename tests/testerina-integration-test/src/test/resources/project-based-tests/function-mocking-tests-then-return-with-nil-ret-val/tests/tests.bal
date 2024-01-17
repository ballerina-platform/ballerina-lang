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

@test:Mock { functionName: "f1"}
test:MockFunction mockFn1 = new();

@test:Mock { functionName: "f2"}
test:MockFunction mockFn2 = new();

@test:Mock { functionName: "f3"}
test:MockFunction mockFn3 = new();

@test:Config {}
function testFunctionMock() {
    test:when(mockFn1).thenReturn(());
    test:assertEquals(f1(), ());
}

@test:Config {}
function testFunctionMock2() {
    test:when(mockFn2).doNothing();
    test:assertEquals(f2(), ());
}

@test:Config {}
function testFunctionMock3() {
    // no return value set
    test:assertEquals(f3(), ());
}
