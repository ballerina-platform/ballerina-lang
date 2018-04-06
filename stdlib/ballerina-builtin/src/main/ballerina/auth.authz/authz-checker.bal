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

package ballerina.auth.authz;

import ballerina/caching;
import ballerina/auth.authz.permissionstore;
import ballerina/runtime;
import ballerina/log;

@Description {value:"Representation of AuthzChecker"}
@Field {value:"authzCache: authorization cache instance"}
public type AuthzChecker object {
    public {
        permissionstore:PermissionStore permissionstore;
        caching:Cache? authzCache;
    }
    new (permissionstore, authzCache) {
    }
    public function authorize (string username, string resourceName, string method, string[] scopes) returns (boolean);
    function authorizeFromCache(string authzCacheKey) returns (boolean|());
    function cacheAuthzResult (string authzCacheKey, boolean isAuthorized);
};

@Description {value:"Creates a Basic Authenticator"}
@Param {value:"permissionstore: PermissionStore instance"}
@Param {value:"cache: Cache instance"}
@Return {value:"AuthzChecker: AuthzChecker instance"}
public function createChecker (permissionstore:PermissionStore permissionstore, caching:Cache? cache)
                                                                                        returns (AuthzChecker) {
    AuthzChecker authzChecker = new(permissionstore, cache);
    return authzChecker;
}

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"username: user name"}
@Param {value:"resourceName: resource name"}
@Param {value:"method: method names"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function AuthzChecker::authorize (string username, string resourceName,
                                                        string method, string[] scopes) returns (boolean) {
    // first, check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = username + "-" + resourceName + "-" + method;
    match self.authorizeFromCache(authzCacheKey) {
        boolean isAuthorized => {
            return isAuthorized;
        }
        () => {
            // if there are scopes set in the AuthenticationContext already from a previous authentication phase, try to
            // match against those.
            string[] authCtxtScopes = runtime:getInvocationContext().authenticationContext.scopes;
            if (lengthof authCtxtScopes > 0) {
                return matchScopes(scopes, authCtxtScopes);
            }
            // if there are groups set in the AuthenticationContext which are relevant to a user, can use the groups in
            // authncontext and check if there is a match between those and the groups relevant to the scopes specified for
            // the resource.
            string[] groupsOfUser = runtime:getInvocationContext().authenticationContext.groups;
            if (lengthof groupsOfUser > 0) {
                return self.permissionstore.isAuthorizedByGroups(groupsOfUser, scopes);
            }
            // if unable to get either scopes or groups from AuthenticationContext, use the permissionstore to lookup the
            // groups of the user and the groups of the scopes, and check for a match.
            boolean authorized = self.permissionstore.isAuthorized(username, scopes);
            if (authorized) {
                log:printDebug("Successfully authorized to access resource: " + resourceName + ", method: " + method);
            } else {
                log:printDebug("Authorization failure for resource: " + resourceName + ", method: " + method);
            }
            // cache authz result
            self.cacheAuthzResult(authzCacheKey, authorized);
            return authorized;
        }
    }
}

@Description {value:"Retrieves the cached authorization result if any, for the given basic auth header value"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Return {value:"boolean|(): cached entry, or nill in a cache miss"}
function AuthzChecker::authorizeFromCache(string authzCacheKey) returns (boolean|()) {
    try {
        match self.authzCache {
            caching:Cache cache => {
                return check <boolean> cache.get(authzCacheKey);
            }
        () => {
                return ();
            }
        }
    } catch (error e) {
        // do nothing
    }
    return ();
}

@Description {value:"Caches the authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Param {value:"isAuthorized: authorization decision"}
function AuthzChecker::cacheAuthzResult (string authzCacheKey, boolean isAuthorized) {
    match self.authzCache {
        caching:Cache cache => {
            cache.put(authzCacheKey, isAuthorized);
        }
        () => {
            return;
        }
    }
}

@Description {value:"Matches the scopes"}
@Param {value:"scopesOfResource: array of scopes for the resource"}
@Param {value:"scopesForRequest: array of scopes relevant to this request"}
@Return {value:"boolean: true if two arrays have at least one match"}
function matchScopes (string[] scopesOfResource, string[] scopesForRequest) returns (boolean) {
    foreach scopeForRequest in scopesForRequest {
        foreach scopeOfResource in scopesOfResource {
            if (scopeForRequest == scopeOfResource) {
                // if  that is equal to a group of a scope, authorization passes
                return true;
            }
        }
    }
    return false;
}
