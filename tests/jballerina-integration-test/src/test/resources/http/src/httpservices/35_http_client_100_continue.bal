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

import ballerina/log;
import ballerina/http;

http:Client continueClient = new ("http://localhost:9241", { cache: { enabled: false }});

@http:ServiceConfig {
    basePath: "/continue"
}
service continueBackend on new http:Listener(9241) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        if (request.expects100Continue()) {
            string mediaType = request.getHeader("Content-Type");
            if (mediaType.toLowerAscii() == "text/plain") {
                var result = caller->continue();
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            } else {
                http:Response res = new;
                res.statusCode = 417;
                res.setPayload("Unprocessable Entity");
                var result = caller->respond(res);
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
                return;
            }
        }
        http:Response res = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            log:printInfo(payload);
            res.statusCode = 200;
            res.setPayload("Hello World!\n");
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted> <string> payload.detail()?.message);
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        }
    }
}

@http:ServiceConfig {
    basePath: "/continue"
}
service continueClientTest on new http:Listener(9242)  {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function testContinueClient(http:Caller caller, http:Request req) {
        req.addHeader("content-type", "text/plain");
        req.addHeader("Expect", "100-continue");
        req.setPayload("Hi");
        var response = continueClient->post("/continue", <@untainted> req);
        if (response is http:Response) {
            checkpanic caller->respond(<@untainted> response);
        } else {
            checkpanic caller->respond("Error in client post - HTTP/1.1");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/failure"
    }
    resource function continueClient(http:Caller caller, http:Request req) {
        req.addHeader("Expect", "100-continue");
        req.addHeader("content-type", "application/json");
        req.setPayload({ name: "apple", color: "red" });
        var response = continueClient->post("/continue", <@untainted> req);
        if (response is http:Response) {
            checkpanic caller->respond(<@untainted> response);
        } else {
            checkpanic caller->respond("Error in client post - HTTP/1.1");
        }
    }
}
