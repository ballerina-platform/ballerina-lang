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


# Representation of an API Listener.
#
# + config - SecureEndpointConfiguration instance
# + secureListener - Secure HTTP Listener instance
public type APIListener object {

    public SecureEndpointConfiguration config;
    public SecureListener secureListener;

    new() {
        secureListener = new;
    }

    # Gets called when the endpoint is being initialize during package init time.
    #
    # + c - The `SecureEndpointConfiguration` of the endpoint
    public function init(SecureEndpointConfiguration c);

    # Gets called every time a service attaches itself to this endpoint. Also happens at package initialization.
    #
    # + serviceType - The type of the service to be registered
    public function register(typedesc serviceType);

    # Starts the registered service.
    public function start();

    # Returns the connector that client code uses.
    #
    # + return - The connector that client code uses
    public function getCallerActions() returns (Connection);

    # Stops the registered service.
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

# The caller actions for responding to client requests to api listener.
#
# + httpCallerActions - HTTP caller actions reference
public type APIListenerActions object {

    public Connection httpCallerActions;

    # The api listener caller actions initializer.
    #
    # + httpCallerActions - HTTP caller actions reference
    new (httpCallerActions) {}

    # Sends the outbound response to the caller.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - Returns an `error` if failed to respond
    public function respond(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
        return httpCallerActions.respond(message);
    }

    # Pushes a promise to the caller.
    #
    # + promise - Push promise message
    # + return - An `error` in case of failures
    public function promise(PushPromise promise) returns error? {
        return httpCallerActions.promise(promise);
    }

    # Sends a promised push response to the caller.
    #
    # + promise - Push promise message
    # + response - The outbound response
    # + return - An `error` in case of failures while responding with the promised response
    public function pushPromisedResponse(PushPromise promise, Response response) returns error? {
        return httpCallerActions.pushPromisedResponse(promise, response);
    }

    # Sends an upgrade request with custom headers.
    #
    # + headers - A `map` of custom headers for handshake
    # + return - WebSocket service endpoint
    public function acceptWebSocketUpgrade(map<string> headers) returns WebSocketListener {
        return httpCallerActions.acceptWebSocketUpgrade(headers);
    }

    # Cancels the handshake.
    #
    # + status - Error Status code for cancelling the upgrade and closing the connection.
    #            This error status code need to be 4xx or 5xx else the default status code would be 400.
    # + reason - Reason for cancelling the upgrade
    # + return - An `error` if an error occurs during cancelling the upgrade or nil
    public function cancelWebSocketUpgrade(int status, string reason) returns error|() {
        return httpCallerActions.cancelWebSocketUpgrade(status, reason);
    }

    # Sends a `100-continue` response to the caller.
    #
    # + return - Returns an `error` if failed to send the `100-continue` response
    public function continue() returns error? {
        return httpCallerActions.continue();
    }

    # Sends a redirect response to the user with the specified redirection status code.
    #
    # + response - Response to be sent to the caller
    # + code - The redirect status code to be sent
    # + locations - An array of URLs to which the caller can redirect to
    # + return - Returns an `error` if failed to send the redirect response
    public function redirect(Response response, RedirectCode code, string[] locations) returns error? {
        return httpCallerActions.redirect(response, code, locations);
    }
};
