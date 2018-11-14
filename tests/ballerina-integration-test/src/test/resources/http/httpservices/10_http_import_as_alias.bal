// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http as network;
import ballerina/io;

endpoint network:Listener echoEP5 {
    port:9101
};

@network:ServiceConfig {
    basePath:"/echo"
}
service<network:Service> echo4 bind echoEP5 {

    @network:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo4 (endpoint caller, network:Request req) {
        var payload = req.getTextPayload();
        network:Response resp = new;
        if (payload is string) {
            _ = caller -> respond(untaint payload);
        } else if (payload is error) {
            resp.statusCode = 500;
            resp.setPayload(untaint payload.reason());
            log:printError("Failed to retrieve payload from request: " + payload.reason());
            var responseError = caller->respond(resp);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        }
    }
}
