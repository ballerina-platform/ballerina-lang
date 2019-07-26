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
import ballerina/cache;
import ballerina/internal;
import ballerina/log;
import ballerina/runtime;
import ballerina/time;

const string SCOPES = "scope";
const string GROUPS = "groups";
const string USERNAME = "name";
const string AUTH_TYPE_JWT = "jwt";

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
        string[] jwtComponents = internal:split(credential, "\\.");
        if (jwtComponents.length() != 3) {
            return false;
        }

        if (self.jwtValidatorConfig.jwtCache.hasKey(credential)) {
            var payload = authenticateFromCache(self.jwtValidatorConfig, credential);
            if (payload is JwtPayload) {
                setAuthenticationContext(payload, credential);
                return true;
            } else {
                return false;
            }
        }

        var validationResult = validateJwt(credential, self.jwtValidatorConfig);
        if (validationResult is JwtPayload) {
            setAuthenticationContext(validationResult, credential);
            addToAuthenticationCache(self.jwtValidatorConfig, credential, validationResult?.exp, validationResult);
            return true;
        } else {
            return auth:prepareError("JWT validation failed.", validationResult);
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

function setAuthenticationContext(JwtPayload jwtPayload, string jwtToken) {
    runtime:Principal? principal = runtime:getInvocationContext()?.principal;
    if (principal is runtime:Principal) {
        string? iss = jwtPayload?.iss;
        string? sub = jwtPayload?.sub;
        principal.userId = (iss is () ? "" : iss) + ":" + (sub is () ? "" : sub);
        // By default set sub as username.
        principal.username = (sub is () ? "" : sub);
        map<json>? claims = jwtPayload?.customClaims;
        if (claims is map<json>) {
            principal.claims = claims;
            if (claims.hasKey(SCOPES)) {
                var scopeString = claims[SCOPES];
                if (scopeString is string) {
                    principal.scopes = internal:split(scopeString, " ");
                }
            }
            if (claims.hasKey(USERNAME)) {
                var name = claims[USERNAME];
                if (name is string) {
                    principal.username = name;
                }
            }
        }
    }

    runtime:AuthenticationContext? authenticationContext = runtime:getInvocationContext()?.authenticationContext;
    if (authenticationContext is runtime:AuthenticationContext) {
        authenticationContext.scheme = AUTH_TYPE_JWT;
        authenticationContext.authToken = jwtToken;
    }
}
