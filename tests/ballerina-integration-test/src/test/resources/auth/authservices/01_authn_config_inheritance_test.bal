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
import ballerina/http;

auth:ConfigAuthStoreProvider basicAuthProvider01 = new;
http:BasicAuthHeaderAuthnHandler basicAuthnHandler01 = new(basicAuthProvider01);

listener http:Listener listener01 = new(9090, config = {
    auth: {
        authnHandlers: [basicAuthnHandler01]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo1",
    auth: {
        enabled: true
    }
}
service echo01_1 on listener01 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: false
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}

@http:ServiceConfig {
    basePath: "/echo2",
    auth: {
        enabled: false
    }
}
service echo01_2 on listener01 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: false
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
