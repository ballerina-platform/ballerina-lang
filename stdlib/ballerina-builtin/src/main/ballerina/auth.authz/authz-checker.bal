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
public struct AuthzChecker {
    permissionstore:PermissionStore permissionstore;
    caching:Cache|null authzCache;
}

@Description {value:"Creates a Basic Authenticator"}
@Param {value:"permissionstore: PermissionStore instance"}
@Param {value:"cache: Cache instance"}
@Return {value:"AuthzChecker: AuthzChecker instance"}
public function createChecker (permissionstore:PermissionStore permissionstore, caching:Cache|null cache)
                                                                                        returns (AuthzChecker) {
    AuthzChecker authzChecker = {permissionstore:permissionstore, authzCache:cache};
    return authzChecker;
}

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"username: user name"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function <AuthzChecker authzChecker> authorize (string username, string[] scopes) returns (boolean) {
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
        return authzChecker.permissionstore.isAuthorizedByGroups(groupsOfUser, scopes);
    }
    // if unable to get either scopes or groups from AuthenticationContext, use the permissionstore to lookup the
    // groups of the user and the groups of the scopes, and check for a match.
    return authzChecker.permissionstore.isAuthorized(username, scopes);
}

@Description {value:"Retrieves the cached authorization result if any, for the given basic auth header value"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Return {value:"any: cached entry, or null in a cache miss"}
public function <AuthzChecker authzChecker> getCachedAuthzResult (string authzCacheKey) returns (any) {
    try {
        match authzChecker.authzCache {
            caching:Cache cache => {
                return cache.get(authzCacheKey);
            }
            null => {
                return null;
            }
        }
    } catch (error e) {
        // do nothing
    }
    return null;
}

@Description {value:"Caches the authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Param {value:"isAuthorized: authorization decision"}
public function <AuthzChecker authzChecker> cacheAuthzResult (string authzCacheKey, boolean isAuthorized) {
    match authzChecker.authzCache {
        caching:Cache cache => {
            cache.put(authzCacheKey, isAuthorized);
        }
        null => {
            return;
        }
    }
}

@Description {value:"Clears any cached authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
public function <AuthzChecker authzChecker> clearCachedAuthzResult (string authzCacheKey) {
    match authzChecker.authzCache {
        caching:Cache cache => {
             cache.remove(authzCacheKey);
        }
        null => {
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
