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

@Description {value:"Represents authenticated user information"}
@Field {value:"userName: User name of the authenticated user"}
@Field {value:"roles: Roles of the user."}
@Field {value:"scopes: Scopes permited in authenticated session"}
@Field {value:"claims: User claims"}
struct JWTAuthContext {
    string userName;
    string[] roles;
    string[] scopes;
    map claims;
}

const string AUTHENTICATOR_JWT = "authenticator_jwt";
const string ISSUER = "issuer";
const string AUDIENCE = "audience";
const string CERTIFICATE_ALIAS = "certificateAlias";
const string JWT_AUTH_CACHE = "jwt_auth_cache";
const string SCOPES = "scope";
const string ROLES = "roles";

struct CachedJWTAuthContext {
    JWTAuthContext jwtAuthContext;
    int expiryTime;
}

@Description {value:"Creates a JWT Authenticator instance"}
@Return {value:"JWTAuthenticator instance"}
public function createAuthenticator () returns (JWTAuthenticator) {
    JWTAuthenticator authenticator = {};
    authenticator.jwtValidatorConfig = getAuthenticatorConfig();
    authenticator.authCache = utils:createCache(JWT_AUTH_CACHE);
    return authenticator;
}

@Description {value:"Authenticate with a jwt token"}
@Param {value:"jwtToken: Jwt token extracted from the authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
@Return {value:"error: If error occured in authentication"}
public function <JWTAuthenticator authenticator> authenticate (string jwtToken) returns (boolean, error) {
    boolean isAuthenticated = false;
    boolean isCacheHit = false;
    JWTAuthContext authContext;
    if (authenticator.authCache != null) {
        isCacheHit, isAuthenticated, authContext = authenticator.authenticateFromCache(jwtToken);
        if (isCacheHit) {
            return isAuthenticated, null;
        }
    }
    var isValid, payload, err = jwt:validate(jwtToken, authenticator.jwtValidatorConfig);
    if (isValid) {
        authContext = setAuthContext(payload);
        if (authenticator.authCache != null) {
            authenticator.addToAuthenticationCache(jwtToken, payload.exp, authContext);
        }
        return true, null;
    } else {
        return false, err;
    }
}

function getAuthenticatorConfig () returns (jwt:JWTValidatorConfig) {
    jwt:JWTValidatorConfig jwtValidatorConfig = {};
    jwtValidatorConfig.issuer = config:getInstanceValue(AUTHENTICATOR_JWT, ISSUER);
    jwtValidatorConfig.audience = config:getInstanceValue(AUTHENTICATOR_JWT, AUDIENCE);
    jwtValidatorConfig.certificateAlias = config:getInstanceValue(AUTHENTICATOR_JWT, CERTIFICATE_ALIAS);
    return jwtValidatorConfig;
}

function <JWTAuthenticator authenticator> authenticateFromCache (string jwtToken) returns (boolean isCacheHit,
                                                                                   boolean isAuthenticated,
                                                                                   JWTAuthContext authContext) {
    var cachedAuthContext, _ = (CachedJWTAuthContext)authenticator.authCache.get(jwtToken);
    if (cachedAuthContext != null) {
        isCacheHit = true;
        if (cachedAuthContext.expiryTime > time:currentTime().time) {
            isAuthenticated = true;
            authContext = cachedAuthContext.jwtAuthContext;
            log:printDebug("Authenticate user :" + authContext.userName + " from cache");
        }
    }
    return;
}

function <JWTAuthenticator authenticator> addToAuthenticationCache (string jwtToken, int exp,
                                                                    JWTAuthContext authContext) {
    CachedJWTAuthContext cachedContext = {};
    cachedContext.jwtAuthContext = authContext;
    cachedContext.expiryTime = exp;
    authenticator.authCache.put(jwtToken, cachedContext);
    log:printDebug("Add authenticated user :" + authContext.userName + " to the cache");
}

function setAuthContext (jwt:Payload jwtPayload) returns (JWTAuthContext) {
    JWTAuthContext authContext = {};
    authContext.userName = jwtPayload.sub;
    if (jwtPayload.customClaims[SCOPES] != null) {
        var scopeString, _ = (string)jwtPayload.customClaims[SCOPES];
        if (scopeString != null) {
            authContext.scopes = scopeString.split(" ");
        }
    }
    if (jwtPayload.customClaims[ROLES] != null) {
        var roleList, _ = (string[])jwtPayload.customClaims[ROLES];
        if (roleList != null) {
            authContext.roles = roleList;
        }
    }
    return authContext;
}
