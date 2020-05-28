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

import ballerina/config;
import ballerina/http;
import ballerina/jwt;

jwt:InboundJwtAuthProvider jwtAuthProvider07 = new({
    issuer: "ballerina",
    audience: "ballerina",
    signatureConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});

http:BearerAuthHandler jwtAuthHandler07 = new(jwtAuthProvider07);

listener http:Listener listener07 = new(20101, {
    auth: {
        authHandlers: [jwtAuthHandler07]
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
        scopes: ["test-scope"]
    }
}
service echo07 on listener07 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
