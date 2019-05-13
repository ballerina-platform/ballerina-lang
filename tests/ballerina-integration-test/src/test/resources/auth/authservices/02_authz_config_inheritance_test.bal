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

auth:ConfigAuthStoreProvider basicAuthProvider02 = new;
http:BasicAuthnHandler basicAuthnHandler02 = new(basicAuthProvider02);

listener http:Listener listener02_1 = new(9091, config = {
    auth: {
        authnHandlers: [basicAuthnHandler02],
        scopes: ["scope1"]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

listener http:Listener listener02_2 = new(9092, config = {
    auth: {
        authnHandlers: [basicAuthnHandler02],
        scopes: ["scope4"]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

listener http:Listener listener02_3 = new(9093, config = {
    auth: {
        authnHandlers: [basicAuthnHandler02]
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
        enabled: true,
        scopes: ["scope3"]
    }
}
service echo02_1 on listener02_1, listener02_2, listener02_3 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope3", "scope4"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }


    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}

@http:ServiceConfig {
    basePath: "/echo2",
    auth: {
        enabled: true,
        scopes: ["scope4"]
    }
}
service echo02_2 on listener02_1, listener02_2, listener02_3 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope3", "scope4"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }


    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}

@http:ServiceConfig {
    basePath: "/echo3",
    auth: {
        enabled: true
    }
}
service echo02_3 on listener02_1, listener02_2, listener02_3 {

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope1", "scope2"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true,
            scopes: ["scope3", "scope4"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }


    @http:ResourceConfig {
        methods: ["GET"],
        auth: {
            enabled: true
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
