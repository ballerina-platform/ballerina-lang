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
import ballerina/lang.'float;
import ballerina/lang.'int;
import ballerina/lang.'int as ints;

// This tests the functionality of overridden predeclared imports

@test:Config {}
function testImportTest1() {
    int x = <int> 'int:fromString("100");
    test:assertEquals(x, 100);
}

@test:Config {}
function testImportTest2() {
    float max = 'float:max(10.5, 0.5);
    test:assertEquals(max, 10.5);
}

@test:Config {}
function testImportTest3() {
    decimal d = 10.5;
    decimal max = 'decimal:max(10.5, 0.5);
    test:assertEquals(max, d);
}

@test:Config {}
function testImportTest4() {
    int x = <int> 'ints:fromString("100");
    test:assertEquals(x, 100);
}
