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

import ballerina/runtime;
import ballerina/jwt;
import ballerina/time;
import ballerina/config;
import ballerina/caching;
import ballerina/log;
import ballerina/auth.utils;

@Description {value:"Represents a JWT Authenticator"}
@Field {value:"jwtValidatorConfig: JWTValidatorConfig object"}
@Field {value:"authCache: Authentication cache object"}
public struct JWTAuthenticator {
    jwt:JWTValidatorConfig jwtValidatorConfig;
    caching:Cache authCache;
}

@final string AUTHENTICATOR_JWT = "authenticator_jwt";
@final string ISSUER = "issuer";
@final string AUDIENCE = "audience";
@final string CERTIFICATE_ALIAS = "certificateAlias";
@final string JWT_AUTH_CACHE = "jwt_auth_cache";
@final string SCOPES = "scope";
@final string GROUPS = "groups";
@final string USERNAME = "name";
@final string AUTH_TYPE_JWT = "jwt";

struct CachedJWTAuthContext {
    jwt:Payload jwtPayload;
    int expiryTime;
}

@Description {value:"Creates a JWT Authenticator instance"}
@Return {value:"JWTAuthenticator instance"}
public function createAuthenticator () returns (JWTAuthenticator) {
    JWTAuthenticator authenticator = {};
    authenticator.jwtValidatorConfig = getAuthenticatorConfig();
    match utils:createCache(JWT_AUTH_CACHE) {
        caching:Cache cache => authenticator.authCache = cache;
        any|null => authenticator.authCache = {};
    }
    return authenticator;
}

@Description {value:"Authenticate with a jwt token"}
@Param {value:"jwtToken: Jwt token extracted from the authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
@Return {value:"error: If error occured in authentication"}
public function <JWTAuthenticator authenticator> authenticate (string jwtToken) returns boolean|error {
    boolean isCacheHit;
    boolean isAuthenticated;
    if (authenticator.authCache.capacity > 0) {
        match authenticator.authenticateFromCache(jwtToken) {
            (boolean, boolean) cacheHit => {
                (isCacheHit, isAuthenticated) = cacheHit;
                if (isCacheHit) {
                    return isAuthenticated;
                }
            }
            (boolean, boolean, jwt:Payload) authResult => {
                var (_, isAuthenticated, jwtPayload) = authResult;
                setAuthContext(jwtPayload, jwtToken);
                return isAuthenticated;
            }
        }
    }
    match jwt:validate(jwtToken, authenticator.jwtValidatorConfig) {
        jwt:Payload authResult => {
            isAuthenticated = true;
            setAuthContext(authResult, jwtToken);
            if (authenticator.authCache.capacity > 0) {
                authenticator.addToAuthenticationCache(jwtToken, authResult.exp, authResult);
            }
            return isAuthenticated;
        }
        boolean isInvalid => return isInvalid;
        error err => return err;
    }
}

function getAuthenticatorConfig () returns (jwt:JWTValidatorConfig) {
    jwt:JWTValidatorConfig jwtValidatorConfig = {};
    jwtValidatorConfig.issuer = getAuthenticatorConfigValue(AUTHENTICATOR_JWT, ISSUER);
    jwtValidatorConfig.audience = getAuthenticatorConfigValue(AUTHENTICATOR_JWT, AUDIENCE);
    jwtValidatorConfig.certificateAlias = getAuthenticatorConfigValue(AUTHENTICATOR_JWT, CERTIFICATE_ALIAS);
    return jwtValidatorConfig;
}

function <JWTAuthenticator authenticator> authenticateFromCache (string jwtToken)
returns (boolean, boolean)|(boolean, boolean, jwt:Payload) {
    boolean isCacheHit;
    boolean isAuthenticated;
    CachedJWTAuthContext cachedAuthContext = {};
    try {
        match <CachedJWTAuthContext>authenticator.authCache.get(jwtToken) {
            CachedJWTAuthContext cache => {
                cachedAuthContext = cache;
                isCacheHit = true;
            }
            error err => isCacheHit = false;
        }
    } catch (error e) {
        isCacheHit = false;
    }

    if (isCacheHit) {
        if (cachedAuthContext.expiryTime > time:currentTime().time) {
            isAuthenticated = true;
            jwt:Payload payload = cachedAuthContext.jwtPayload;
            log:printDebug("Authenticate user :" + payload.sub + " from cache");
            return (isCacheHit, isAuthenticated, payload);
        }
    }
    return (isCacheHit, isAuthenticated);
}

function <JWTAuthenticator authenticator> addToAuthenticationCache (string jwtToken, int exp,
                                                                    jwt:Payload payload) {
    CachedJWTAuthContext cachedContext = {};
    cachedContext.jwtPayload = payload;
    cachedContext.expiryTime = exp;
    authenticator.authCache.put(jwtToken, cachedContext);
    log:printDebug("Add authenticated user :" + payload.sub + " to the cache");
}

function setAuthContext (jwt:Payload jwtPayload, string jwtToken) {
    runtime:AuthenticationContext authContext = runtime:getInvocationContext().authenticationContext;
    authContext.userId = jwtPayload.sub;
    // By default set sub as username.
    authContext.username = jwtPayload.sub;
    if (jwtPayload.customClaims[SCOPES] != null) {
        var scopeString = <string>jwtPayload.customClaims[SCOPES];
        if (scopeString != null) {
            authContext.scopes = scopeString.split(" ");
        }
        _ = jwtPayload.customClaims.remove(SCOPES);
    }
    if (jwtPayload.customClaims[GROUPS] != null) {
        string[] userGroups =? <string[]>jwtPayload.customClaims[GROUPS];
        if (lengthof userGroups > 0) {
            authContext.groups = userGroups;
        }
        _ = jwtPayload.customClaims.remove(GROUPS);
    }
    if (jwtPayload.customClaims[USERNAME] != null) {
        string name = <string>jwtPayload.customClaims[USERNAME];
        authContext.username = name;
        _ = jwtPayload.customClaims.remove(USERNAME);
    }

    authContext.authType = AUTH_TYPE_JWT;
    authContext.authToken = jwtToken;
}

function getAuthenticatorConfigValue (string instanceId, string property) returns (string) {
    match config:getAsString(instanceId + "." + property) {
        string value => return value;
        any|null => return "";
    }
}
