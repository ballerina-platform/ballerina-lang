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

public int counter = 1;

endpoint http:Listener circuitBreakerEP00 {
    port:9306
};

endpoint http:Client backendClientEP00 {
    url: "http://localhost:8086",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 60000,
            bucketSizeMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.3,
        resetTimeMillis: 3000,
        statusCodes: [501, 502, 503]
    },

    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/cb"
}
service<http:Service> circuitbreaker00 bind circuitBreakerEP00 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/typical"
    }
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = backendClientEP00->forward("/hello/typical", request);
        if (counter % 5 == 0) {
            runtime:sleep(3000);
        } else {
            runtime:sleep(1000);
        }
        match backendRes {
            http:Response res => {
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                http:Response response = new;
                response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                response.setPayload(responseError.message);
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

// This sample service is used to mock connection timeouts and service outages.
// Mock a service outage by stopping/starting this service.
// This should run separately from the `circuitBreakerDemo` service.
@http:ServiceConfig { basePath: "/hello" }
service<http:Service> helloWorld bind { port: 8086 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/typical"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        if (counter % 5 == 3) {
            counter++;
            runtime:sleep(3000);
        } else {
            counter++;
        }
        res.setPayload("Hello World!!!");
        caller->respond(res) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}
