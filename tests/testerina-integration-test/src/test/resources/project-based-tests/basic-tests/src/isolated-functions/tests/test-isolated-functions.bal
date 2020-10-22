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

string[] outputs = [];
int counter = 0;

@test:Config {}
function testCallingIsolatedFunction() {
    test:assertEquals(bar(), 34);
}

@test:Config {}
isolated function testIsolatedTestFunction() {
    test:assertTrue(true);
}

@test:Mock {
    functionName: "print"
}
public function mockPrint(string s) {
    outputs[counter] = s;
    counter += 1;
}

@test:Config {}
function testFunc() {
    main();
    test:assertEquals(outputs[0], "hello-isolated");
    test:assertEquals(outputs[1], "hello-non-isolated");
}
