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

int requestCount = 0;
int actualCount = 0;

endpoint http:Listener circuitBreakerEP06 {
    port:9312
};

endpoint http:Client backendClientEP06 {
    url: "http://localhost:8092",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 60000,
            bucketSizeMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.3,
        resetTimeMillis: 2000,
        statusCodes: [501, 502, 503]
    },
    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/cb"
}
service<http:Service> circuitbreaker06 bind circuitBreakerEP06 {

    @http:ResourceConfig {
        path: "/trialrun"
    }
    getState(endpoint caller, http:Request request) {
        requestCount += 1;
        // To ensure the reset timeout period expires
        if (requestCount == 3) {
            runtime:sleep(3000);
        }
        var backendRes = backendClientEP06->forward("/hello06", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendRes is error) {
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

@http:ServiceConfig { basePath: "/hello06" }
service<http:Service> helloService06 bind { port: 8092 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        actualCount += 1;
        http:Response res = new;
        if (actualCount == 1 || actualCount == 2) {
            res.statusCode = http:SERVICE_UNAVAILABLE_503;
            res.setPayload("Service unavailable.");
        } else {
            res.setPayload("Hello World!!!");
        }
        var responseToCaller = caller->respond(res);
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}
