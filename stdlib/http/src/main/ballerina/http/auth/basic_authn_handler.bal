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
import ballerina/encoding;
import ballerina/log;
import ballerina/runtime;

# Defines Basic Auth handler for HTTP traffic.
#
# + authProvider - AuthProvider instance
public type BasicAuthnHandler object {

    *AuthnHandler;

    public auth:AuthProvider authProvider;

    public function __init(auth:AuthProvider authProvider) {
        self.authProvider = authProvider;
    }
};

# Checks if the provided request can be authenticated with basic auth.
#
# + req - Request object
# + return - `true` if it is possible authenticate with basic auth, else `false`, or `error` in case of errors
public function BasicAuthnHandler.handle(Request req) returns boolean|error {
    string basicAuthHeader = extractAuthorizationHeaderValue(req);
    string credential = basicAuthHeader.substring(5, basicAuthHeader.length()).trim();
    return self.authProvider.authenticate(credential);
}

# Intercept requests for authentication.
#
# + req - Request object
# + return - `true` if authentication is a success, else `false`
public function BasicAuthnHandler.canHandle(Request req) returns boolean {
    if (req.hasHeader(AUTH_HEADER)) {
        string basicAuthHeader = extractAuthorizationHeaderValue(req);
        return basicAuthHeader.hasPrefix(AUTH_SCHEME_BASIC);
    }
    return false;
}
