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
    basePath:"/test-service"
}
service testServiceTwo on new http:Listener(9092) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-1"
    }
    resource function resourceOne(http:Caller caller, http:Request clientRequest) {
        http:Client httpEndpoint = new("http://localhost:10011/echo-service", {
            cache: {
                enabled: false
            }
        });
        string messageForEcho = "Hello Echo !";
        http:Response response = checkpanic httpEndpoint->get("/echo/" + messageForEcho);
        var payload = response.getTextPayload();
        if (payload != "Received: " + messageForEcho) {
            error err = error("invalid payload from echo backend");
            panic err;
        }

        http:Response outResponse = new;
        outResponse.setTextPayload("Hello, World! from resource one");
        checkpanic caller->respond(outResponse);
    }
}
