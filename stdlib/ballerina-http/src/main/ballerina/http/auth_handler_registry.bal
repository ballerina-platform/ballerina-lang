package ballerina.http;

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

@Description {value:"Representation of the Http Auth Handler Registry"}
@Field {value:"httpAuthHandlers: map for auth handlers. key = auth provider id"}
public type AuthHandlerRegistry object {
    private {
        map<HttpAuthnHandler> httpAuthHandlers;
    }
    new () {
    }
    public function add (string id, HttpAuthnHandler authnHandler);
    public function get (string id) returns HttpAuthnHandler?;
    public function getAll () returns map<HttpAuthnHandler>;
    public function remove (string id);
    public function clear ();
};

@Description {value:"Add a HttpAuthnHandler to HttpAuthHandlerRegistry"}
@Param {value:"id: auth provider id"}
@Param {value:"authnHandler: HttpAuthnHandler instance"}
public function AuthHandlerRegistry::add (string id, HttpAuthnHandler authnHandler) {
    self.httpAuthHandlers[id] = authnHandler;
}

@Description {value:"Retrieve a HttpAuthnHandler from HttpAuthHandlerRegistry which corresponds to the given id"}
@Param {value:"id: auth provider id"}
@Return {value:"HttpAuthnHandler: HttpAuthnHandler instance or nil if not found"}
public function AuthHandlerRegistry::get (string id) returns HttpAuthnHandler? {
    if (self.httpAuthHandlers.hasKey(id)) {
        return self.httpAuthHandlers[id];
    }
    return ();
}

@Description {value:"Retrieve the HttpAuthnHandler map"}
@Return {value:"map<HttpAuthnHandler>: map of HttpAuthnHandler"}
public function AuthHandlerRegistry::getAll () returns map<HttpAuthnHandler> {
    return self.httpAuthHandlers;
}

@Description {value:"Removes a specific authn handler from the HttpAuthnHandler map"}
public function AuthHandlerRegistry::remove (string id) {
    _ = self.httpAuthHandlers.remove(id);
}

@Description {value:"Removes all authn handler from the HttpAuthnHandler map"}
public function AuthHandlerRegistry::clear () {
    self.httpAuthHandlers.clear();
}