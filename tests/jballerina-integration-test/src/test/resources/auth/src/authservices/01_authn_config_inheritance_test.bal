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

auth:InboundBasicAuthProvider basicAuthProvider01 = new;
http:BasicAuthHandler basicAuthHandler01 = new(basicAuthProvider01);

listener http:Listener listener01 = new(20000, {
    auth: {
        authHandlers: [basicAuthHandler01]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

// Service with auth enabled - true
@http:ServiceConfig {
    basePath: "/echo1",
    auth: {
        enabled: true
    }
}
service echo01_1 on listener01 {

    // Resource with auth enabled - true
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - false
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: false
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - not given
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Service with auth enabled - false
@http:ServiceConfig {
    basePath: "/echo2",
    auth: {
        enabled: false
    }
}
service echo01_2 on listener01 {

    // Resource with auth enabled - true
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - false
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: false
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - not given
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Service with auth enabled - not given
@http:ServiceConfig {
    basePath: "/echo3"
}
service echo01_3 on listener01 {

    // Resource with auth enabled - true
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - false
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: false
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with auth enabled - not given
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
