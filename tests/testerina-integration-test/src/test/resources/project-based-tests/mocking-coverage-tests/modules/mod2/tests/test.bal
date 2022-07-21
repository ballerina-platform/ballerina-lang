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

import ballerina/test;
import mocking_coverage.mod1;
import testOrg/mockLib.mod1 as mockLib;

public function mockFloatAdd(float a, float b) returns float {
    return 5.0;
}

public function mockIntAdd(int a, int b) returns int {
    return 123;
}

public function mockIntMultiply(int a, int b) returns int {
    return 100;
}

@test:Mock {
    functionName: "floatAdd",
    moduleName: "mocking_coverage.mod2"
}
test:MockFunction mock_floatAdd = new();

@test:Mock {
    functionName: "intAdd",
    moduleName: "mocking_coverage.mod1"
}
test:MockFunction mock_intAdd = new();

@test:Mock {
    functionName: "intMultiply",
    moduleName: "testOrg/mockLib.mod1"
}
test:MockFunction mock_intMultiply = new();

@test:Config {}
function testStringAdd() {
    test:assertEquals(stringAdd("Ballerina"), "Hello Ballerina");
    test:assertEquals(byteAdd(2, 4), 6);
}

@test:Config {}
function testFloatAdd() {
    test:when(mock_floatAdd).call("mockFloatAdd");
    test:when(mock_intAdd).call("mockIntAdd");
    test:when(mock_intMultiply).call("mockIntMultiply");
    test:assertEquals(floatAdd(2, 5), 5.0);
    test:assertEquals(mod1:intAdd(20, 5), 123);
    test:assertEquals(mod1:decimalAdd(5.4, 4.5), 9.9d);
    test:assertEquals(mockLib:intMultiply(5, 5), 100);
}
