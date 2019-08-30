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

import ballerina/auth;
import ballerina/config;
import ballerina/http;

auth:InboundBasicAuthProvider basicAuthProvider04 = new;
http:BasicAuthHandler basicAuthHandler04 = new(basicAuthProvider04);

listener http:Listener listener04 = new(20005, {
    auth: {
        authHandlers: [basicAuthHandler04]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo",
    auth: {
        enabled: true,
        scopes: ["scope4"]
    }
}
service echo04 on listener04 {

    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
