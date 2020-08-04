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
import ballerina/test;
import ballerina/log;
import ballerina/io;

service helloTest on new http:Listener(9092) {
    resource function sayHello(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello, Test!");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

# Test service
@test:Config {

}
function testFunction() {
    http:Client clientModule = new ("http://localhost:9090");
    http:Client clientTest = new ("http://localhost:9092");
    var respModule = clientModule->get("/helloModule/sayHello");
    var respTest = clientTest->get("/helloTest/sayHello");

    if (respModule is http:Response) {
        var payload = respModule.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            io:println(payload.message());
        }
    } else {
        io:println(respModule.message());
    }

    if (respTest is http:Response) {
        var payload = respTest.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            io:println(payload.message());
        }
    } else {
        io:println(respTest.message());
    }
}

