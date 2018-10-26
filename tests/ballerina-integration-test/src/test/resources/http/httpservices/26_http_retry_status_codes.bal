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

endpoint http:Client internalErrorEP {
    url: "http://localhost:8080",
    retryConfig: {
        interval: 3000,
        count: 3,
        backOffFactor: 2.0,
        maxWaitInterval: 20000,
        statusCodes: [501, 502, 503]
    },
    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/retry"
}
service<http:Service> retryStatusService bind { port: 9225 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    invokeEndpoint(endpoint caller, http:Request request) {
        if (request.getHeader("x-retry") == "recover") {
            var backendResponse = internalErrorEP->get("/status/recover", message = untaint request);
            match backendResponse {
                http:Response response => {
                    caller->respond(response) but {
                        error e => log:printError("Error sending response", err = e)
                    };
                }
                error responseError => {
                    http:Response errorResponse = new;
                    errorResponse.statusCode = 500;
                    errorResponse.setPayload(responseError.message);
                    caller->respond(errorResponse) but {
                        error e => log:printError("Error sending response", err = e)
                    };
                }
            }
        } else if (request.getHeader("x-retry") == "internalError") {
            var backendResponse = internalErrorEP->get("/status/internalError", message = untaint request);
            match backendResponse {
                http:Response response => {
                    caller->respond(response) but {
                        error e => log:printError("Error sending response", err = e)
                    };
                }
                error responseError => {
                    http:Response errorResponse = new;
                    errorResponse.statusCode = 500;
                    errorResponse.setPayload(responseError.message);
                    caller->respond(errorResponse) but {
                        error e => log:printError("Error sending response", err = e)
                    };
                }
            }
        }
    }
}

public int retryCounter = 0;

@http:ServiceConfig { basePath: "/status" }
service<http:Service> mockStatusCodeService bind { port: 8080 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/recover"
    }
    recoverableResource(endpoint caller, http:Request req) {
        retryCounter = retryCounter + 1;
        if (retryCounter % 4 != 0) {
            http:Response res = new;
            res.statusCode = 502;
            res.setPayload("Gateway Timed out.");
            caller->respond(res) but {
                error e => log:printError("Error sending response from the service", err = e)
            };
        } else {
            caller->respond("Hello World!!!") but {
                error e => log:printError("Error sending response from the service", err = e)
            };
        }
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/internalError"
    }
    unRecoverableResource(endpoint caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 502;
        res.setPayload("Gateway Timed out.");
        caller->respond(res) but {
            error e => log:printError("Error sending response from the service", err = e)
        };
    }
}
