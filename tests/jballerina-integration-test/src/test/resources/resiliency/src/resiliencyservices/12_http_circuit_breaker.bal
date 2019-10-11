// Copyright (c) 2018 WSO2 Inc. (//www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// //www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

import ballerina/http;
import ballerina/log;

listener http:Listener circuitBreakerEP05 = new(9311);

http:ClientConfiguration conf05 = {
    circuitBreaker: {
        rollingWindow: {
            timeWindowInMillis: 60000,
            bucketSizeInMillis: 20000,
            requestVolumeThreshold: 3
        },
        failureThreshold: 0.3,
        resetTimeInMillis: 1000,
        statusCodes: [501, 502, 503]
    },
    timeoutInMillis: 2000
};

http:Client backendClientEP05 = new("http://localhost:8091", conf05);

@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker05 on circuitBreakerEP05 {

    @http:ResourceConfig {
        path: "/statuscode"
    }
    resource function getState(http:Caller caller, http:Request request) {
        var backendRes = backendClientEP05->forward("/statuscode", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = backendRes;
            http:Response response = new;
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            string errCause = <string> err.detail()?.message;
            response.setPayload(errCause);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

@http:ServiceConfig { basePath: "/statuscode" }
service statuscodeservice on new http:Listener(8091) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = http:STATUS_SERVICE_UNAVAILABLE;
        res.setPayload("Service unavailable.");
        var responseToCaller = caller->respond(res);
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}
