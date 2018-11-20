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

import ballerina/cache;
import ballerina/internal;
import ballerina/log;
import ballerina/runtime;
import ballerina/time;

# Represents a JWT Authenticator
#
# + jwtAuthProviderConfig - JWT authentication provider configurations
public type JWTAuthProvider object {

    public JWTAuthProviderConfig jwtAuthProviderConfig;
    private cache:Cache authCache;

    # Provides authentication based on the provided jwt token
    #
    # + jwtAuthProviderConfig - JWT authentication provider configurations
    public new(jwtAuthProviderConfig) {
        self.authCache = new;
    }

    # Authenticate with a jwt token
    #
    # + jwtToken - Jwt token extracted from the authentication header
    # + return - true if authentication is successful, false otherwise.
    #            If an error occur during authentication, the error will be returned.
    public function authenticate(string jwtToken) returns boolean|error {
        if (self.authCache.hasKey(jwtToken)) {
            var payload = self.authenticateFromCache(jwtToken);
            if (payload is internal:JwtPayload) {
                self.setAuthContext(payload, jwtToken);
                return true;
            } else {
                return false;
            }
        }

        var payload = internal:validate(jwtToken, self.jwtAuthProviderConfig);
        if (payload is internal:JwtPayload) {
            self.setAuthContext(payload, jwtToken);
            self.addToAuthenticationCache(jwtToken, payload.exp, payload);
            return true;
        } else {
            return payload;
        }
    }

    function authenticateFromCache(string jwtToken) returns internal:JwtPayload|() {
        var context = <CachedJWTAuthContext>self.authCache.get(jwtToken);
        if (context is CachedJWTAuthContext) {
            // convert to current time and check the expiry time
            if (context.expiryTime > (time:currentTime().time / 1000)) {
                internal:JwtPayload payload = context.jwtPayload;
                log:printDebug(function() returns string {
                    return "Authenticate user :" + payload.sub + " from cache";
                });
                return payload;
            }
        }
        return ();
    }

    function addToAuthenticationCache(string jwtToken, int exp, internal:JwtPayload payload) {
        CachedJWTAuthContext cachedContext = {jwtPayload : payload, expiryTime : exp};
        self.authCache.put(jwtToken, cachedContext);
        log:printDebug(function() returns string {
            return "Add authenticated user :" + payload.sub + " to the cache";
        });
    }

    function setAuthContext(internal:JwtPayload jwtPayload, string jwtToken) {
        runtime:UserPrincipal userPrincipal = runtime:getInvocationContext().userPrincipal;
        userPrincipal.userId = jwtPayload.iss + ":" + jwtPayload.sub;
        // By default set sub as username.
        userPrincipal.username = jwtPayload.sub;
        userPrincipal.claims = jwtPayload.customClaims;
        if (jwtPayload.customClaims.hasKey(SCOPES)) {
            var scopeString = jwtPayload.customClaims[SCOPES];
            if (scopeString is string) {
                userPrincipal.scopes = scopeString.split(" ");
            }
        }
        if (jwtPayload.customClaims.hasKey(USERNAME)) {
            var name = jwtPayload.customClaims[USERNAME];
            if (name is string) {
                userPrincipal.username = name;
            }
        }
        runtime:AuthContext authContext = runtime:getInvocationContext().authContext;
        authContext.scheme = AUTH_TYPE_JWT;
        authContext.authToken = jwtToken;
    }

};

const string SCOPES = "scope";
const string GROUPS = "groups";
const string USERNAME = "name";
const string AUTH_TYPE_JWT = "jwt";

# Represents JWT validator configurations
#
# + issuer - Identifier of the token issuer
# + audience - Identifier of the token recipients
# + clockSkew - Time in seconds to mitigate clock skew
# + certificateAlias - Token signed key alias
# + trustStoreFilePath - Path to the trust store file
# + trustStorePassword - Trust store password
public type JWTAuthProviderConfig record {
    string issuer = "";
    string audience = "";
    int clockSkew = 0;
    string certificateAlias = "";
    string trustStoreFilePath = "";
    string trustStorePassword = "";
    !...
};

type CachedJWTAuthContext record {
    internal:JwtPayload jwtPayload;
    int expiryTime;
    !...
};
