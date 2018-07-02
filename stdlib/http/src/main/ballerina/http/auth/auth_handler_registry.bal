
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

documentation {
    Representation of the Http Auth Handler Registry.

    F{{httpAuthHandlers}} map for auth handlers. key = auth provider id
}
public type AuthHandlerRegistry object {
    private map<HttpAuthnHandler> httpAuthHandlers;

    public new () {
    }

    documentation {
        Add an HttpAuthnHandler to HttpAuthHandlerRegistry

        P{{id}} Auth provider id
        P{{authnHandler}} HttpAuthnHandler instance
    }
    public function add (string id, HttpAuthnHandler authnHandler);

    documentation {
        Retrieves an HttpAuthnHandler from HttpAuthHandlerRegistry which corresponds to the given id

        P{{id}} Auth provider id
        R{{HttpAuthnHandler}} HttpAuthnHandler instance or nil if not found
    }
    public function get (string id) returns HttpAuthnHandler?;


    documentation {
        Retrieve the HttpAuthnHandler map

        R{{}} map of HttpAuthnHandler
    }
    public function getAll () returns map<HttpAuthnHandler>;

    documentation {
        Removes a specific authn handler from the HttpAuthnHandler map
    }
    public function remove (string id);

    documentation {
        Removes all authn handler from the HttpAuthnHandler map
    }
    public function clear ();
};

function AuthHandlerRegistry::add (string id, HttpAuthnHandler authnHandler) {
    self.httpAuthHandlers[id] = authnHandler;
}

function AuthHandlerRegistry::get (string id) returns HttpAuthnHandler? {
    if (self.httpAuthHandlers.hasKey(id)) {
        return self.httpAuthHandlers[id];
    }
    return ();
}

function AuthHandlerRegistry::getAll () returns map<HttpAuthnHandler> {
    return self.httpAuthHandlers;
}

function AuthHandlerRegistry::remove (string id) {
    _ = self.httpAuthHandlers.remove(id);
}

function AuthHandlerRegistry::clear () {
    self.httpAuthHandlers.clear();
}