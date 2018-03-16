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

import ballerina.net.http;
import ballerina.auth.utils;
import ballerina.auth.authz.permissionstore;
import ballerina.log;

@Description {value:"Authorization cache name"}
const string AUTHZ_CACHE = "authz_cache";
@Description {value:"Authentication header name"}
const string AUTH_HEADER = "Authorization";
@Description {value:"Basic authentication scheme"}
const string AUTH_SCHEME = "Basic";

@Description {value:"AuthorizationChecker instance"}
AuthzChecker authzChecker;

@Description {value:"Representation of Authorization Handler"}
@Field {value:"name: Authz handler name"}
public struct HttpAuthzHandler {
    string name = "default";
}

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"req: Request instance"}
@Param {value:"scopeName: name of the scope"}
@Param {value:"resourceName: name of the resource which is being accessed"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function <HttpAuthzHandler httpAuthzHandler> handle (http:Request req,
                                                                    string scopeName, string resourceName) (boolean) {

    // TODO: extracting username and passwords are not required once the Ballerina SecurityContext is available
    // extract the header value
    var basicAuthHeaderValue, err = utils:extractBasicAuthHeaderValue(req);
    if (err != null) {
        log:printErrorCause("Error in extracting basic authentication header", err);
        return false;
    }

    string username;
    username, _, err = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
    if (err != null) {
        log:printErrorCause("Error in decoding basic authentication header", err);
        return false;
    }
    if (authzChecker == null) {
        permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
        authzChecker = createChecker((permissionstore:PermissionStore)fileBasedPermissionstore,
                                     utils:createCache(AUTHZ_CACHE));
    }

    // check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = username + "-" + resourceName + "-" + req.method;
    any cachedAuthzResult = authzChecker.getCachedAuthzResult(authzCacheKey);
    if (cachedAuthzResult != null) {
        log:printDebug("Authz cache hit for request URL: " + resourceName);
        var isAuthorized, typeCastErr = (boolean)cachedAuthzResult;
        if (typeCastErr == null) {
            // no type cast error, return cached result.
            return isAuthorized;
        }
        // if a casting error occurs, clear the cache entry
        authzChecker.clearCachedAuthzResult(authzCacheKey);
    }
    log:printDebug("Authz cache miss for request URL: " + resourceName);

    boolean isAuthorized = authzChecker.check(username, scopeName);
    if (isAuthorized) {
        log:printDebug("Successfully authorized to access resource: " + resourceName);
    } else {
        log:printDebug("Insufficient permission to access resource: " + resourceName);
    }
    authzChecker.cacheAuthzResult(authzCacheKey, isAuthorized);
    return isAuthorized;
}

@Description {value:"Checks if the provided request can be authorized"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authorize, else false"}
public function <HttpAuthzHandler httpAuthzHandler> canHandle (http:Request req) (boolean) {
    string basicAuthHeader = req.getHeader(AUTH_HEADER);
    if (basicAuthHeader != null && basicAuthHeader.hasPrefix(AUTH_SCHEME)) {
        return true;
    }
    return false;
}
