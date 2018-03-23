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

package ballerina.auth.basic;

import ballerina/auth.userstore;
import ballerina/caching;

@Description {value:"Represents a Basic Authenticator"}
@Field {value:"userStore: UserStore object; ex.: in basic authenticator, the user store"}
@Field {value:"authCache: Authentication cache object"}
public struct BasicAuthenticator {
    userstore:UserStore userStore;
    caching:Cache|null authCache;
}

@Description {value:"Represents an authentication decision about a user"}
@Field {value:"username: user name"}
@Field {value:"isAuthenticated: authentication decision, true if authenticated, else false"}
public struct AuthenticationInfo {
    string username;
    boolean isAuthenticated;
}

@Description {value:"Creates a Basic Authenticator"}
@Param {value:"userStore: implementation of the credentials store - ldap, jdbc, file based userstore, etc."}
@Param {value:"cache: cache instance"}
@Return {value:"BasicAuthenticator instance"}
public function createAuthenticator (userstore:UserStore userStore,
                                     caching:Cache|null cache) returns (BasicAuthenticator) {
    BasicAuthenticator authenticator = {userStore:userStore, authCache:cache};
    return authenticator;
}

@Description {value:"Performs basic authentication with the given username and password"}
@Param {value:"username: user name"}
@Param {value:"password: password"}
@Return {value:"boolean: true if authentication is successful, else false"}
public function <BasicAuthenticator authenticator> authenticate (string username, string password) returns (boolean) {
    return authenticator.userStore.authenticate(username, password);
}

@Description {value:"Retrieves the cached authentication result if any, for the given basic auth header value"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Return {value:"any: cached entry, or null in a cache miss"}
public function <BasicAuthenticator authenticator> getCachedAuthResult (string basicAuthCacheKey) returns (any) {
    try {
        match authenticator.authCache {
            caching:Cache cache => {
                return cache.get(basicAuthCacheKey);
            }
            null => {
                return null;
            }
        }
    } catch (error e) {
        // nothing to do
    }
    return null;
}

@Description {value:"Caches the authentication result"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Param {value:"authInfo: AuthenticationInfo instance containing authentication decision"}
public function <BasicAuthenticator authenticator> cacheAuthResult (string basicAuthCacheKey,
                                                                    AuthenticationInfo authInfo) {
    match authenticator.authCache {
        caching:Cache cache => {
            cache.put(basicAuthCacheKey, authInfo);
        }
        null => {
            return;
        }
    }
}

@Description {value:"Clears any cached authentication result"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
public function <BasicAuthenticator authenticator> clearCachedAuthResult (string basicAuthCacheKey) {
    match authenticator.authCache {
        caching:Cache cache => {
            cache.remove(basicAuthCacheKey);
        }
        null => {
            return;
        }
    }
}

@Description {value:"Creates AuthenticationInfo instance"}
@Param {value:"username: user name"}
@Param {value:"isAuthenticated: authentication decision"}
@Return {value:"AuthenticationInfo: Authentication decision instance, whether the user is authenticated or not"}
public function createAuthenticationInfo (string username, boolean isAuthenticated) returns (AuthenticationInfo) {
    AuthenticationInfo authInfo = {username:username, isAuthenticated:isAuthenticated};
    return authInfo;
}
