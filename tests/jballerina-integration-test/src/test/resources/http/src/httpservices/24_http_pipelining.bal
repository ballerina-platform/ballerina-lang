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

service pipeliningTest on new http:Listener(9220) {

    resource function responseOrder(http:Caller caller, http:Request req) {
        http:Response response = new;

        if (req.hasHeader("message-id")) {
            //Request one roughly takes 4 seconds to prepare its response
            if (req.getHeader("message-id") == "request-one") {
                runtime:sleep(4000);
                response.setHeader("message-id", "response-one");
                response.setPayload("Hello1");
            }
            //Request two's response will get ready immediately without any sleep time
            if (req.getHeader("message-id") == "request-two") {
                response.setHeader("message-id", "response-two");
                response.setPayload("Hello2");
            }
            //Request three roughly takes 2 seconds to prepare its response
            if (req.getHeader("message-id") == "request-three") {
                runtime:sleep(2000);
                response.setHeader("message-id", "response-three");
                response.setPayload("Hello3");
            }
        }

        var result = caller->respond(<@untainted> response);
        if (result is error) {
            error err = result;
            log:printError(<string> err.detail()["message"], result);
        }
    }
}

service pipelining on new http:Listener(9221, { timeoutInMillis: 1000 }) {

    resource function testTimeout(http:Caller caller, http:Request req) {
        http:Response response = new;

        if (req.hasHeader("message-id")) {
            //Request one roughly takes 8 seconds to prepare its response
            if (req.getHeader("message-id") == "request-one") {
                runtime:sleep(8000);
                response.setHeader("message-id", "response-one");
                response.setPayload("Hello1");
            }
            //Request two and three will be ready immediately, but they should't have sent out to the client
            if (req.getHeader("message-id") == "request-two") {
                response.setHeader("message-id", "response-two");
                response.setPayload("Hello2");
            }

            if (req.getHeader("message-id") == "request-three") {
                response.setHeader("message-id", "response-three");
                response.setPayload("Hello3");
            }
        }

        var responseError = caller->respond(response);
        if (responseError is error) {
            error err = responseError;
            log:printError("Pipeline timeout:" + err.reason(), err);
        }
    }
}

service pipeliningLimit on new http:Listener(9222, { http1Settings: { maxPipelinedRequests: 2 } }) {
    resource function testMaxRequestLimit(http:Caller caller, http:Request req) {
        http:Response response = new;
        //Let the thread sleep for sometime so the requests have enough time to queue up
        runtime:sleep(8000);
        response.setPayload("Pipelined Response");

        var responseError = caller->respond(response);
        if (responseError is error) {
            error err = responseError;
            log:printError("Pipeline limit exceeded:" + err.reason(), err);
        }
    }
}
