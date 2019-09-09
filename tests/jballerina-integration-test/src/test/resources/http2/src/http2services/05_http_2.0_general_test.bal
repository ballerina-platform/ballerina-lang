// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener http:Listener ep = new(9099, { httpVersion: "2.0" });

//Backend pointed by these clients should be down.
http:Client priorOn = new("http://localhost:14555", { httpVersion: "2.0", http2Settings: {
                http2PriorKnowledge: true }, poolConfig: {} });

http:Client priorOff = new("http://localhost:14555", { httpVersion: "2.0", http2Settings: {
                http2PriorKnowledge: false }, poolConfig: {} });

@http:ServiceConfig {
    basePath: "/general"
}
service generalCases on ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/serverDown"
    }
    resource function backEndDown(http:Caller caller, http:Request req) {
        http:Request serviceReq = new;
        var result1 = priorOn->get("/bogusResource");
        var result2 = priorOff->get("/bogusResource");
        string response = handleResponse(result1) + "--" + handleResponse(result2);
        checkpanic caller->respond(<@untainted> response);
    }
}

function handleResponse(http:Response|error result) returns string {
    string response = "";
    if (result is http:Response) {
        response = "Call succeeded";
    } else {
        error err = result;
        string? errMsg = <string> err.detail()?.message;
        string reply = errMsg is string ? <@untainted string> errMsg : "client call";
        response = "Call to backend failed due to:" + reply;
    }
    return response;
}
