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
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

# Representation of Authorization Handler for HTTP.
#
# + positiveAuthzCache - `Cache` instance, which is cache positive authorizations
# + negativeAuthzCache - `Cache` instance, which is cache negative authorizations
public type AuthzHandler object {

    public cache:Cache? positiveAuthzCache;
    public cache:Cache? negativeAuthzCache;

    public function __init(cache:Cache? positiveAuthzCache, cache:Cache? negativeAuthzCache) {
        self.positiveAuthzCache = positiveAuthzCache;
        self.negativeAuthzCache = negativeAuthzCache;
    }

    # Checks if the request can be authorized.
    #
    # + req - `Request` instance
    # + return - `true` if can be authorized, else `false`, or `AuthorizationError` if error occurred
    function canProcess(Request req) returns boolean|AuthorizationError {
        runtime:Principal? principal = runtime:getInvocationContext()?.principal;
        if (principal is runtime:Principal) {
            if (principal?.username is ()) {
                return prepareAuthorizationError("Username not set in runtime:Principal. Unable to authorize.");
            }
            return true;
        }
        return prepareAuthorizationError("runtime:Principal is not set in runtime:InvocationContext. Unable to authorize.");
    }

    # Tries to authorize the request.
    #
    # + authzCacheKey - Authorization cache key
    # + authCtxtScopes - Scopes set at the `runtime:Principal` of `runtime:InvocationContext`
    # + scopes - Array of scopes or Array of arrays of scopes
    # + return - true if authorization check is a success, else false
    function process(string authzCacheKey, string[] authCtxtScopes, string[]|string[][] scopes) returns boolean {
        var authorizedFromCache = auth:authorizeFromCache(authzCacheKey, self.positiveAuthzCache, self.negativeAuthzCache);
        if (authorizedFromCache is boolean) {
            return authorizedFromCache;
        } else {
            if (authCtxtScopes.length() > 0) {
                boolean authorized = auth:checkForScopeMatch(scopes, authCtxtScopes);
                auth:cacheAuthzResult(authorized, authzCacheKey, self.positiveAuthzCache, self.negativeAuthzCache);
                return authorized;
            }
            return false;
        }
    }
};
