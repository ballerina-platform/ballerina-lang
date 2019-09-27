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
public type WebSocketClient client object {
    // This is to keep track if the ready() function has been called
    private boolean isReady = false;

    private string id = "";
    private string? negotiatedSubProtocol = ();
    private boolean secure = false;
    private boolean open = false;
    private Response? response = ();
    private map<any> attributes = {};

    private WebSocketConnector conn = new;
    private string url = "";
    private WebSocketClientConfiguration config = {};

    # Initializes the client when called.
    #
    # + c - The `WebSocketClientConfiguration` of the endpoint
    public function __init(string url, public WebSocketClientConfiguration? config = ()) {
        self.url = url;
        self.config = config ?: {};
        self.initEndpoint();
    }

    # Initializes the endpoint.
    public function initEndpoint() = external;

    # Push text to the connection.
    #
    # + data - Data to be sent, if byte[] it is converted to a UTF-8 string for sending
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - `error` if an error occurs when sending
    public remote function pushText(string|json|xml|boolean|int|float|byte|byte[] data, 
    public boolean finalFrame = true) returns WebSocketError? {
        return self.conn.pushText(data, finalFrame);
    }

    # Push binary data to the connection.
    #
    # + data - Binary data to be sent
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return - `error` if an error occurs when sending
    public remote function pushBinary(byte[] data, public boolean finalFrame = true) returns error? {
        return self.conn.pushBinary(data, finalFrame);
    }

    # Ping the connection.
    #
    # + data - Binary data to be sent.
    # + return - `error` if an error occurs when sending
    public remote function ping(byte[] data) returns WebSocketError? {
        return self.conn.ping(data);
    }

    # Send pong message to the connection.
    #
    # + data - Binary data to be sent
    # + return - `error` if an error occurs when sending
    public remote function pong(byte[] data) returns WebSocketError? {
        return self.conn.pong(data);
    }

    # Close the connection.
    #
    # + statusCode - Status code for closing the connection
    # + reason - Reason for closing the connection
    # + timeoutInSeconds - Time to wait for the close frame to be received from the remote endpoint before closing the
    #                   connection. If the timeout exceeds, then the connection is terminated even though a close frame
    #                   is not received from the remote endpoint. If the value < 0 (e.g., -1), then the connection waits
    #                   until a close frame is received. If WebSocket frame is received from the remote endpoint,
    #                   within waiting period the connection is terminated immediately.
    # + return - `error` if an error occurs when sending
    public remote function close(public int? statusCode = 1000, public string? reason = (),
        public int timeoutInSeconds = 60) returns WebSocketError? {
        return self.conn.close(statusCode, reason, timeoutInSeconds);
    }

    # Called when the client is ready to receive messages. Can be called only once.
    #
    # + return - `error` if an error occurs when sending
    public remote function ready() returns WebSocketError? = external;

    # Sets a connection related attribute.
    #
    # + key - key that identifies the attribute
    # + value - value of the attribute
    public function setAttribute(string key, any value) {
        self.attributes[key] = value;
    }

    # Gets connection related attribute if any.
    #
    # + key - the key to identify the attribute.
    # + return - the attribute related to the given key or `nil`
    public function getAttribute(string key) returns any {
        return self.attributes[key];
    }

    # Removes connection related attribute if any.
    #
    # + key - the key to identify the attribute.
    # + return - the attribute related to the given key or `nil`
    public function removeAttribute(string key) returns any {
        return self.attributes.remove(key);
    }

    # Gives the connection id associated with this connection.
    #
    # + return - the unique id associated with the connection
    public function getConnectionId() returns string {
        return self.id;
    }

    # Gives the subprotocol if any that is negotiated with the client.
    #
    # + return - The subprotocol if any negotiated with the client or `nil`
    public function getNegotiatedSubProtocol() returns string? {
        return self.negotiatedSubProtocol;
    }

    # Gives the secured status of the connection.
    #
    # + return - `true` if the connection is secure.
    public function isSecure() returns boolean {
        return self.secure;
    }

    # Gives the open or closed status of the connection.
    #
    # + return - `true` if the connection is open
    public function isOpen() returns boolean {
        return self.open;
    }

    # Gives the HTTP response if any received for the client handshake request.
    #
    # + return - the HTTP response received for the client handshake request
    public function getHttpResponse() returns Response? {
        return self.response;
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
# + webSocketCompressionEnabled - Enable support for compression in WebSocket
public type WebSocketClientConfiguration record {|
    service? callbackService = ();
    string[] subProtocols = [];
    map<string> customHeaders = {};
    int idleTimeoutInSeconds = -1;
    boolean readyOnConnect = true;
    ClientSecureSocket? secureSocket = ();
    int maxFrameSize = 0;
    boolean webSocketCompressionEnabled = true;
|};
