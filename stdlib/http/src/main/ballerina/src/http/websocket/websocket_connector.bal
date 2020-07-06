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

import ballerina/lang.'string as strings;
import ballerina/java;

# Represents a WebSocket connection in Ballerina. This includes all connection-oriented operations.
type WebSocketConnector object {
    private boolean isReady = false;

    # Pushes text to the connection. If an error occurs while sending the text message to the connection, that message
    # will be lost.
    #
    # + data - Data to be sent. If it is a byte[], it is converted to a UTF-8 string for sending
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - An `error` if an error occurs when sending
    public function pushText(string|json|xml|boolean|int|float|byte|byte[] data, boolean finalFrame)
    returns WebSocketError? {
        string text = "";
        if (data is byte[]) {
            string|error result = strings:fromBytes(data);

            if (result is error) {
                return WsGenericError("Error occurred during the text message creation", result);
            }
            text = <string> result;
        } else if (data is json) {
            text = data.toJsonString();
        } else {
            text = data.toString();
        }
        return externPushText(self, text, finalFrame);
    }

    # Pushes binary data to the connection. If an error occurs while sending the binary message to the connection,
    # that message will be lost.
    #
    # + data - Binary data to be sent
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - An `error` if an error occurs when sending
    public function pushBinary(byte[] data, boolean finalFrame) returns WebSocketError? {
        return externPushBinary(self, data, finalFrame);
    }

    # Pings the connection. If an error occurs while sending the ping frame to the connection, that frame will be lost.
    #
    # + data - Binary data to be sent
    # + return  - An `error` if an error occurs when sending
    public function ping(byte[] data) returns WebSocketError? {
        return externPing(self, data);
    }

    # Sends a pong message to the connection. If an error occurs while sending the pong frame to the connection, that
    # frame will be lost.
    #
    # + data - Binary data to be sent
    # + return  - An `error` if an error occurs when sending
    public function pong(byte[] data) returns WebSocketError? {
        return externPong(self, data);
    }

    # Calls when the endpoint is ready to receive messages. It can be called only once per endpoint. The
    # WebSocketListener can be called only in the `upgrade` or `onOpen` resources.
    #
    # + return - An `error` if an error occurs when sending
    public function ready() returns WebSocketError? {
        return externReady(self);
    }

    # Closes the connection.
    #
    # + statusCode - Status code for closing the connection
    # + reason - Reason for closing the connection
    # + timeoutInSecs - Time to wait for the close frame to be received from the remote endpoint before closing the
    #                   connection. If the timeout exceeds, then the connection is terminated even though a close frame
    #                   is not received from the remote endpoint. If the value < 0 (e.g., -1), then the connection waits
    #                   until a close frame is received. If WebSocket frame is received from the remote endpoint
    #                   within the waiting period, the connection is terminated immediately.
    # + return - An `error` if an error occurs when sending
    public function close(int? statusCode = 1000, string? reason = (), int timeoutInSecs = 60) returns WebSocketError? {
        if (statusCode is int) {
            if (statusCode <= 999 || statusCode >= 1004 && statusCode <= 1006 || statusCode >= 1012 &&
                statusCode <= 2999 || statusCode > 4999) {
                string errorMessage = "Failed to execute close. Invalid status code: " + statusCode.toString();
                return WsConnectionClosureError(errorMessage);
            }
            return externClose(self, statusCode, reason is () ? "" : reason, timeoutInSecs);
        } else {
            return externClose(self, -1, "", timeoutInSecs);
        }
    }
};

function externPushText(WebSocketConnector wsConnector, string text, boolean finalFrame) returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.WebSocketConnector"
} external;

function externPushBinary(WebSocketConnector wsConnector, byte[] data, boolean finalFrame) returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.WebSocketConnector",
    name: "pushBinary"
} external;

function externPing(WebSocketConnector wsConnector, byte[] data) returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.WebSocketConnector",
    name: "ping"
} external;

function externPong(WebSocketConnector wsConnector, byte[] data) returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.WebSocketConnector",
    name: "pong"
} external;

function externClose(WebSocketConnector wsConnector, int statusCode, string reason, int timeoutInSecs)
                     returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.Close"
} external;

function externReady(WebSocketConnector wsConnector) returns WebSocketError? = @java:Method {
    class: "org.ballerinalang.net.http.actions.websocketconnector.Ready",
    name: "ready"
} external;
