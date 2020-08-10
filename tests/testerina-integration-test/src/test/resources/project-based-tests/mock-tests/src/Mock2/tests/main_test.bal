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
import ballerina/http;
import ballerina/io;

//
// MOCK FUNCTION OBJECTS
//

@test:Mock {
    functionName : "intAdd"
}
test:MockFunction mock1 = new();

//
// MOCK FUNCTIONS
//

public function mockIntAdd(int a, int b) returns (int) {
    return a - b;
}

//
// TESTS
//

http:Client clientEP = new("http://localhost:9090");

@test:Config {}
function service_Test1() {
    io:println("[serivce_Test1] Testing mock functionality within a service");
    http:Request req = new();
    test:when(mock1).call("mockIntAdd");
    http:Response|error res = clientEP->get("/addService/add");

    if (res is http:Response) {
        var payload = res.getJsonPayload();
       if (payload is json) {
           int value = <int>payload.value;
           test:assertEquals(value, 4);
       }
    } else {
        test:assertFail("Fail");
    }
}

@test:Config{}
function call_Test() {
    io:println("[call_Test] Testing mock functionality in separate module");
    test:when(mock1).call("mockIntAdd");
    int val = callIntAdd(7, 3);
    test:assertEquals(val, 4);
}
