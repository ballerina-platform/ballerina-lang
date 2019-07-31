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

import ballerina/http;
import ballerina/log;
import ballerina/runtime;

@http:ServiceConfig {
    basePath: "/idle"
}
service idleTimeout on new http:Listener(9112, { timeoutInMillis: 1000, server: "Mysql" }) {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/timeout408"
    }
    resource function timeoutTest408(http:Caller caller, http:Request req) {
        var result = req.getTextPayload();
        if (result is string) {
            log:printInfo(result);
        } else  {
            log:printError("Error reading request", result);
        }
        var responseError = caller->respond("some");
        if (responseError is error) {
            log:printError("Error sending response", responseError);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/timeout500"
    }
    resource function timeoutTest500(http:Caller caller, http:Request req) {
        runtime:sleep(3000);
        var responseError = caller->respond("some");
        if (responseError is error) {
            log:printError("Error sending response", responseError);
        }
    }
}
