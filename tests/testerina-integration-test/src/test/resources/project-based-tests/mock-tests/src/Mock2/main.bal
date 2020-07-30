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

import ballerina/http;

@http:ServiceConfig {
    basePath : "/addService"
}
service addService on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/add"
    }
    resource function add(http:Caller caller, http:Request req) returns error? {
        http:Response response  = new;
        int val = intAdd(10, 6);
        json payload = {
            value : val
        };
        response.setJsonPayload(payload);
        check caller->respond(response);
    }
}

// Function to be mocked
public function intAdd(int a, int b) returns (int) {
    return a + b;
}

// Function calling the original
public function callIntAdd(int a, int b) returns (int) {
    return intAdd(a, b);
}

// Function to be mocked from another module
public function intAdd2(int a, int b) returns (int) {
    return a + b;
}
