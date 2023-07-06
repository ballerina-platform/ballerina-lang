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

public isolated client class TestClient {
    remote function getMessage() returns string {
      return "Test Client";
    }
}

@test:Config {}
public function testAsyncRemoteFunctionMain() returns (error?) {
    MainClient 'client = new MainClient();
    future<string> message = start 'client->getMessage();
    string|error msg1 = wait message;
    test:assertEquals(msg1, "Main Client");
}

@test:Config {}
public function testAsyncRemoteFunctionTest() returns (error?) {
    TestClient 'client = new TestClient();
    future<string> message = start 'client->getMessage();
    string|error msg1 = wait message;
    test:assertEquals(msg1, "Test Client");
}
