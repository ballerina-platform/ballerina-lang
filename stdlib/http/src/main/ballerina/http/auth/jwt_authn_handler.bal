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


import ballerina/log;
import ballerina/auth;

# Representation of JWT Auth handler for HTTP traffic
#
# + name - Name of the auth handler
# + jwtAuthenticator - `JWTAuthenticator` instance
public type HttpJwtAuthnHandler object {

    public string name = "jwt";
    public auth:JWTAuthProvider jwtAuthenticator;

    public function __init(auth:JWTAuthProvider jwtAuthenticator) {
        self.jwtAuthenticator = jwtAuthenticator;
    }

    # Checks if the request can be authenticated with JWT
    #
    # + req - `Request` instance
    # + return - true if can be authenticated, else false
    public function canHandle (Request req) returns (boolean);

    # Authenticates the incoming request using JWT authentication
    #
    # + req - `Request` instance
    # + return - true if authenticated successfully, else false
    public function handle (Request req) returns (boolean);
};

public function HttpJwtAuthnHandler.canHandle(Request req) returns (boolean) {
    string authHeader = "";
    var headerValue = trap req.getHeader(AUTH_HEADER);
    if (headerValue is string) {
        authHeader = headerValue;
    } else if (headerValue is error) {
        log:printDebug(function() returns string {
            return "Error in retrieving header " + AUTH_HEADER + ": " + headerValue.reason();
        });
        return false;
    }
    if (authHeader.hasPrefix(AUTH_SCHEME_BEARER)) {
        string[] authHeaderComponents = authHeader.split(" ");
        if (authHeaderComponents.length() == 2) {
            string[] jwtComponents = authHeaderComponents[1].split("\\.");
            if (jwtComponents.length() == 3) {
                return true;
            }
        }
    }
    return false;
}

public function HttpJwtAuthnHandler.handle (Request req) returns (boolean) {
    string jwtToken = extractJWTToken(req);
    var authenticated = self.jwtAuthenticator.authenticate(jwtToken);
    if (authenticated is boolean) {
        return authenticated;
    } else if (authenticated is error) {
        log:printError("Error while validating JWT token ", err = authenticated);
    }
    return false;
}

# Extracts the JWT from the incoming request
#
# + req - `Request` instance
# + return - Extracted JWT string
function extractJWTToken (Request req) returns (string) {
    string authHeader = req.getHeader(AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}
