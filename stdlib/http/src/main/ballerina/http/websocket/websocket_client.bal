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

# Represents a WebSocket client endpoint.
#
# + id - The connection id
# + negotiatedSubProtocol - The subprotocols negoriated with the server
# + isSecure - `true` if the connection is secure
# + isOpen - `true` if the connection is open
# + response - Represents the HTTP response
# + attributes - A map to store connection related attributes
public type WebSocketClient object {

    @readonly public string id;
    @readonly public string negotiatedSubProtocol;
    @readonly public boolean isSecure;
    @readonly public boolean isOpen;
    @readonly public Response response;
    @readonly public map attributes;

    private WebSocketConnector conn;
    private WebSocketClientEndpointConfig config;

    # Gets called when the endpoint is being initialize during package init time.
    #
    # + c - The `WebSocketClientEndpointConfig` of the endpoint
    public function init(WebSocketClientEndpointConfig c) {
        self.config = c;
        initEndpoint();
    }

    # Initializes the endpoint.
    public extern function initEndpoint();

    # Allows access to connector that the client endpoint uses.
    #
    # + return - The connector that client endpoint uses
    public function getCallerActions() returns (WebSocketConnector) {
        return conn;
    }

    # Stops the registered service.
    public function stop() {
        WebSocketConnector webSocketConnector = getCallerActions();
        check webSocketConnector.close(statusCode = 1001, reason = "going away", timeoutInSecs = 0);
    }
};

    # Configuration struct for WebSocket client endpoint.
    #
    # + url - The url of the server to connect to
    # + callbackService - The callback service for the client. Resources in this service gets called on receipt of messages from the server.
    # + subProtocols - Negotiable sub protocols for the client
    # + customHeaders - Custom headers which should be sent to the server
    # + idleTimeoutInSeconds - Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)
    # + readyOnConnect - true if the client is ready to recieve messages as soon as the connection is established. This is true by default. If changed to false the function ready() of the
    #                    `WebSocketClient`needs to be called once to start receiving messages.
    # + secureSocket - SSL/TLS related options
public type WebSocketClientEndpointConfig record {
    string url;
    typedesc? callbackService;
    string[] subProtocols;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
    boolean readyOnConnect = true;
    SecureSocket? secureSocket;
    !...
};
