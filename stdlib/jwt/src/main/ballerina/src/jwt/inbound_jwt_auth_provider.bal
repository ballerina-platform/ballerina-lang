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
import ballerina/log;
import ballerina/stringutils;
import ballerina/time;

# Represents the inbound JWT auth provider, which authenticates by validating a JWT.
# The `jwt:InboundJwtAuthProvider` is another implementation of the `auth:InboundAuthProvider` interface.
# ```ballerina
# jwt:InboundJwtAuthProvider inboundJwtAuthProvider = new({
#     issuer: "example",
#     audience: "ballerina",
#     trustStoreConfig: {
#         certificateAlias: "ballerina",
#         trustStore: {
#             path: "/path/to/truststore.p12",
#             password: "ballerina"
#         }
#     }
# });
# ```
#
# + jwtValidatorConfig - JWT validator configurations
public type InboundJwtAuthProvider object {

    *auth:InboundAuthProvider;

    public JwtValidatorConfig jwtValidatorConfig;
    cache:Cache inboundJwtCache;

    # Provides authentication based on the provided JWT.
    #
    # + jwtValidatorConfig - JWT validator configurations
    public function __init(JwtValidatorConfig jwtValidatorConfig) {
        self.jwtValidatorConfig = jwtValidatorConfig;
        self.inboundJwtCache = jwtValidatorConfig.jwtCache;
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

        if (self.inboundJwtCache.hasKey(credential)) {
            JwtPayload? payload = authenticateFromCache(self.inboundJwtCache, credential);
            if (payload is JwtPayload) {
                auth:setAuthenticationContext("jwt", credential);
                setPrincipal(payload);
                return true;
            }
        }

        JwtPayload|Error validationResult = validateJwt(credential, self.jwtValidatorConfig);
        if (validationResult is JwtPayload) {
            auth:setAuthenticationContext("jwt", credential);
            setPrincipal(validationResult);
            addToAuthenticationCache(self.inboundJwtCache, credential, <@untainted> validationResult?.exp,
                <@untainted> validationResult);
            return true;
        } else {
            return prepareAuthError("JWT validation failed.", validationResult);
        }
    }
};

function authenticateFromCache(cache:Cache jwtCache, string jwt) returns JwtPayload? {
    if (jwtCache.hasKey(jwt)) {
        InboundJwtCacheEntry jwtCacheEntry = <InboundJwtCacheEntry>jwtCache.get(jwt);
        int? expTime = jwtCacheEntry.expTime;
        // convert to current time and check the expiry time
        if (expTime is () || expTime > (time:currentTime().time / 1000)) {
            JwtPayload payload = jwtCacheEntry.jwtPayload;
            string? sub = payload?.sub;
            if (sub is string) {
                string printMsg = sub;
                log:printDebug(function() returns string {
                    return "Authenticate user :" + printMsg + " from the cache";
                });
            }
            return payload;
        } else {
            cache:Error? result = jwtCache.invalidate(jwt);
            if (result is cache:Error) {
                log:printDebug(function() returns string {
                    return "Failed to invalidate JWT from the cache.";
                });
            }
        }
    }
}

function addToAuthenticationCache(cache:Cache jwtCache, string jwt, int? exp, JwtPayload payload) {
    InboundJwtCacheEntry jwtCacheEntry = {jwtPayload : payload, expTime : exp };
    cache:Error? result = jwtCache.put(jwt, jwtCacheEntry);
    if (result is cache:Error) {
        log:printDebug(function() returns string {
            return "Failed to add JWT to the cache";
        });
        return;
    }
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
