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

endpoint http:Listener serviceEndpointWithoutSSL {
    port: 9095,
    httpVersion: "2.0"
};

endpoint http:Listener serviceEndpointWithSSL {
    port: 9096,
    httpVersion: "2.0",
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorldWithoutSSL bind serviceEndpointWithoutSSL {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHelloGet(endpoint caller, http:Request req) {
        _ = caller->respond("Version: " + untaint req.httpVersion);
    }
}

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorldWithSSL bind serviceEndpointWithSSL {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHelloGet(endpoint caller, http:Request req) {
        _ = caller->respond("Version: " + untaint req.httpVersion);
    }
}
