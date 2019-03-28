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

import ballerina/http;

http:AuthProvider basicAuthProvider02 = {
    scheme: http:BASIC_AUTH,
    authStoreProvider: http:CONFIG_AUTH_STORE
};

listener http:Listener listener02 = new(9091, config = {
    authProviders: [basicAuthProvider02],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo",
    authConfig: {
        authentication: { enabled: false },
        scopes: ["xxx", "aaa"]
    }
}
service echo02 on listener02 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test",
        authConfig: {
            authentication: { enabled: true }
        }
    }
    resource function echo(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
