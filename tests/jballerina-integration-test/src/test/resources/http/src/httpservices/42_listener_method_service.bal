// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

http:Listener backendEP = new(9252);
boolean listenerIdle = true;
boolean listenerStopped = false;


service startService on new http:Listener(9251) {
    resource function test(http:Caller caller, http:Request req) {
        checkpanic backendEP.__attach(mock1);
        checkpanic backendEP.__attach(mock2);
        checkpanic backendEP.__start();
        var result = caller->respond("Backend service started!");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

service mock1 =
@http:ServiceConfig {
    basePath: "/mock1"
} service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock1(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock1 invoked!");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};

service mock2 =
@http:ServiceConfig {
    basePath: "/mock2"
} service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock2(http:Caller caller, http:Request req) {
        checkpanic backendEP.__gracefulStop();
        runtime:sleep(2000);
        var responseToCaller = caller->respond("Mock2 invoked!");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};
