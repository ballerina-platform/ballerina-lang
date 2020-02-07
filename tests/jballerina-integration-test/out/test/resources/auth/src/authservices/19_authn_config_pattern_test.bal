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
import ballerina/jwt;

auth:InboundBasicAuthProvider basicAuthProvider19 = new;
http:BasicAuthHandler basicAuthHandler19 = new(basicAuthProvider19);

jwt:InboundJwtAuthProvider jwtAuthProvider19_1 = new({
    issuer: "example1",
    audience: "ballerina",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler19_1 = new(jwtAuthProvider19_1);

jwt:InboundJwtAuthProvider jwtAuthProvider19_2 = new({
    issuer: "example2",
    audience: "ballerina",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler19_2 = new(jwtAuthProvider19_2);

jwt:InboundJwtAuthProvider jwtAuthProvider19_3 = new({
    issuer: "example3",
    audience: "ballerina",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler19_3 = new(jwtAuthProvider19_3);

jwt:InboundJwtAuthProvider jwtAuthProvider19_4 = new({
    issuer: "example4",
    audience: "ballerina",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler19_4 = new(jwtAuthProvider19_4);

listener http:Listener listener19 = new(20025, {
    auth: {
        authHandlers: [basicAuthHandler19]
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
service echo19 on listener19 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [jwtAuthHandler19_1]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [jwtAuthHandler19_1, jwtAuthHandler19_2]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[jwtAuthHandler19_1]]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[jwtAuthHandler19_1], [jwtAuthHandler19_3]]
        }
    }
    resource function test4(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[jwtAuthHandler19_1, jwtAuthHandler19_2], [jwtAuthHandler19_3, jwtAuthHandler19_4]]
        }
    }
    resource function test5(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
