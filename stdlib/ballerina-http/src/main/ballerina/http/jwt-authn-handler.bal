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

package ballerina.http;

import ballerina/log;
import ballerina/auth.jwtAuth;

@Description {value:"JWT authenticator instance"}
jwtAuth:JWTAuthenticator jwtAuthenticator = jwtAuth:createAuthenticator();

@Description {value:"Representation of JWT Auth handler for HTTP traffic"}
@Field {value:"name: Authentication handler name"}
public type HttpJwtAuthnHandler object {
    public {
        string name = "jwt";
    }

    @Description {value:"Intercepts a HTTP request for authentication"}
    @Param {value:"req: Request object"}
    @Return {value:"boolean: true if authentication is a success, else false"}
    public function canHandle (Request req) returns (boolean) {
        string authHeader;
        try {
            authHeader = req.getHeader(AUTH_HEADER);
        } catch (error e) {
            log:printDebug("Error in retrieving header " + AUTH_HEADER + ": " + e.message);
            return false;
        }
        if (authHeader != null && authHeader.hasPrefix(AUTH_SCHEME_BEARER)) {
            string[] authHeaderComponents = authHeader.split(" ");
            if (lengthof authHeaderComponents == 2) {
                string[] jwtComponents = authHeaderComponents[1].split("\\.");
                if (lengthof jwtComponents == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    @Description {value:"Checks if the provided HTTP request can be authenticated with JWT authentication"}
    @Param {value:"req: Request object"}
    @Return {value:"boolean: true if its possible to authenticate with JWT auth, else false"}
    public function handle (Request req) returns (boolean) {
        string jwtToken = extractJWTToken(req);
        var isAuthenticated = jwtAuthenticator.authenticate(jwtToken);
        match isAuthenticated {
            boolean authenticated => {
                return authenticated;
            }
            error err => {
                log:printErrorCause("Error while validating JWT token ", err);
                return false;
            }
        }
    }
};

function extractJWTToken (Request req) returns (string) {
    string authHeader = req.getHeader(AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}
