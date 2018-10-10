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
import ballerina/runtime;
import ballerina/log;
import ballerina/io;

# Representation of Authorization Handler for HTTP
#
# + authStoreProvider - `AuthStoreProvider` instance
# + authzCache - `Cache` instance, which is optional
public type HttpAuthzHandler object {
    public auth:AuthStoreProvider authStoreProvider;
    public cache:Cache? authzCache;

    public new (authStoreProvider, authzCache) {
    }

    # Checks if the request can be authorized
    #
    # + req - `Request` instance
    # + return - true if can be authorized, else false
    function canHandle(Request req) returns (boolean);

    # Tries to authorize the request
    #
    # + username - User name
    # + serviceName - `Service` name
    # + resourceName - `Resource` name
    # + method - HTTP method name
    # + scopes - Array of scopes
    # + return - true if authorization check is a success, else false
    function handle(string username, string serviceName, string resourceName, string method,
                                                                                    string[] scopes) returns (boolean);
    # Tries to retrieve authorization decision from the cached information, if any
    #
    # + authzCacheKey - Cache key
    # + return - true or false in case of a cache hit, nil in case of a cache miss
    function authorizeFromCache(string authzCacheKey) returns (boolean|());

    # Cached the authorization result
    #
    # + authzCacheKey - Cache key
    # + isAuthorized - boolean flag to indicate the authorization decision
    function cacheAuthzResult (string authzCacheKey, boolean isAuthorized);
};

function HttpAuthzHandler::handle (string username, string serviceName, string resourceName, string method,
                                                                                    string[] scopes) returns (boolean) {
    // first, check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = runtime:getInvocationContext().userPrincipal.username +
                                                    "-" + serviceName +  "-" + resourceName + "-" + method;
    match self.authorizeFromCache(authzCacheKey) {
        boolean isAuthorized => {
            return isAuthorized;
        }
        () => {
            // if there are scopes set in the AuthenticationContext already from a previous authentication phase, try to
            // match against those.
            string[] authCtxtScopes = runtime:getInvocationContext().userPrincipal.scopes;
            if (lengthof authCtxtScopes > 0) {
                boolean authorized = checkForScopeMatch(scopes, authCtxtScopes, resourceName, method);
                // cache authz result
                self.cacheAuthzResult(authzCacheKey, authorized);
                return authorized;
            } else {
                // no scopes found for user, try to retrieve using the auth provider
                string[] scopesFromAuthProvider = self.authStoreProvider.getScopes(username);
                if (lengthof scopesFromAuthProvider > 0) {
                    boolean authorized = checkForScopeMatch(scopes, scopesFromAuthProvider, resourceName, method);
                    // cache authz result
                    self.cacheAuthzResult(authzCacheKey, authorized);
                    return authorized;
                } else {
                    log:printDebug("No scopes found for user: " + username + " to access resource: " + resourceName +
                            ", method:" + method);
                    return false;
                }
            }
        }
    }
}

# Check whether the scopes of the user and scopes of resource matches.
#
# + resourceScopes - Scopes of resource
# + userScopes - Scopes of user
# + resourceName - Name of the `resource`
# + method - HTTP method name
# + return - true if there is a match between resource and user scopes, else false
function checkForScopeMatch (string[] resourceScopes, string[] userScopes, string resourceName, string method)
                                                                                                    returns boolean {
    boolean authorized = matchScopes(resourceScopes, userScopes);
    if (authorized) {
        log:printDebug("Successfully authorized to access resource: " + resourceName + ", method: " +
                method);
    } else {
        log:printDebug("Authorization failure for resource: " + resourceName + ", method: " + method);
    }
    return authorized;
}

function HttpAuthzHandler::authorizeFromCache(string authzCacheKey) returns (boolean|()) {
    try {
        match self.authzCache {
            cache:Cache cache => {
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

function HttpAuthzHandler::cacheAuthzResult (string authzCacheKey, boolean isAuthorized) {
    match self.authzCache {
        cache:Cache cache => {
            cache.put(authzCacheKey, isAuthorized);
        }
        () => {
            return;
        }
    }
}

# Tries to find a match between the two scope arrays
#
# + scopesOfResource - Scopes of resource
# + scopesForRequest - Scopes of the user
# + return - true if there is a match, else false
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

function HttpAuthzHandler::canHandle (Request req) returns (boolean) {
    if (runtime:getInvocationContext().userPrincipal.username.length() == 0) {
        log:printError("Username not set in auth context. Unable to authorize");
        return false;
    }
    return true;
}
