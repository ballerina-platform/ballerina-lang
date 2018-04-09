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

package ballerina.http;

import ballerina/caching;
import ballerina/runtime;
import ballerina/log;
import ballerina/io;

@Description {value:"Auth provider instance"}
auth:ConfigAuthProvider configAuthProvider1 = new;
auth:AuthProvider authProvider1 = <auth:AuthProvider> configAuthProvider1;

@Description {value:"Representation of AuthzHandler"}
@Field {value:"authzCache: authorization cache instance"}
type HttpAuthzHandler object {
    public {
        caching:Cache? authzCache;
    }
    new (authzCache) {
    }
    function canHandle(Request req) returns (boolean);
    function handle(string username, string serviceName, string resourceName, string method,
                                                                                    string[] scopes) returns (boolean);
    function authorizeFromCache(string authzCacheKey) returns (boolean|());
    function cacheAuthzResult (string authzCacheKey, boolean isAuthorized);
};

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"username: user name"}
@Param {value:"serviceName: service name"}
@Param {value:"resourceName: resource name"}
@Param {value:"method: method names"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorization check is a success, else false"}
function HttpAuthzHandler::handle (string username, string serviceName, string resourceName, string method,
                                                                                    string[] scopes) returns (boolean) {
    // first, check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = runtime:getInvocationContext().authenticationContext.username +
                                                    "-" + serviceName +  "-" + resourceName + "-" + method;
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
            // if unable to get scopes from AuthenticationContext, use the auth provider to lookup the
            // scopes of the user, and check for a match.
            string[] scopesOfUser = authProvider1.getScopes(username);
            if (lengthof scopesOfUser > 0) {
                boolean authorized = matchScopes(scopes, scopesOfUser);
                if (authorized) {
                    log:printDebug("Successfully authorized to access resource: " + resourceName + ", method: " +
                        method);
                } else {
                    log:printDebug("Authorization failure for resource: " + resourceName + ", method: " + method);
                }
                // cache authz result
                self.cacheAuthzResult(authzCacheKey, authorized);
                return authorized;
            } else {
                // no scopes found for user, authorization failure
                return false;
            }
        }
    }
}

@Description {value:"Retrieves the cached authorization result if any, for the given basic auth header value"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Return {value:"boolean|(): cached entry, or nill in a cache miss"}
function HttpAuthzHandler::authorizeFromCache(string authzCacheKey) returns (boolean|()) {
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
function HttpAuthzHandler::cacheAuthzResult (string authzCacheKey, boolean isAuthorized) {
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

@Description {value:"Checks if the provided request can be authorized. This method will validate if the username is
already set in the authentication context. If not, the flow cannot continue."}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authorize, else false"}
function HttpAuthzHandler::canHandle (Request req) returns (boolean) {
    if (runtime:getInvocationContext().authenticationContext.username.length() == 0) {
        log:printError("Username not set in auth context. Unable to authorize");
        return false;
    }
    return true;
}
