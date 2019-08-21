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

http:Client internalErrorEP = new("http://localhost:8080", {
    retryConfig: {
        intervalInMillis: 3000,
        count: 3,
        backOffFactor: 2.0,
        maxWaitIntervalInMillis: 20000,
        statusCodes: [501, 502, 503]
    },
    timeoutInMillis: 2000
});

@http:ServiceConfig {
    basePath: "/retry"
}
service retryStatusService on new http:Listener(9225) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function invokeEndpoint(http:Caller caller, http:Request request) {
        if (request.getHeader("x-retry") == "recover") {
            var backendResponse = internalErrorEP->get("/status/recover", <@untainted> request);
            if (backendResponse is http:Response) {
                var responseError = caller->respond(backendResponse);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            } else {
                error err = backendResponse;
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(err.reason());
                var responseError = caller->respond(errorResponse);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            }
        } else if (request.getHeader("x-retry") == "internalError") {
            var backendResponse = internalErrorEP->get("/status/internalError", <@untainted> request);
            if (backendResponse is http:Response) {
                var responseError = caller->respond(backendResponse);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            } else {
                error err = backendResponse;
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(err.reason());
                var responseError = caller->respond(errorResponse);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            }
        }
    }
}

int retryCounter = 0;

@http:ServiceConfig { basePath: "/status" }
service mockStatusCodeService on new http:Listener(8080) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/recover"
    }
    resource function recoverableResource(http:Caller caller, http:Request req) {
        retryCounter = retryCounter + 1;
        if (retryCounter % 4 != 0) {
            http:Response res = new;
            res.statusCode = 502;
            res.setPayload("Gateway Timed out.");
            var responseError = caller->respond(res);
            if (responseError is error) {
                log:printError("Error sending response from the service", responseError);
            }
        } else {
            var responseError = caller->respond("Hello World!!!");
            if (responseError is error) {
                log:printError("Error sending response from the service", responseError);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/internalError"
    }
    resource function unRecoverableResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 502;
        res.setPayload("Gateway Timed out.");
        var responseError = caller->respond(res);
        if (responseError is error) {
            log:printError("Error sending response from the service", responseError);
        }
    }
}
