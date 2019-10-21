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

http:Client h2Client = new ("http://localhost:9107", {
    httpVersion: "2.0",
    http2Settings: {
        http2PriorKnowledge: true
    }
});

@http:ServiceConfig {
    basePath: "/test"
}
service helloWorld on new http:Listener(9107, {httpVersion: "2.0"}) {
    @http:ResourceConfig {
        path: "/hello"
    }
    resource function abnormalResource(http:Caller caller, http:Request request) {
        var result = caller->continue();
        handleError(result);
        http:Response res = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            res.statusCode = 200;
            res.setPayload(<@untaintedstring>payload);
            var result1 = caller->respond(res);
            handleError(result1);
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted><string>payload.detail()?.message);
            var result1 = caller->respond(res);
            handleError(result1);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/initial"
    }
    resource function test100Continue(http:Caller caller, http:Request req) {
        var response = h2Client->post("/test/hello", "100 continue response should be ignored by this client");
        if (response is http:Response) {
            checkpanic caller->respond(<@untainted>response);
        } else {
            checkpanic caller->respond("Error sending client request");
        }
    }
}

function handleError(error? result) {
    if (result is error) {
        log:printError(result.reason(), err = result);
    }
}
