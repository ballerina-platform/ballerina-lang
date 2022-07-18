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

public function mockFloatAdd(float a, float b) returns float {
    return 5;
}

@test:Mock {
    functionName: "floatAdd",
    moduleName: "mocking_coverage"
}
test:MockFunction mock_floatAdd = new();

@test:Config {}
function testStringAdd() {
    test:assertEquals(stringAdd("Ballerina"), "Hello Ballerina");
    test:assertEquals(byteAdd(2, 4), 6);
}

@test:Config {}
function testFloatAdd() {
    test:when(mock_floatAdd).call("mockFloatAdd");
    test:assertEquals(floatAdd(2, 5), 5.0);
    test:assertEquals(intAdd(2, 5), 7);

}
