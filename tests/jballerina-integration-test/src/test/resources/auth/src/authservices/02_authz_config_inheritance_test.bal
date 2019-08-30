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

auth:InboundBasicAuthProvider basicAuthProvider02 = new;
http:BasicAuthHandler basicAuthHandler02 = new(basicAuthProvider02);

// Listener with valid scope
listener http:Listener listener02_1 = new(20001, {
    auth: {
        authHandlers: [basicAuthHandler02],
        scopes: ["scope1"]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

// Listener with invalid scope
listener http:Listener listener02_2 = new(20002, {
    auth: {
        authHandlers: [basicAuthHandler02],
        scopes: ["scope10"]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

// Listener with scope not given
listener http:Listener listener02_3 = new(20003, {
    auth: {
        authHandlers: [basicAuthHandler02]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

// Service with valid scope
@http:ServiceConfig {
    basePath: "/echo1",
    auth: {
        enabled: true,
        scopes: ["scope1"]
    }
}
service echo02_1 on listener02_1, listener02_2, listener02_3 {

    // Resource with valid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with invalid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope10", "scope11"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with scope not given
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Service with invalid scope
@http:ServiceConfig {
    basePath: "/echo2",
    auth: {
        enabled: true,
        scopes: ["scope10"]
    }
}
service echo02_2 on listener02_1, listener02_2, listener02_3 {

    // Resource with valid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with invalid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope10", "scope11"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with scope not given
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Service with scope not given
@http:ServiceConfig {
    basePath: "/echo3",
    auth: {
        enabled: true
    }
}
service echo02_3 on listener02_1, listener02_2, listener02_3 {

    // Resource with valid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with invalid scope
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope10", "scope11"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }

    // Resource with scope not given
    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
