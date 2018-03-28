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
@Param {value:"scopeName: name of the scope"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function <AuthzChecker authzChecker> check (string username, string scopeName) returns (boolean) {
    // TODO: check if there are any groups set in the SecurityContext and if so, match against those.
    return authzChecker.permissionstore.isAuthorized(username, scopeName);
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
