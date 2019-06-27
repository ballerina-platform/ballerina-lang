// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/encoding;
import ballerina/http;
import ballerina/runtime;

CustomAuthStoreProvider customAuthStoreProvider = new;
CustomAuthnHandler customAuthnHandler = new(customAuthStoreProvider);

listener http:Listener listener17 = new(9113, config = {
        auth: {
            authnHandlers: [customAuthnHandler],
            scopes: ["all"]
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
service echo17 on listener17 {
    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello Ballerina!");
    }
}


// ---------- custom_authn_handler ----------

public type CustomAuthnHandler object {

    *http:AuthnHandler;

    public auth:AuthProvider authProvider;

    public function __init(auth:AuthProvider authProvider) {
        self.authProvider = authProvider;
    }
};

public function CustomAuthnHandler.handle(http:Request req) returns boolean|error {
    var customAuthHeader = req.getHeader(http:AUTH_HEADER);
    string credential = customAuthHeader.substring(6, customAuthHeader.length()).trim();
    return self.authProvider.authenticate(credential);
}

public function CustomAuthnHandler.canHandle(http:Request req) returns boolean {
    var customAuthHeader = req.getHeader(http:AUTH_HEADER);
    return customAuthHeader.hasPrefix("Custom");
}


// ---------- custom_auth_store_provider ----------

public type CustomAuthStoreProvider object {

    *auth:AuthProvider;

    public function authenticate(string credential) returns boolean|error {
        string actualUsername = "abc";
        string actualPassword = "123";

        string decodedHeaderValue = encoding:byteArrayToString(check encoding:decodeBase64(credential));
        string[] decodedCredentials = decodedHeaderValue.split(":");
        string username = decodedCredentials[0];
        string password = decodedCredentials[1];

        boolean isAuthenticated = username == actualUsername && password == actualPassword;
        if (isAuthenticated) {
            runtime:Principal principal = runtime:getInvocationContext().principal;
            principal.userId = username;
            principal.username = username;
            principal.scopes = self.getScopes();
        }
        return isAuthenticated;
    }

    public function getScopes() returns string[] {
        string[] scopes = ["all"];
        return scopes;
    }
};
