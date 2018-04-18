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

package ballerina.auth;

import ballerina/caching;
import ballerina/config;
import ballerina/jwt;
import ballerina/log;
import ballerina/runtime;
import ballerina/time;

@Description {value:"Represents a JWT Authenticator"}
@Field {value:"jwtAuthProviderConfig: JWTAuthProviderConfig object"}
@Field {value:"authCache: Authentication cache object"}
public type JWTAuthProvider object {
    public {
        JWTAuthProviderConfig jwtAuthProviderConfig;
        caching:Cache authCache;
    }

    new (jwtAuthProviderConfig) {
    }
    public function authenticate (string jwtToken) returns (boolean|error);
    function authenticateFromCache (string jwtToken)
                                                returns ( boolean, jwt:Payload)? ;
    function addToAuthenticationCache (string jwtToken, int exp, jwt:Payload payload);
};

@Description {value:"Represents JWT validator configurations"}
public type JWTAuthProviderConfig {
    string issuer,
    string audience,
    int clockSkew,
    string certificateAlias,
    string trustStoreFilePath,
    string trustStorePassword,
};

@final string JWT_AUTH_CACHE = "jwt_auth_cache";
@final string SCOPES = "scope";
@final string GROUPS = "groups";
@final string USERNAME = "name";
@final string AUTH_TYPE_JWT = "jwt";

type CachedJWTAuthContext {
    jwt:Payload jwtPayload,
    int expiryTime;
};


@Description {value:"Authenticate with a jwt token"}
@Param {value:"jwtToken: Jwt token extracted from the authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
@Return {value:"error: If error occured in authentication"}
public function JWTAuthProvider::authenticate (string jwtToken) returns (boolean|error) {
    match self.authenticateFromCache(jwtToken) {
        (boolean, jwt:Payload) authResult => {
            var (isAuthenticated, jwtPayload) = authResult;
            match jwtPayload {
                jwt:Payload payload => setAuthContext(payload, jwtToken);
            }
            return isAuthenticated;
        }
        () => log:printDebug("Auth cache not initialized.");
    }
    match jwt:validate(jwtToken, self.jwtAuthProviderConfig) {
        (boolean, jwt:Payload) result => {
        var (isValid, jwtPayload) = result;
            if(isValid){
              match jwtPayload {
                jwt:Payload payload => {
                    setAuthContext(payload, jwtToken);
                    self.addToAuthenticationCache(jwtToken, payload. exp, payload);
                }
              }
            }
            return isValid;
        }
        error err => return err;
    }
}

function JWTAuthProvider::authenticateFromCache (string jwtToken) returns (boolean,jwt:Payload)|() {
    boolean isCacheHit;
    CachedJWTAuthContext cachedAuthContext = {};
    try {
        match <CachedJWTAuthContext> self.authCache.get(jwtToken) {
            CachedJWTAuthContext context => cachedAuthContext = context;
            error => isCacheHit = false;
        }
    } catch (error e) {
        isCacheHit = false;
    }

    if (isCacheHit) {
        if (cachedAuthContext.expiryTime > time:currentTime().time) {
            jwt:Payload payload = cachedAuthContext.jwtPayload;
            log:printDebug("Authenticate user :" + payload.sub + " from cache");
            return (true, payload);
        }
        return (false, ());
    }
    return ();
}

function JWTAuthProvider::addToAuthenticationCache (string jwtToken, int exp, jwt:Payload payload) {
    CachedJWTAuthContext cachedContext = {};
    cachedContext.jwtPayload = payload;
    cachedContext.expiryTime = exp;
    self.authCache.put(jwtToken, cachedContext);
    log:printDebug("Add authenticated user :" + payload.sub + " to the cache");
}

function setAuthContext (jwt:Payload jwtPayload, string jwtToken) {
    runtime:AuthenticationContext authContext = runtime:getInvocationContext().authenticationContext;
    authContext.userId = jwtPayload.sub;
    // By default set sub as username.
    authContext. username = jwtPayload.sub;
    if (jwtPayload.customClaims.hasKey(SCOPES)) {
        match jwtPayload.customClaims[SCOPES] {
            string scopeString => {
                authContext.scopes = scopeString.split(" ");
                _ = jwtPayload.customClaims.remove(SCOPES);
            }
            any => {}
        }
    }

    if (jwtPayload.customClaims.hasKey(GROUPS)) {
        match jwtPayload.customClaims[GROUPS] {
            string[] userGroups => {
                authContext.groups = userGroups;
                _ = jwtPayload.customClaims.remove(GROUPS);
            }
            any => {}
        }
    }

    if(jwtPayload.customClaims.hasKey(USERNAME)) {
        match jwtPayload.customClaims[USERNAME] {
            string name => {
                authContext.username = name;
                _ = jwtPayload.customClaims.remove(USERNAME);
            }
            any => {}
        }
    }

    authContext.authType = AUTH_TYPE_JWT;
    authContext.authToken = jwtToken;
}
