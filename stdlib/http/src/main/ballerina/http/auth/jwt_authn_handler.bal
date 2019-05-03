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
import ballerina/log;

# Representation of JWT Auth handler for HTTP traffic
#
# + authProvider - `JWTAuthProvider` instance
public type JwtAuthnHandler object {

    *AuthnHandler;

    public auth:AuthProvider authProvider;

    public function __init(auth:AuthProvider authProvider) {
        self.authProvider = authProvider;
    }
};

# Checks if the request can be authenticated with JWT
#
# + req - `Request` instance
# + return - `true` if can be authenticated, else `false`
public function JwtAuthnHandler.canHandle(Request req) returns boolean {
    if (req.hasHeader(AUTH_HEADER)) {
        string headerValue = extractAuthorizationHeaderValue(req);
        if (headerValue.hasPrefix(AUTH_SCHEME_BEARER)) {
            string[] authHeaderComponents = headerValue.split(" ");
            if (authHeaderComponents.length() == 2) {
                string[] jwtComponents = authHeaderComponents[1].split("\\.");
                if (jwtComponents.length() == 3) {
                    return true;
                }
            }
        }
    }
    return false;
}

# Authenticates the incoming request using JWT authentication
#
# + req - `Request` instance
# + return - `true` if authenticated successfully, else `false`, or `error` in case of errors
public function JwtAuthnHandler.handle(Request req) returns boolean|error {
    string jwtToken = extractJWTToken(req);
    var authenticated = self.authProvider.authenticate(jwtToken);
    if (authenticated is boolean) {
        return authenticated;
    } else {
        return prepareError("Error while validating JWT token");
    }
}

function extractJWTToken(Request req) returns string {
    string authHeader = req.getHeader(AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}
