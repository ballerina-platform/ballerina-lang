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
import ballerina/mime;

http:Client clientEndpoint = new("http://localhost:9224");

@http:ServiceConfig {
    basePath: "/continue"
}
service helloContinue on new http:Listener(9090) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        if (request.expects100Continue()) {
            if (request.hasHeader("X-Status")) {
                log:printInfo("Sending 100-Continue response");
                var responseError = caller->continue();
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            } else {
                log:printInfo("Ignore payload by sending 417 response");
                http:Response res = new;
                res.statusCode = 417;
                res.setPayload("Do not send me any payload");
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
                return;
            }
        }

        http:Response res = new;
        var result  = request.getTextPayload();

        if (result is string) {
            var responseError = caller->respond(<@untainted> result);
            if (responseError is error) {
                log:printError("Error sending response", responseError);
            }
        } else {
            error err = result;
            res.statusCode = 500;
            res.setPayload(<@untainted> err.reason());
            log:printError("Failed to retrieve payload from request: " + err.reason());
            var responseError = caller->respond(res);
            if (responseError is error) {
                log:printError("Error sending response", responseError);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function getFormParam(http:Caller caller, http:Request req) {
        string replyMsg = "Result =";
        var bodyParts = req.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            int i = 0;
            while (i < bodyParts.length()) {
                mime:Entity part = bodyParts[i];
                mime:ContentDisposition contentDisposition = part.getContentDisposition();
                var result = part.getText();
                if (result is string) {
                    replyMsg += " Key:" + contentDisposition.name + " Value: " + result;
                } else {
                    replyMsg += <string> " Key:" + contentDisposition.name + " Value: " + result.detail()["message"];
                }
                i += 1;
            }
            var responseError = caller->respond(<@untainted> replyMsg);
            if (responseError is error) {
                error err = responseError;
                log:printError(<string> err.detail()["message"], responseError);
            }
        } else {
            error err = bodyParts;
            log:printError(<string> err.detail()["message"], bodyParts);
        }
    }

    resource function testPassthrough(http:Caller caller, http:Request req) {
        if (req.expects100Continue()) {
            req.removeHeader("Expect");
            var responseError = caller->continue();
            if (responseError is error) {
                log:printError("Error sending response", responseError);
            }
        }
        var res = clientEndpoint->forward("/backend/hello", <@untainted> req);
        if (res is http:Response) {
            var responseError = caller->respond(res);
            if (responseError is error) {
                log:printError("Error sending response", responseError);
            }
        } else {
            error err = res;
            log:printError(<string> err.detail()["message"], res);
        }
    }
}

service backend on new http:Listener(9224) {
    resource function hello(http:Caller caller, http:Request request) {
        http:Response response = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            response.setTextPayload(<@untainted> payload);
        } else {
            error err = payload;
            response.setTextPayload(<@untainted> err.reason());
        }
        var responseError = caller->respond(response);
        if (responseError is error) {
            log:printError("Error sending response", responseError);
        }
    }
}
