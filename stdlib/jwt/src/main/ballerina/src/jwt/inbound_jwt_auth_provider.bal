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
import ballerina/stringutils;

# Represents the inbound JWT auth provider, which authenticates by validating a JWT.
# The `jwt:InboundJwtAuthProvider` is another implementation of the `auth:InboundAuthProvider` interface.
# ```ballerina
# jwt:InboundJwtAuthProvider inboundJwtAuthProvider = new({
#     issuer: "example",
#     audience: "ballerina",
#     signatureConfig: {
#         certificateAlias: "ballerina",
#         trustStore: {
#             path: "/path/to/truststore.p12",
#             password: "ballerina"
#         }
#     }
# });
# ```
#
public type InboundJwtAuthProvider object {

    *auth:InboundAuthProvider;

    JwtValidatorConfig jwtValidatorConfig;

    # Provides authentication based on the provided JWT.
    #
    # + jwtValidatorConfig - JWT validator configurations
    public function init(JwtValidatorConfig jwtValidatorConfig) {
        self.jwtValidatorConfig = jwtValidatorConfig;
    }

# Authenticates provided JWT against `jwt:JwtValidatorConfig`.
#```ballerina
# boolean|auth:Error result = inboundJwtAuthProvider.authenticate("<credential>");
# ```
#
# + credential - JWT to be authenticated
# + return - `true` if authentication is successful, `false` otherwise or else an `auth:Error` if JWT validation failed
    public function authenticate(string credential) returns @tainted (boolean|auth:Error) {
        string[] jwtComponents = stringutils:split(credential, "\\.");
        if (jwtComponents.length() != 3) {
            return false;
        }

        JwtPayload|Error validationResult = validateJwt(credential, self.jwtValidatorConfig);
        if (validationResult is JwtPayload) {
            auth:setAuthenticationContext("jwt", credential);
            setPrincipal(validationResult);
            return true;
        } else {
            return prepareAuthError("JWT validation failed.", validationResult);
        }
    }
};

function setPrincipal(JwtPayload jwtPayload) {
    string? iss = jwtPayload?.iss;
    string? sub = jwtPayload?.sub;
    string userId = (iss is () ? "" : iss) + ":" + (sub is () ? "" : sub);
    // By default set sub as username.
    string username = (sub is () ? "" : sub);
    auth:setPrincipal(userId, username);
    map<json>? claims = jwtPayload?.customClaims;
    if (claims is map<json>) {
        auth:setPrincipal(claims = claims);
        if (claims.hasKey("scope")) {
            json scopeString = claims["scope"];
            if (scopeString is string && scopeString != "") {
                auth:setPrincipal(scopes = stringutils:split(scopeString, " "));
            }
        }
        if (claims.hasKey("name")) {
            json name = claims["name"];
            if (name is string) {
                auth:setPrincipal(username = name);
            }
        }
    }
}
