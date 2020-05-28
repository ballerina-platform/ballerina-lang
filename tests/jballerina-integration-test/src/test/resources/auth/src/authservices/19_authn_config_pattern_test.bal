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

auth:InboundBasicAuthProvider basicAuthProvider19 = new;
http:BasicAuthHandler basicAuthHandler19 = new(basicAuthProvider19);

auth:InboundBasicAuthProvider basicAuthProvider19_1 = new({ tableName: "b7a.group1" });
http:BasicAuthHandler basicAuthHandler19_1 = new(basicAuthProvider19_1);

auth:InboundBasicAuthProvider basicAuthProvider19_2 = new({ tableName: "b7a.group2" });
http:BasicAuthHandler basicAuthHandler19_2 = new(basicAuthProvider19_2);

auth:InboundBasicAuthProvider basicAuthProvider19_3 = new({ tableName: "b7a.group3" });
http:BasicAuthHandler basicAuthHandler19_3 = new(basicAuthProvider19_3);

auth:InboundBasicAuthProvider basicAuthProvider19_4 = new({ tableName: "b7a.group4" });
http:BasicAuthHandler basicAuthHandler19_4 = new(basicAuthProvider19_4);

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
            authHandlers: [basicAuthHandler19_1]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [basicAuthHandler19_1, basicAuthHandler19_2]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler19_1]]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler19_1], [basicAuthHandler19_3]]
        }
    }
    resource function test4(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            authHandlers: [[basicAuthHandler19_1, basicAuthHandler19_2], [basicAuthHandler19_3, basicAuthHandler19_4]]
        }
    }
    resource function test5(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
