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
import ballerina/stringutils;
import ballerina/time;

# Represents inbound JWT auth provider.
#
# + jwtValidatorConfig - JWT validator configurations
public type InboundJwtAuthProvider object {

    *auth:InboundAuthProvider;

    public JwtValidatorConfig jwtValidatorConfig;

    # Provides authentication based on the provided JWT token.
    #
    # + jwtValidatorConfig - JWT validator configurations
    public function __init(JwtValidatorConfig jwtValidatorConfig) {
        self.jwtValidatorConfig = jwtValidatorConfig;
    }

    # Authenticate with a JWT token.
    #
    # + credential - Jwt token extracted from the authentication header
    # + return - `true` if authentication is successful, othewise `false` or `auth:Error` occurred during JWT validation
    public function authenticate(string credential) returns @tainted (boolean|auth:Error) {
        string[] jwtComponents = stringutils:split(credential, "\\.");
        if (jwtComponents.length() != 3) {
            return false;
        }

        if (self.jwtValidatorConfig.jwtCache.hasKey(credential)) {
            var payload = authenticateFromCache(self.jwtValidatorConfig, credential);
            if (payload is JwtPayload) {
                auth:setAuthenticationContext("jwt", credential);
                setPrincipal(payload);
                return true;
            } else {
                return false;
            }
        }

        var validationResult = validateJwt(credential, self.jwtValidatorConfig);
        if (validationResult is JwtPayload) {
            auth:setAuthenticationContext("jwt", credential);
            setPrincipal(validationResult);
            addToAuthenticationCache(self.jwtValidatorConfig, credential, <@untainted> validationResult?.exp,
                <@untainted> validationResult);
            return true;
        } else {
            return prepareAuthError("JWT validation failed.", validationResult);
        }
    }
};

function authenticateFromCache(JwtValidatorConfig jwtValidatorConfig, string jwtToken) returns JwtPayload? {
    var cachedJwt = trap <CachedJwt>jwtValidatorConfig.jwtCache.get(jwtToken);
    if (cachedJwt is CachedJwt) {
        // convert to current time and check the expiry time
        if (cachedJwt.expiryTime > (time:currentTime().time / 1000)) {
            JwtPayload payload = cachedJwt.jwtPayload;
            string? sub = payload?.sub;
            if (sub is string) {
                string printMsg = sub;
                log:printDebug(function() returns string {
                    return "Authenticate user :" + printMsg + " from cache";
                });
            }
            return payload;
        } else {
            jwtValidatorConfig.jwtCache.remove(jwtToken);
        }
    }
}

function addToAuthenticationCache(JwtValidatorConfig jwtValidatorConfig, string jwtToken, int? exp, JwtPayload payload) {
    CachedJwt cachedJwt = {jwtPayload : payload, expiryTime : exp is () ? 0 : exp};
    jwtValidatorConfig.jwtCache.put(jwtToken, cachedJwt);
    string? sub = payload?.sub;
    if (sub is string) {
        string printMsg = sub;
        log:printDebug(function() returns string {
            return "Add authenticated user :" + printMsg + " to the cache";
        });
    }
}

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
        if (claims.hasKey(SCOPE)) {
            var scopeString = claims[SCOPE];
            if (scopeString is string && scopeString != "") {
                auth:setPrincipal(scopes = stringutils:split(scopeString, " "));
            }
        }
        if (claims.hasKey(USERNAME)) {
            var name = claims[USERNAME];
            if (name is string) {
                auth:setPrincipal(username = name);
            }
        }
    }
}
