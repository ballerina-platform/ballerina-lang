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

import ballerina/encoding;

# Represents a WebSocket connection in Ballerina. This includes all connection-oriented operations.
type WebSocketConnector object {
    private boolean isReady = false;

    # Push text to the connection.
    #
    # + data - Data to be sent, if byte[] it is converted to a UTF-8 string for sending
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - `error` if an error occurs when sending
    public function pushText(string|json|xml|boolean|int|float|byte|byte[] data, boolean finalFrame) returns error? {
        string text = "";
        if (data is byte) {
            text = <string>(<int>data);
        } else if (data is int) {
            text = <string>data;
        } else if (data is float) {
            text = <string>data;
        } else if (data is boolean) {
            text = <string>data;
        } else if (data is string) {
            text = data;
        } else if (data is xml) {
            text = string.convert(data);
        } else if (data is byte[]) {
            text = encoding:byteArrayToString(data);
        } else {
            text = data.toString();
        }
        return self.externPushText(text, finalFrame);
    }

    extern function externPushText(string text, boolean finalFrame) returns error?;

    # Push binary data to the connection.
    #
    # + data - Binary data to be sent
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return - `error` if an error occurs when sending
    public extern function pushBinary(byte[] data, boolean finalFrame) returns error?;

    # Ping the connection.
    #
    # + data - Binary data to be sent.
    # + return - `error` if an error occurs when sending
    public extern function ping(byte[] data) returns error?;

    # Send pong message to the connection.
    #
    # + data - Binary data to be sent
    # + return - `error` if an error occurs when sending
    public extern function pong(byte[] data) returns error?;

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
    public function close(int? statusCode = 1000, string? reason = (), int timeoutInSecs = 60) returns error? {
        if (statusCode is int) {
            if (statusCode <= 999 || statusCode >= 1004 && statusCode <= 1006 || statusCode >= 1012 &&
                statusCode <= 2999 || statusCode > 4999) {
                error err = error("Failed to execute close. Invalid status code: " + statusCode);
                return err;
            }
            return self.externClose(statusCode, reason ?: "", timeoutInSecs);
        } else {
            return self.externClose(-1, "", timeoutInSecs);
        }
    }

    extern function externClose(int statusCode, string reason, int timeoutInSecs) returns error?;

    # Called when the endpoint is ready to receive messages. Can be called only once per endpoint. For the
    # WebSocketListener can be called only in upgrade or onOpen resources.
    #
    # + return - `error` if an error occurs when sending
    public extern function ready() returns error?;

};
