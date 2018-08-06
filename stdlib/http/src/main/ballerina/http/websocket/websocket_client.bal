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
    Represents a WebSocket client endpoint.

    F{{id}} The connection id
    F{{negotiatedSubProtocol}} The subprotocols negoriated with the server
    F{{isSecure}} `true` if the connection is secure
    F{{isOpen}} `true` if the connection is open
    F{{attributes}} A map to store connection related attributes
}
public type WebSocketClient object {

    @readonly public string id;
    @readonly public string negotiatedSubProtocol;
    @readonly public boolean isSecure;
    @readonly public boolean isOpen;
    @readonly public Response response;
    @readonly public map attributes;

    private WebSocketConnector conn;
    private WebSocketClientEndpointConfig config;

    documentation {
        Gets called when the endpoint is being initialize during package init time.

        P{{c}} The `WebSocketClientEndpointConfig` of the endpoint
    }
    public function init(WebSocketClientEndpointConfig c) {
        self.config = c;
        initEndpoint();
    }

    documentation {
        Initializes the endpoint.
    }
    public extern function initEndpoint();

    documentation {
        Allows access to connector that the client endpoint uses.

        R{{}} The connector that client endpoint uses
    }
    public function getCallerActions() returns (WebSocketConnector) {
        return conn;
    }

    documentation {
        Stops the registered service.
    }
    public function stop() {
        WebSocketConnector webSocketConnector = getCallerActions();
        check webSocketConnector.close(1001, "going away", timeoutInSecs = 0);
    }
};

documentation {
        Configuration struct for WebSocket client endpoint.

        F{{url}} The url of the server to connect to
        F{{callbackService}} The callback service for the client. Resources in this service gets called on receipt of messages from the server.
        F{{subProtocols}} Negotiable sub protocols for the client
        F{{customHeaders}} Custom headers which should be sent to the server
        F{{idleTimeoutInSeconds}} Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)
        F{{readyOnConnect}}
 true if the client is ready to recieve messages as soon as the connection is established. This is true by default. If changed to false the function ready() of the
`WebSocketClient`needs to be called once to start receiving messages.
        F{{secureSocket}} SSL/TLS related options
}
public type WebSocketClientEndpointConfig record {
    string url,
    typedesc? callbackService,
    string[] subProtocols,
    map<string> customHeaders,
    int idleTimeoutInSeconds = -1,
    boolean readyOnConnect = true,
    SecureSocket? secureSocket,
};
