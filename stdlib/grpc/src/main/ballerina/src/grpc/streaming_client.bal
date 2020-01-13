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

import ballerinax/java;

# Provides the gRPC streaming client actions for interacting with gRPC server.
public type StreamingClient client object {

    # Sends request message to the server.
    #
    # + res - The inbound request message.
    # + return - Returns an error if encounters an error while sending the response, returns nil otherwise.
    public remote function send(anydata res) returns Error? {
        return streamSend(self, res);
    }

    # Informs the server, caller finished sending messages.
    #
    # + return - Returns an error if encounters an error while sending the response, returns nil otherwise.
    public remote function complete() returns Error? {
        return streamComplete(self);
    }

    # Sends error message to the server.
    #
    # + statusCode - Error status code.
    # + message - Error message.
    # + return - Returns an error if encounters an error while sending the response, returns nil otherwise.
    public remote function sendError(int statusCode, string message) returns Error? {
        return streamSendError(self, statusCode, java:fromString(message));
    }
};

function streamSend(StreamingClient streamConnection, anydata res) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.streamingclient.FunctionUtils"
} external;

function streamComplete(StreamingClient streamConnection) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.streamingclient.FunctionUtils"
} external;

function streamSendError(StreamingClient streamConnection, int statusCode, handle message) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.streamingclient.FunctionUtils"
} external;
