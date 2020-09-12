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

import ballerina/auth;
import ballerina/cache;
import ballerina/log;
import ballerina/runtime;

# Representation of Authorization Handler for HTTP.
#
# + positiveAuthzCache - The `cache:Cache` instance, which holds positive authorizations
# + negativeAuthzCache - The `cache:Cache` instance, which holds negative authorizations
public class AuthzHandler {

    public cache:Cache? positiveAuthzCache;
    public cache:Cache? negativeAuthzCache;

    # Initializes the `AuthzHandler` object.
    #
    # + positiveAuthzCache - The `cache:Cache` instance, which holds positive authorizations
    # + negativeAuthzCache - The `cache:Cache` instance, which holds negative authorizations
    public function init(cache:Cache? positiveAuthzCache, cache:Cache? negativeAuthzCache) {
        self.positiveAuthzCache = positiveAuthzCache;
        self.negativeAuthzCache = negativeAuthzCache;
    }

    # Checks if the request can be authorized.
    #
    # + req - The `http:Request` instance
    # + return - `true` if it can be authorized, `false` otherwise, or else an `http:AuthorizationError` if an error occurred
    function canProcess(Request req) returns boolean|AuthorizationError {
        if (auth:getInvocationContext()?.userId is ()) {
            return prepareAuthorizationError("UserId not set in auth:InvocationContext. Unable to authorize.");
        }
        return true;
    }

    # Authorizes the request.
    #
    # + scopes - An array of scopes or an array consisting of arrays of scopes of the listener or resource or service
    # + return - `true` if authorization check is a success, else `false`
    function process(Scopes scopes) returns boolean {
        // since different resources can have different scopes,
        // cache key is <username>-<service>-<resource>-<http-method>-<scopes-separated-by-comma>
        string serviceName = runtime:getInvocationContext().attributes[SERVICE_NAME].toString();
        string resourceName = runtime:getInvocationContext().attributes[RESOURCE_NAME].toString();
        string requestMethod = runtime:getInvocationContext().attributes[REQUEST_METHOD].toString();

        string userId = auth:getInvocationContext()?.userId ?: "";    // This is already validated in the `canProcess` function.
        string[] userScopes = auth:getInvocationContext()?.scopes ?: [];
        string authzCacheKey = generateAuthzCacheKey(userId, userScopes, serviceName, resourceName, requestMethod);

        boolean authorized = auth:checkForScopeMatch(scopes, userScopes, authzCacheKey, self.positiveAuthzCache,
                                                     self.negativeAuthzCache);

        if (authorized) {
            log:printDebug(function () returns string {
                return "Successfully authorized to access resource: " + serviceName + ", method: " + requestMethod;
            });
        } else {
            log:printDebug(function () returns string {
                return "Authorization failure for resource: " + serviceName + ", method: " + requestMethod;
            });
        }
        return authorized;
    }
}

function generateAuthzCacheKey(string username, string[] userScopes, string serviceName, string resourceName,
                               string requestMethod) returns string {
    string authzCacheKey = username + "-" + serviceName + "-" + resourceName + "-" + requestMethod;
    if (userScopes.length() > 0) {
        authzCacheKey += "-";
        foreach string userScope in userScopes {
            authzCacheKey += userScope.trim() + ",";
        }
    }
    return authzCacheKey;
}
