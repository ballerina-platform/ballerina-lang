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

import ballerina/auth;
import ballerina/config;
import ballerina/http;

auth:InboundBasicAuthProvider basicAuthProvider20 = new;
http:BasicAuthHandler basicAuthHandler20 = new(basicAuthProvider20);

listener http:Listener listener20 = new(20026, {
    auth: {
        authHandlers: [basicAuthHandler20]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo20 on listener20 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: [["scope1"]]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: [["scope1"], ["scope3"]]
        }
    }
    resource function test4(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: [["scope1", "scope2"], ["scope3", "scope4"]]
        }
    }
    resource function test5(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
