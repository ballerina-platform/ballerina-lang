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

auth:ConfigAuthStoreProvider basicAuthProvider12_1 = new;
auth:ConfigAuthStoreProvider basicAuthProvider12_2 = new;
http:BasicAuthnHandler basicAuthnHandler12_1 = new(basicAuthProvider12_1);
http:BasicAuthnHandler basicAuthnHandler12_2 = new(basicAuthProvider12_2);

listener http:Listener listener12 = new(9194, config = {
    auth: {
        authnHandlers: [basicAuthnHandler12_1]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo",
    auth: {
        authnHandlers: [basicAuthnHandler12_2],
        enabled: true,
        scopes: ["scope2"]
    }
}
service echo12 on listener12 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}