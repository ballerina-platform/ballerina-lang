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
public type JWTAuthenticator object {
    public {
        jwt:JWTValidatorConfig jwtValidatorConfig;
        caching:Cache? authCache;
    }
    public function authenticate (string jwtToken) returns (boolean|error);
    function authenticateFromCache (string jwtToken)
                                                returns (boolean, boolean)|(boolean, boolean, jwt:Payload);
    function addToAuthenticationCache (string jwtToken, int exp, jwt:Payload payload);
};

@final string AUTHENTICATOR_JWT = "authenticator_jwt";
@final string ISSUER = "issuer";
@final string AUDIENCE = "audience";
@final string CERTIFICATE_ALIAS = "certificateAlias";
@final string JWT_AUTH_CACHE = "jwt_auth_cache";
@final string SCOPES = "scope";
@final string GROUPS = "groups";
@final string USERNAME = "name";
@final string AUTH_TYPE_JWT = "jwt";

public type CachedJWTAuthContext {
    jwt:Payload jwtPayload,
    int expiryTime;
};

@Description {value:"Creates a JWT Authenticator instance"}
@Return {value:"JWTAuthenticator instance"}
public function createAuthenticator () returns (JWTAuthenticator) {
    JWTAuthenticator authenticator = new;
    authenticator.jwtValidatorConfig = getAuthenticatorConfig();
    match utils:createCache(JWT_AUTH_CACHE) {
        caching:Cache cache => authenticator.authCache = cache;
        () => authenticator.authCache = ();
    }
    return authenticator;
}

@Description {value:"Authenticate with a jwt token"}
@Param {value:"jwtToken: Jwt token extracted from the authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
@Return {value:"error: If error occured in authentication"}
public function JWTAuthenticator::authenticate (string jwtToken) returns (boolean|error) {
    boolean isCacheHit;
    boolean isAuthenticated;
    if (self.authCache.capacity > 0) {
        match self.authenticateFromCache(jwtToken) {
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
    match jwt:validate(jwtToken, jwtValidatorConfig) {
        jwt:Payload authResult => {
            isAuthenticated = true;
            setAuthContext(authResult, jwtToken);
            if (self.authCache.capacity > 0) {
                self.addToAuthenticationCache(jwtToken, authResult.exp, authResult);
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

function JWTAuthenticator::authenticateFromCache (string jwtToken)
returns (boolean, boolean)|(boolean, boolean, jwt:Payload) {
    boolean isCacheHit;
    boolean isAuthenticated;
    CachedJWTAuthContext cachedAuthContext = {};
    try {
        match <CachedJWTAuthContext> self.authCache.get(jwtToken) {
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

function JWTAuthenticator::addToAuthenticationCache (string jwtToken, int exp, jwt:Payload payload) {
    CachedJWTAuthContext cachedContext = {};
    cachedContext.jwtPayload = payload;
    cachedContext.expiryTime = exp;
    self.authCache.put(jwtToken, cachedContext);
    log:printDebug("Add authenticated user :" + payload.sub + " to the cache");
}

function setAuthContext (jwt:Payload jwtPayload, string jwtToken) {
    runtime:AuthenticationContext authContext = runtime:getInvocationContext().authenticationContext;
    authContext.userId = jwtPayload.sub;
    match jwtPayload.customClaims[SCOPES] {
        string scopeString => {
            authContext.scopes = scopeString.split(" ");
            _ = jwtPayload.customClaims.remove(SCOPES);
        }
        () => {}
    }

    match jwtPayload.customClaims[GROUPS] {
        string[] userGroups => {
            authContext.groups = userGroups;
            _ = jwtPayload.customClaims.remove(GROUPS);
        }
        () => {}
    }

    match jwtPayload.customClaims[USERNAME] {
        string name => {
            authContext.username = name;
            _ = jwtPayload.customClaims.remove(USERNAME);
        }
        () => {
            // By default set sub as username.
            authContext.username = jwtPayload.sub;
        }
    }

    authContext.authType = AUTH_TYPE_JWT;
    authContext.authToken = jwtToken;
}

function getAuthenticatorConfigValue (string instanceId, string property) returns (string) {
    match config:getAsString(instanceId + "." + property) {
        string value => return value;
        () => return "";
    }
}
