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
import ballerina/http;

# Defines OAuth2 handler for HTTP traffic.
#
# + authProvider - AuthProvider instance
public type OAuth2Handler object {

    *http:AuthnHandler;

    public auth:AuthProvider authProvider;

    public function __init(auth:AuthProvider authProvider) {
        self.authProvider = authProvider;
    }
};

# Checks if the provided request can be authenticated with OAuth2.
#
# + req - Request object
# + return - `true` if it is possible authenticate with OAuth2, else `false`, or `error` in case of errors
public function OAuth2Handler.handle(http:Request req) returns boolean|error {
    string authorizationHeader = http:extractAuthorizationHeaderValue(req);
    string credential = authorizationHeader.substring(6, authorizationHeader.length()).trim();
    return self.authProvider.authenticate(credential);
}

# Intercept requests for authentication.
#
# + req - Request object
# + return - `true` if authentication is a success, else `false`
public function OAuth2Handler.canHandle(http:Request req) returns boolean {
    if (req.hasHeader(http:AUTH_HEADER)) {
        string authorizationHeader = http:extractAuthorizationHeaderValue(req);
        return authorizationHeader.hasPrefix(http:AUTH_SCHEME_BEARER);
    }
    return false;
}
