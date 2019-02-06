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
import ballerina/runtime;

listener http:Listener circuitBreakerEP03 = new(9309);

http:ClientEndpointConfig conf03 = {
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 60000,
            bucketSizeMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.2,
        resetTimeMillis: 1000,
        statusCodes: [501, 502, 503]
    },
    timeoutMillis: 2000
};

http:Client simpleClientEP = new("http://localhost:8089", config = conf03);

@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker03 on circuitBreakerEP03 {

    @http:ResourceConfig {
        path: "/getstate"
    }
    resource function getState(http:Caller caller, http:Request request) {
        http:CircuitBreakerClient cbClient = <http:CircuitBreakerClient>simpleClientEP.httpClient;
        var backendRes = simpleClientEP->forward("/simple", request);
        http:CircuitState currentState = cbClient.getCurrentState();
        if (backendRes is http:Response) {
            if (!(currentState == http:CB_CLOSED_STATE)) {
                backendRes.setPayload("Circuit Breaker is not in correct state state");
            }
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else {
            http:Response response = new;
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            string errCause = <string> backendRes.detail().message;
            response.setPayload(errCause);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}

@http:ServiceConfig { basePath: "/simple" }
service simpleservice on new http:Listener(8089) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Hello World!!!");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}
