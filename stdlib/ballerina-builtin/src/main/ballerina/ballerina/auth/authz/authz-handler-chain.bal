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
import ballerina.log;

@Description {value:"Representation of Authorization Handler chain"}
@Field {value:"authzHandlers: map of authz handlers"}
public struct AuthzHandlerChain {
    map authzHandlers;
}

@Description {value:"Creates an Authz handler chain"}
@Return {value:"AuthzHandlerChain: AuthzHandlerChain instance"}
public function createAuthzHandlerChain () (AuthzHandlerChain) {
    AuthzHandlerChain authzHandlerChain = {authzHandlers:{}};
    // TODO: read the authz handlers from a config file and load them dynamically. currently its hardcoded.
    HttpAuthzHandler authzHandler = {};
    authzHandlerChain.authzHandlers[authzHandler.name] = authzHandler;
    return authzHandlerChain;
}

@Description {value:"Tries to perform the authorization check against any one of the available authorization handlers"}
@Param {value:"req: Request instance"}
@Param {value:"scopeName: name of the scope"}
@Param {value:"resourceName: name of the resource which is being accessed"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function <AuthzHandlerChain authzHandlerChain> handle (http:Request req, string scopeName,
                                                              string resourceName) (boolean) {
    foreach currentAuthHandlerType, currentAuthHandler in authzHandlerChain.authzHandlers {
        var authzHandler, err = (HttpAuthzHandler)currentAuthHandler;
        if (err == null && authzHandler.canHandle(req)) {
            log:printDebug("trying to authorize with the authz handler: " + currentAuthHandlerType);
            return authzHandler.handle(req, scopeName, resourceName);
        }
    }
    return false;
}
