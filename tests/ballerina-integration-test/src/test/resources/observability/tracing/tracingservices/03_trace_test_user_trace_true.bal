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
import ballerina/testobserve;
import ballerina/observe;

endpoint http:Listener listener2 {
    port: 9092
};

@http:ServiceConfig {
    basePath: "/echoService"
}
service echoService2 bind listener2 {
    resourceOne(endpoint caller, http:Request clientRequest) {
        int spanId = observe:startRootSpan("uSpanThree");
        http:Response outResponse = new;
        var response = check callNextResource2(spanId);
        outResponse.setTextPayload("Hello, World!");
        _ = caller->respond(outResponse);
        _ = observe:finishSpan(spanId);
    }

    resourceTwo(endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        res.setTextPayload("Hello, World 2!");
        _ = caller->respond(res);
    }

    getMockTracers(endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        json returnString = testobserve:getMockTracers();
        res.setJsonPayload(returnString);
        _ = caller->respond(res);
    }
}

function callNextResource2(int parentSpanId) returns (http:Response|error) {
    endpoint http:Client httpEndpoint {
        url: "http://localhost:9092/echoService"
    };
    int spanId = check observe:startSpan("uSpanFour", parentSpanId = parentSpanId);
    http:Response resp = check httpEndpoint->get("/resourceTwo");
    _ = observe:addTagToSpan(spanId = spanId, "Allowed", "Successful");
    _ = observe:finishSpan(spanId);
    _ = observe:addTagToSpan(spanId = spanId, "Disallowed", "Unsuccessful");
    return resp;
}
