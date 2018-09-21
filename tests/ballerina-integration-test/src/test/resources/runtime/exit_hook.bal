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
import ballerina/io;
import ballerina/runtime;

endpoint http:Listener echoEP1 {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo1 bind echoEP1 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo1 (endpoint caller, http:Request req) {
        addExitHooks();
        var payload = req.getTextPayload();
        match payload {
            string payloadValue => {
                http:Response resp = new;
                resp.setTextPayload(untaint payloadValue);
                _ = caller -> respond(resp);
            }
            any | () => {
                io:println("Error while fetching string payload");
            }
        }
    }
}

public function addExitHooks() {
    int a = 4;

    (function() returns ()) fn1 = function() {
        io:println("Exit hook one invoked");
    };
    runtime:addExitHook(fn1);

    var fn2 = function() {
        io:println("Exit hook two invoked with var : ", a);
    };

    runtime:addExitHook(fn2);
}
