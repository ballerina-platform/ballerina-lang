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

import ballerina/log;

@Description {value:"Representation of Authorization Handler chain"}
@Field {value:"authzHandlers: map of authz handlers"}
public type AuthzHandlerChain object {
    public {
        map authzHandlers;
    }
    new () {
    }
    public function handle (Request req, string[] scopes, string resourceName) returns (boolean);
};

@Description {value:"Creates an Authz handler chain"}
@Return {value:"AuthzHandlerChain: AuthzHandlerChain instance"}
public function createAuthzHandlerChain () returns (AuthzHandlerChain) {
    AuthzHandlerChain authzHandlerChain = new;
    // TODO: read the authz handlers from a config file and load them dynamically. currently its hardcoded.
    HttpAuthzHandler authzHandler = new;
    authzHandlerChain.authzHandlers[authzHandler.name] = authzHandler;
    return authzHandlerChain;
}

@Description {value:"Tries to perform the authorization check against any one of the available authorization handlers"}
@Param {value:"req: Request instance"}
@Param {value:"scopes: array of scope names"}
@Param {value:"resourceName: name of the resource which is being accessed"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function AuthzHandlerChain::handle (Request req, string[] scopes,
                                                              string resourceName) returns (boolean) {
    foreach currentAuthHandlerType, currentAuthHandler in self.authzHandlers {
        var authzHandler = check <HttpAuthzHandler> currentAuthHandler;
        if (authzHandler.canHandle(req)) {
            log:printDebug("trying to authorize with the authz handler: " + currentAuthHandlerType);
            return authzHandler.handle(req, scopes, resourceName);
        }
    }
    return false;
}
