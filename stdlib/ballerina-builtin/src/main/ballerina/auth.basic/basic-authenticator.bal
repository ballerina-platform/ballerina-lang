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
import ballerina/runtime;
import ballerina/log;
import ballerina/security.crypto;

@Description {value:"Represents a Basic Authenticator"}
@Field {value:"userStore: UserStore object; ex.: in basic authenticator, the user store"}
@Field {value:"authCache: Authentication cache object"}
public type BasicAuthenticator object {

    public {
        userstore:UserStore userStore;
        caching:Cache? authCache;
    }

    new (userStore, authCache) {
    }
    
    public function authenticate(string username, string password) returns (boolean);
    function authenticateFromCache(string basicAuthCacheKey) returns (boolean|());
    function cacheAuthResult (string basicAuthCacheKey, AuthenticationInfo authInfo);
};

@Description {value:"Performs basic authentication with the given username and password"}
@Param {value:"username: user name"}
@Param {value:"password: password"}
@Return {value:"boolean: true if authentication is successful, else false"}
public function BasicAuthenticator::authenticate (string username, string password) returns (boolean) {
    // check cache first
    string basicAuthCacheKey = crypto:getHash(username + "-" + password, crypto:SHA256);
    match authenticateFromCache(basicAuthCacheKey) {
        boolean isAuthenticated => {
            return isAuthenticated;
        }
        () => {
            AuthenticationInfo authInfo = createAuthenticationInfo(username,
                userStore.authenticate(username, password));
            if (authInfo.isAuthenticated) {
                log:printDebug("Successfully authenticated against the userstore");
                // populate AuthenticationContext for this request
                // set the username
                runtime:getInvocationContext().authenticationContext.username = username;
                // set the groups if available in userstore
                string[] groupsOfUser = userStore.readGroupsOfUser(username);
                runtime:getInvocationContext().authenticationContext.groups = groupsOfUser;
                authInfo.groups = groupsOfUser;
            } else {
                log:printDebug("Authentication failure");
            }
            cacheAuthResult(basicAuthCacheKey, authInfo);
            return authInfo.isAuthenticated;
        }
    }
}

@Description {value:"Retrieves the cached authentication result if any, for the given basic auth header value"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Return {value:"boolean|(): cached entry, or nil in a cache miss"}
function BasicAuthenticator::authenticateFromCache(string basicAuthCacheKey) returns (boolean|()) {
    try {
        match authCache {
            caching:Cache cache => {
                AuthenticationInfo authInfo = check <AuthenticationInfo> cache.get(basicAuthCacheKey);
                if (authInfo.isAuthenticated) {
                    runtime:getInvocationContext().authenticationContext.username = authInfo.username;
                    runtime:getInvocationContext().authenticationContext.groups = authInfo.groups;
                }
                return authInfo.isAuthenticated;
            }
            () => {
                return ();
            }
        }
    } catch (error e) {
        // nothing to do
    }
    return ();
}

@Description {value:"Caches the authentication result"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Param {value:"authInfo: AuthenticationInfo instance containing authentication decision"}
function BasicAuthenticator::cacheAuthResult (string basicAuthCacheKey, AuthenticationInfo authInfo) {
    match authCache {
        caching:Cache cache => {
            cache.put(basicAuthCacheKey, authInfo);
        }
        () => {
            return;
        }
    }
}

@Description {value:"Represents an authentication decision about a user"}
@Field {value:"username: user name"}
@Field {value:"isAuthenticated: authentication decision, true if authenticated, else false"}
public type AuthenticationInfo {
   string username,
   boolean isAuthenticated,
   string[] groups;
};

@Description {value:"Creates a Basic Authenticator"}
@Param {value:"userStore: implementation of the credentials store - ldap, jdbc, file based userstore, etc."}
@Param {value:"cache: cache instance"}
@Return {value:"BasicAuthenticator instance"}
public function createAuthenticator (userstore:UserStore userStore, caching:Cache? cache) returns (BasicAuthenticator) {
    BasicAuthenticator authenticator = new(userStore, cache);
    return authenticator;
}

@Description {value:"Creates AuthenticationInfo instance"}
@Param {value:"username: user name"}
@Param {value:"isAuthenticated: authentication decision"}
@Return {value:"AuthenticationInfo: Authentication decision instance, whether the user is authenticated or not"}
function createAuthenticationInfo (string username, boolean isAuthenticated) returns (AuthenticationInfo) {
    AuthenticationInfo authInfo = {username:username, isAuthenticated:isAuthenticated};
    return authInfo;
}
