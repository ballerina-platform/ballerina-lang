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
    Representation of an API Listener.

    E{{}}
    F{{config}} SecureEndpointConfiguration instance
    F{{secureListener}} Secure HTTP Listener instance
}
public type APIListener object {

    public SecureEndpointConfiguration config;
    public SecureListener secureListener;

    new() {
        secureListener = new;
    }

    documentation {
        Gets called when the endpoint is being initialize during package init time.

        P{{c}} The `SecureEndpointConfiguration` of the endpoint
    }
    public function init(SecureEndpointConfiguration c);

    documentation {
        Gets called every time a service attaches itself to this endpoint. Also happens at package initialization.

        P{{serviceType}} The type of the service to be registered
    }
    public function register(typedesc serviceType);

    documentation {
        Starts the registered service.
    }
    public function start();

    documentation {
        Returns the connector that client code uses.

        R{{}} The connector that client code uses
    }
    public function getCallerActions() returns (Connection);

    documentation {
        Stops the registered service.
    }
    public function stop();
};

function APIListener::init(SecureEndpointConfiguration c) {
    self.secureListener.init(c);
}

function APIListener::register(typedesc serviceType) {
    self.secureListener.register(serviceType);
}

function APIListener::start() {
    self.secureListener.start();
}

function APIListener::getCallerActions() returns (APIListenerActions) {
    APIListenerActions apiListenerActions = new (self.secureListener.getCallerActions());
    return apiListenerActions;
}

function APIListener::stop() {
    self.secureListener.stop();
}

documentation {
    The caller actions for responding to client requests to api listener.
}
public type APIListenerActions object {

    public Connection httpCallerActions;

    documentation {
        The api listener caller actions initializer.

        P{{httpCallerActions}} HTTP caller actions reference
    }
    new (httpCallerActions) {}

    documentation {
        Sends the outbound response to the caller.

        P{{message}} The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} Returns an `error` if failed to respond
    }
    public function respond(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
        return httpCallerActions.respond(message);
    }

    documentation {
        Pushes a promise to the caller.

        P{{promise}} Push promise message
        R{{}} An `error` in case of failures
    }
    public function promise(PushPromise promise) returns error? {
        return httpCallerActions.promise(promise);
    }

    documentation {
        Sends a promised push response to the caller.

        P{{promise}} Push promise message
        P{{response}} The outbound response
        R{{}} An `error` in case of failures while responding with the promised response
    }
    public function pushPromisedResponse(PushPromise promise, Response response) returns error? {
        return httpCallerActions.pushPromisedResponse(promise, response);
    }

    documentation {
        Sends an upgrade request with custom headers.

        P{{headers}} A `map` of custom headers for handshake
    }
    public function acceptWebSocketUpgrade(map headers) returns WebSocketListener {
        return httpCallerActions.acceptWebSocketUpgrade(headers);
    }

    documentation {
        Cancels the handshake.

        P{{status}} Error Status code for cancelling the upgrade and closing the connection.
        This error status code need to be 4xx or 5xx else the default status code would be 400.
        P{{reason}} Reason for cancelling the upgrade
        R{{}} An `error` if an error occurs during cancelling the upgrade or nil
    }
    public function cancelWebSocketUpgrade(int status, string reason) returns error|() {
        return httpCallerActions.cancelWebSocketUpgrade(status, reason);
    }

    documentation {
        Sends a `100-continue` response to the caller.

        R{{}} Returns an `error` if failed to send the `100-continue` response
    }
    public function continue() returns error? {
        return httpCallerActions.continue();
    }

    documentation {
        Sends a redirect response to the user with the specified redirection status code.

        P{{response}} Response to be sent to the caller
        P{{code}} The redirect status code to be sent
        P{{locations}} An array of URLs to which the caller can redirect to
        R{{}} Returns an `error` if failed to send the redirect response
    }
    public function redirect(Response response, RedirectCode code, string[] locations) returns error? {
        return httpCallerActions.redirect(response, code, locations);
    }
};
