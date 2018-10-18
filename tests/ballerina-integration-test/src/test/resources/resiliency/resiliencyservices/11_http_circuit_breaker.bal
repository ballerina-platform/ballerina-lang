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

endpoint http:Listener circuitBreakerEP04 {
    port:9310
};

endpoint http:Client errornousClientEP {
    url: "http://localhost:8090",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 60000,
            bucketSizeMillis: 20000,
            requestVolumeThreshold: 6
        },
        failureThreshold: 0.3,
        resetTimeMillis: 10000,
        statusCodes: [500, 502, 503]
    },
    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/cb"
}
service<http:Service> circuitbreaker04 bind circuitBreakerEP04 {

    @http:ResourceConfig {
        path: "/requestvolume"
    }
    getState(endpoint caller, http:Request request) {
        var backendRes = errornousClientEP->forward("/errornous", request);
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

@http:ServiceConfig { basePath: "/errornous" }
service<http:Service> errornousservice bind { port: 8090 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.statusCode = http:INTERNAL_SERVER_ERROR_500;
        res.setPayload("Internal error occurred while processing the request.");
        caller->respond(res) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}
