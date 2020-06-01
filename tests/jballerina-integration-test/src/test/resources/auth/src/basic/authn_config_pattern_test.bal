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

auth:InboundBasicAuthProvider basicAuthProvider07 = new;
http:BasicAuthHandler basicAuthHandler07 = new(basicAuthProvider07);

auth:InboundBasicAuthProvider basicAuthProvider07_1 = new({ tableName: "b7a.group1" });
http:BasicAuthHandler basicAuthHandler07_1 = new(basicAuthProvider07_1);

auth:InboundBasicAuthProvider basicAuthProvider07_2 = new({ tableName: "b7a.group2" });
http:BasicAuthHandler basicAuthHandler07_2 = new(basicAuthProvider07_2);

auth:InboundBasicAuthProvider basicAuthProvider07_3 = new({ tableName: "b7a.group3" });
http:BasicAuthHandler basicAuthHandler07_3 = new(basicAuthProvider07_3);

auth:InboundBasicAuthProvider basicAuthProvider07_4 = new({ tableName: "b7a.group4" });
http:BasicAuthHandler basicAuthHandler07_4 = new(basicAuthProvider07_4);

listener http:Listener listener07 = new(20009, {
    auth: {
        authHandlers: [basicAuthHandler07]
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
service echo07 on listener07 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [basicAuthHandler07_1]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [basicAuthHandler07_1, basicAuthHandler07_2]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler07_1]]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler07_1], [basicAuthHandler07_3]]
        }
    }
    resource function test4(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler07_1, basicAuthHandler07_2], [basicAuthHandler07_3, basicAuthHandler07_4]]
        }
    }
    resource function test5(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
