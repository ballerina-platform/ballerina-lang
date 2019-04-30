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

# Representation of Authorization Handler for HTTP
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

    # Checks if the request can be authorized
    #
    # + req - `Request` instance
    # + return - `true` if can be authorized, else `false`, or `error` if error occured
    function canHandle(Request req) returns boolean|error;

    # Tries to authorize the request
    #
    # + username - User name
    # + serviceName - `Service` name
    # + resourceName - `Resource` name
    # + method - HTTP method name
    # + scopes - Array of scopes
    # + return - true if authorization check is a success, else false
    function handle(string username, string serviceName, string resourceName, string method, string[] scopes) returns boolean;

    # Tries to retrieve authorization decision from the cached information, if any
    #
    # + authzCacheKey - Cache key
    # + return - true or false in case of a cache hit, nil in case of a cache miss
    function authorizeFromCache(string authzCacheKey) returns boolean?;

    # Cached the authorization result
    #
    # + authzCacheKey - Cache key
    # + authorized - boolean flag to indicate the authorization decision
    function cacheAuthzResult(string authzCacheKey, boolean authorized);
};

function AuthzHandler.handle(string username, string serviceName, string resourceName, string method, string[] scopes) returns boolean {
    // first, check in the cache. cache key is <username>-<service>-<resource>-<http method>-<scopes-separated-by-comma>,
    // since different resources can have different scopes
    string authzCacheKey = runtime:getInvocationContext().principal.userId + "-" + serviceName + "-" + resourceName + "-" + method;

    string[] authCtxtScopes = runtime:getInvocationContext().principal.scopes;
    //TODO: Make sure principal.scopes array is sorted and set to invocation context in order to prevent cache-misses that could happen due to ordering
    if (authCtxtScopes.length() > 0) {
        authzCacheKey += "-";
        foreach var authCtxtScope in authCtxtScopes {
            authzCacheKey += authCtxtScope + ",";
        }
    }

    var authorizedFromCache = self.authorizeFromCache(authzCacheKey);
    if (authorizedFromCache is boolean) {
        return authorizedFromCache;
    } else {
        // if there are scopes set in the AuthenticationContext already from a previous authentication phase, try to
        // match against those.
        if (authCtxtScopes.length() > 0) {
            boolean authorized = checkForScopeMatch(scopes, authCtxtScopes, resourceName, method);
            // cache authz result
            self.cacheAuthzResult(authzCacheKey, authorized);
            return authorized;
        }
    }
    return false;
}

# Check whether the scopes of the user and scopes of resource matches.
#
# + resourceScopes - Scopes of resource
# + userScopes - Scopes of user
# + resourceName - Name of the `resource`
# + method - HTTP method name
# + return - true if there is a match between resource and user scopes, else false
function checkForScopeMatch(string[] resourceScopes, string[] userScopes, string resourceName, string method) returns boolean {
    boolean authorized = matchScopes(resourceScopes, userScopes);
    if (authorized) {
        log:printDebug(function () returns string {
            return "Successfully authorized to access resource: " + resourceName + ", method: " + method;
        });
    } else {
        log:printDebug(function () returns string {
            return "Authorization failure for resource: " + resourceName + ", method: " + method;
        });
    }
    return authorized;
}

function AuthzHandler.authorizeFromCache(string authzCacheKey) returns boolean? {
    var positiveCacheResponse = self.positiveAuthzCache.get(authzCacheKey);
    if (positiveCacheResponse is boolean) {
        return true;
    }
    var negativeCacheResponse = self.negativeAuthzCache.get(authzCacheKey);
    if (negativeCacheResponse is boolean) {
        return false;
    }
}

function AuthzHandler.cacheAuthzResult(string authzCacheKey, boolean authorized) {
    if (authorized) {
        self.positiveAuthzCache.put(authzCacheKey, authorized);
    } else {
        self.negativeAuthzCache.put(authzCacheKey, authorized);
    }
}

# Tries to find a match between the two scope arrays
#
# + resourceScopes - Scopes of resource
# + userScopes - Scopes of the user
# + return - true if resourceScopes is a subset of userScopes, else false
function matchScopes(string[] resourceScopes, string[] userScopes) returns boolean {
    foreach var resourceScope in resourceScopes {
        boolean matched = false;
        foreach var userScope in userScopes {
            if (resourceScope == userScope) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            return false;
        }
    }
    return true;
}

function AuthzHandler.canHandle(Request req) returns boolean|error {
    if (runtime:getInvocationContext().principal.username.length() == 0) {
        return prepareError("Username not set in auth context. Unable to authorize.");
    }
    return true;
}
