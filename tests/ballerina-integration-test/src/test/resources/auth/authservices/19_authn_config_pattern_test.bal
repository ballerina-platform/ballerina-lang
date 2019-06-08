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
import ballerina/http;
import ballerina/jwt;

auth:ConfigAuthStoreProvider basicAuthProvider19 = new;
http:BasicAuthHeaderAuthnHandler basicAuthnHandler19 = new(basicAuthProvider19);

jwt:JWTAuthProvider jwtAuthProvider19_1 = new({
    issuer: "example1",
    audience: ["ballerina"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});
http:BearerAuthHeaderAuthnHandler jwtAuthnHandler19_1 = new(jwtAuthProvider19_1);

jwt:JWTAuthProvider jwtAuthProvider19_2 = new({
    issuer: "example2",
    audience: ["ballerina"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});
http:BearerAuthHeaderAuthnHandler jwtAuthnHandler19_2 = new(jwtAuthProvider19_2);

jwt:JWTAuthProvider jwtAuthProvider19_3 = new({
    issuer: "example3",
    audience: ["ballerina"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});
http:BearerAuthHeaderAuthnHandler jwtAuthnHandler19_3 = new(jwtAuthProvider19_3);

jwt:JWTAuthProvider jwtAuthProvider19_4 = new({
    issuer: "example4",
    audience: ["ballerina"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});
http:BearerAuthHeaderAuthnHandler jwtAuthnHandler19_4 = new(jwtAuthProvider19_4);

listener http:Listener listener19 = new(9114, config = {
    auth: {
        authnHandlers: [basicAuthnHandler19]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
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
            authnHandlers: [jwtAuthnHandler19_1]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authnHandlers: [jwtAuthnHandler19_1, jwtAuthnHandler19_2]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authnHandlers: [[jwtAuthnHandler19_1]]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authnHandlers: [[jwtAuthnHandler19_1], [jwtAuthnHandler19_3]]
        }
    }
    resource function test4(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authnHandlers: [[jwtAuthnHandler19_1, jwtAuthnHandler19_2], [jwtAuthnHandler19_3, jwtAuthnHandler19_4]]
        }
    }
    resource function test5(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
