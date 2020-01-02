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
import ballerina/io;

listener http:Listener echoEP  = new(9229);

string globalLvlStr = "sample value";

@http:ServiceConfig {basePath:"/listener"}
service echo on echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/respond"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(<@untainted> globalLvlStr);
        checkpanic caller->respond(res);
        globalLvlStr = "respond";
        io:println("Service Level Variable : " + globalLvlStr);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/redirect"
    }
    resource function round1(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["/redirect1/round2"]);
        globalLvlStr = "redirect";
        io:println("Service Level Variable : " + globalLvlStr);
    }

}
