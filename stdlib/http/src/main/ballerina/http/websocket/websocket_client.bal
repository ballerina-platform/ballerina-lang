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
# + negotiatedSubProtocol - The subprotocols that are negotiated with the server
# + isSecure - `true` if the connection is secure
# + isOpen - `true` if the connection is open
# + response - Represents the HTTP response
# + attributes - A map to store connection related attributes
public type WebSocketClient client object {

    public string id = "";
    public string negotiatedSubProtocol = "";
    public boolean isSecure = false;
    public boolean isOpen = false;
    public Response response = new;
    public map<any> attributes = {};

    private WebSocketConnector conn = new;
    private string url = "";
    private WebSocketClientEndpointConfig config = {};

    # Initializes the client when called.
    #
    # + c - The `WebSocketClientEndpointConfig` of the endpoint
    public function __init(string url, WebSocketClientEndpointConfig? config = ()) {
        self.url = url;
        self.config = config ?: {};
        self.initEndpoint();
    }

    # Initializes the endpoint.
    public extern function initEndpoint();

    # Push text to the connection.
    #
    # + data - Data to be sent, if byte[] it is converted to a UTF-8 string for sending
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - `error` if an error occurs when sending
    public remote function pushText(string|json|xml|boolean|int|float|byte|byte[] data, boolean finalFrame = true) returns error? {
        return self.conn.pushText(data, finalFrame);
    }

    # Push binary data to the connection.
    #
    # + data - Binary data to be sent
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return - `error` if an error occurs when sending
    public remote function pushBinary(byte[] data, boolean finalFrame = true) returns error? {
        return self.conn.pushBinary(data, finalFrame);
    }

    # Ping the connection.
    #
    # + data - Binary data to be sent.
    # + return - `error` if an error occurs when sending
    public remote function ping(byte[] data) returns error? {
        return self.conn.ping(data);
    }

    # Send pong message to the connection.
    #
    # + data - Binary data to be sent
    # + return - `error` if an error occurs when sending
    public remote function pong(byte[] data) returns error? {
        return self.conn.pong(data);
    }

    # Close the connection.
    #
    # + statusCode - Status code for closing the connection
    # + reason - Reason for closing the connection
    # + timeoutInSecs - Time to wait for the close frame to be received from the remote endpoint before closing the
    #                   connection. If the timeout exceeds, then the connection is terminated even though a close frame
    #                   is not received from the remote endpoint. If the value < 0 (e.g., -1), then the connection waits
    #                   until a close frame is received. If WebSocket frame is received from the remote endpoint,
    #                   within waiting period the connection is terminated immediately.
    # + return - `error` if an error occurs when sending
    public remote function close(int? statusCode = 1000, string? reason = (), int timeoutInSecs = 60) returns error? {
        return self.conn.close(statusCode = statusCode, reason = reason, timeoutInSecs = timeoutInSecs);
    }

    # Called when the endpoint is ready to receive messages. Can be called only once per endpoint. For the
    # WebSocketListener can be called only in upgrade or onOpen resources.
    #
    # + return - `error` if an error occurs when sending
    public remote function ready() returns error? {
        return self.conn.ready();
    }
};

# Configuration for the WebSocket client endpoint.
#
# + callbackService - The callback service for the client. Resources in this service gets called on receipt of messages
#                     from the server.
# + subProtocols - Negotiable sub protocols for the client
# + customHeaders - Custom headers which should be sent to the server
# + idleTimeoutInSeconds - Idle timeout of the client. Upon timeout, `onIdleTimeout` resource (if defined) in the client
#                          service will be triggered.
# + readyOnConnect - `true` if the client is ready to receive messages as soon as the connection is established.
#                    This is true by default. If changed to false the function ready() of the
#                    `WebSocketClient`needs to be called once to start receiving messages.
# + secureSocket - SSL/TLS related options
# + maxFrameSize - The maximum payload size of a WebSocket frame in bytes.
#                  If this is not set or is negative  or zero the default frame size of 65536 will be used.
public type WebSocketClientEndpointConfig record {
    service? callbackService = ();
    string[] subProtocols = [];
    map<string> customHeaders = {};
    int idleTimeoutInSeconds = -1;
    boolean readyOnConnect = true;
    SecureSocket? secureSocket = ();
    int maxFrameSize = 0;
    !...;
};
