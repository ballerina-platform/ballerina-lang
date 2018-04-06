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

import ballerina/auth.utils;
import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/log;
import ballerina/runtime;

@Description {value:"Authorization cache name"}
@final string AUTHZ_CACHE = "authz_cache";

permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore> fileBasedPermissionstore;
@Description {value:"AuthorizationChecker instance"}
authz:AuthzChecker authzChecker = authz:createChecker(permissionStore, utils:createCache(AUTHZ_CACHE));

@Description {value:"Representation of Authorization Handler"}
@Field {value:"name: Authz handler name"}
public type HttpAuthzHandler object {
    public {
        string name;
    }
    new () {
        name = "default";
    }
    public function canHandle (Request req) returns (boolean);

    public function handle (Request req, string[] scopes, string resourceName) returns (boolean);
};

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"req: Request instance"}
@Param {value:"scopes: names of the scopes"}
@Param {value:"resourceName: name of the resource which is being accessed"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function HttpAuthzHandler::handle (Request req, string[] scopes, string resourceName) returns (boolean) {
    return authzChecker.authorize(runtime:getInvocationContext().authenticationContext.username, resourceName,
                                                                                                req.method, scopes);
}

@Description {value:"Checks if the provided request can be authorized. This method will validate if the username is
already set in the authentication context. If not, the flow cannot continue."}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authorize, else false"}
public function HttpAuthzHandler::canHandle (Request req) returns (boolean) {
    if (runtime:getInvocationContext().authenticationContext.username.length() > 0) {
        log:printError("Username not set in auth context. Unable to authorize");
        return true;
    }
    return false;
}

