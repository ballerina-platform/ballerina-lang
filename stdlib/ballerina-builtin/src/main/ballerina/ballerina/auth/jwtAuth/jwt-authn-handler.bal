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

package ballerina.auth.jwtAuth;

import ballerina.net.http;
import ballerina.log;

@Description {value:"Authentication header name"}
const string AUTH_HEADER = "Authorization";
@Description {value:"Bearer authentication scheme"}
const string AUTH_SCHEME = "Bearer";

@Description {value:"JWT authenticator instance"}
JWTAuthenticator authenticator;

@Description {value:"Representation of JWT Auth handler for HTTP traffic"}
@Field {value:"name: Authentication handler name"}
public struct HttpJwtAuthnHandler {
    string name = "jwt";
}

@Description {value:"Intercepts a HTTP request for authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function <HttpJwtAuthnHandler authnHandler> canHandle (http:Request req) (boolean) {
    string authHeader = req.getHeader(AUTH_HEADER);
    if (authHeader != null && authHeader.hasPrefix(AUTH_SCHEME)) {
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
public function <HttpJwtAuthnHandler authnHandler> handle (http:Request req) (boolean) {
    string jwtToken = extractJWTToken(req);
    if (authenticator == null) {
        authenticator = createAuthenticator();
    }
    var isAuthenticated, err = authenticator.authenticate(jwtToken);
    if (isAuthenticated) {
        return true;
    } else {
        if (err != null) {
            log:printErrorCause("Error while validating JWT token ", err);
        }
        return false;
    }
}

function extractJWTToken (http:Request req) (string) {
    string authHeader = req.getHeader(AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}
