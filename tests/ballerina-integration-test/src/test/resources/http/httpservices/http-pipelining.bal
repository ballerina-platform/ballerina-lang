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

endpoint http:Listener listener {
    port: 9220
};

service<http:Service> pipeliningTest bind listener {

    responseOrder(endpoint caller, http:Request req) {
        http:Response reply = new;
        string replyMsg;

        if (req.hasHeader("message-id")) {
            //Request one will take 8 seconds to prepare its response
            if (req.getHeader("message-id") == "request-one") {
                runtime:sleep(8000);
                reply.setHeader("message-id", "response-one");
                reply.setPayload("Hello1");
            }
            //Request two's response will be ready immediately
            if (req.getHeader("message-id") == "request-two") {
                reply.setHeader("message-id", "response-two");
                reply.setPayload("Hello2");
            }
            //Request three will take 2 seconds to prepare its response
            if (req.getHeader("message-id") == "request-three") {
                runtime:sleep(2000);
                reply.setHeader("message-id", "response-three");
                reply.setPayload("Hello3");
            }
        }

        caller->respond(untaint reply) but {
            error err => log:printError(err.message, err = err)
        };
    }
}
