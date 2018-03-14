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

package ballerina.auth;

import ballerina.net.http;
import ballerina.log;
import ballerina.auth.basic;
import ballerina.auth.jwtAuth;

@Description {value:"Representation of Authentication handler chain"}
public struct AuthnHandlerChain {
    map authnHandlers;
}

@Description {value:"Creates a Authentication handler chain"}
@Return {value:"AuthnHandlerChain: AuthnHandlerChain instance"}
public function createAuthnHandlerChain () (AuthnHandlerChain) {
    AuthnHandlerChain authnHandlerChain = {authnHandlers:{}};
    // TODO: read the authn handlers from a config file and load them dynamically. currently its hardcoded.
    basic:HttpBasicAuthnHandler httpAuthnHandler = {};
    jwtAuth:HttpJwtAuthnHandler jwtAuthnHandler = {};
    // add to map
    authnHandlerChain.authnHandlers[httpAuthnHandler.name] = httpAuthnHandler;
    authnHandlerChain.authnHandlers[jwtAuthnHandler.name] = jwtAuthnHandler;
    return authnHandlerChain;
}

@Description {value:"Tries to authenticate against any one of the available authentication handlers"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authenticated successfully, else false"}
public function <AuthnHandlerChain authnHandlerChain> handle (http:Request req) (boolean) {
    foreach currentAuthHandlerType, currentAuthHandler in authnHandlerChain.authnHandlers {
        var authnHandler, err = (HttpAuthnHandler)currentAuthHandler;
        if (err == null && authnHandler.canHandle(req)) {
            log:printDebug("trying to authenticate with the authn handler: " + currentAuthHandlerType);
            return authnHandler.handle(req);
        }
    }
    return false;
}
