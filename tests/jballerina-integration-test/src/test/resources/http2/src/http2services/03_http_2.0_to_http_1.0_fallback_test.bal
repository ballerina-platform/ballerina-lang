// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package http2;

import ballerina/http;

listener http:Listener serviceEndpointWithoutSSL = new(9095, { httpVersion: "2.0" });

listener http:Listener serviceEndpointWithSSL = new(9096, {
    httpVersion: "2.0",
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorldWithoutSSL on serviceEndpointWithoutSSL {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHelloGet(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Version: " + <@untainted> req.httpVersion);
    }
}

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorldWithSSL on serviceEndpointWithSSL {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHelloGet(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Version: " + <@untainted> req.httpVersion);
    }
}
