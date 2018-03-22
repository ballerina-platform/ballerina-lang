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

package ballerina.net.http.authadaptor;

import ballerina/net.http;
import ballerina/auth.utils;
import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/log;

@Description {value:"Authorization cache name"}
const string AUTHZ_CACHE = "authz_cache";

permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
permissionstore:PermissionStore permissionStore =? <permissionstore:PermissionStore> fileBasedPermissionstore;
@Description {value:"AuthorizationChecker instance"}
authz:AuthzChecker authzChecker = authz:createChecker(permissionStore, utils:createCache(AUTHZ_CACHE));

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
                                                                    string scopeName, string resourceName) returns (boolean) {

    // TODO: extracting username and passwords are not required once the Ballerina SecurityContext is available
    // extract the header value
    string basicAuthHeaderValue = extractBasicAuthHeaderValue(req);
    if (basicAuthHeaderValue.length() == 0) {
        log:printError("Error in extracting basic authentication header");
        return false;
    }

    var credentials = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
    string username;
    match credentials {
        (string, string) creds => {
            var (user, _) = creds;
            username = user;
        }
        error err => {
            log:printErrorCause("Error in decoding basic authentication header", err);
            return false;
        }
    }

    // check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = username + "-" + resourceName + "-" + req.method;
    match getCachedAuthzValue(authzCacheKey) {
        boolean cachedAuthzValue => {
            log:printDebug("Authz cache hit for request URL: " + resourceName);
            return cachedAuthzValue;
        }
        any|null => {
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
    }
}

function getCachedAuthzValue (string cacheKey) returns (boolean | null) {
    match authzChecker.getCachedAuthzResult(cacheKey) {
        boolean isAuthorized => {
            return isAuthorized;
        }
        any|null => {
            return null;
        }
    }
}

@Description {value:"Checks if the provided request can be authorized"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authorize, else false"}
public function <HttpAuthzHandler httpAuthzHandler> canHandle (http:Request req) returns (boolean) {
    string basicAuthHeader;
    try {
        basicAuthHeader = req.getHeader(AUTH_HEADER);
    } catch (error e) {
       return false;
    }
    if (basicAuthHeader != null && basicAuthHeader.hasPrefix(AUTH_SCHEME_BASIC)) {
        return true;
    }
    return false;
}

