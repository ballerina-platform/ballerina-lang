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

import ballerina/java;

# Provides the gRPC remote functions for interacting with caller.
#
# + instanceId - The connection id
# + remoteDetails - The remote details
# + local - The local details
public type Caller client object {

    public Remote remoteDetails = {};
    public Local local = {};

    private int instanceId = -1;

# Returns the unique identification of the caller.
# ```ballerina
# int result = caller.getId();
# ```
#
# + return - caller ID
    public function getId() returns int {
        return self.instanceId;
    }

# Sends the outbound response to the caller.
# ```ballerina
# grpc:Error? err = caller->send(message, headers);
# ```
#
# + res - - The outbound response message
# + headers - - Optional headers parameter. The header values are passed only if needed. The default value is `()`
# + return - - A `grpc:Error` if an error occurs while sending the response or else `()`
    public remote function send(anydata res, Headers? headers = ()) returns Error? {
        return externSend(self, res, headers);
    }

# Informs the caller, when the server has sent all the messages.
# ```ballerina
# grpc:Error? result = caller->complete();
# ```
#
# + return - A `grpc:Error` if an error occurs while sending the response or else `()`
    public remote function complete() returns Error? {
        return externComplete(self);
    }

# Checks whether the connection is closed by the caller.
# ```ballerina
# boolean result = caller.isCancelled();
# ```
#
# + return - True if the caller has already closed the connection or else false
    public function isCancelled() returns boolean {
        return externIsCancelled(self);
    }

# Sends a server error to the caller.
# ```ballerina
# grpc:Error? result = caller->sendError(grpc:ABORTED, "Operation aborted", headers);
# ```
#
# + statusCode - Error status code
# + message - Error message
# + headers - Optional headers parameter. The header values are passed only if needed. The default value is `()`
# + return - A `grpc:Error` if an error occurs while sending the response or else `()`
    public remote function sendError(int statusCode, string message, Headers? headers = ()) returns Error? {
        return externSendError(self, statusCode, java:fromString(message), headers);
    }
};

function externSend(Caller endpointClient, anydata res, Headers? headers) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.caller.FunctionUtils"
} external;

function externComplete(Caller endpointClient) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.caller.FunctionUtils"
} external;

function externIsCancelled(Caller endpointClient) returns boolean =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.caller.FunctionUtils"
} external;

function externSendError(Caller endpointClient, int statusCode, handle message, Headers? headers) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.caller.FunctionUtils"
} external;

# Presents a read-only view of the remote address.
#
# + host - The remote host name/IP
# + port - The remote port
public type Remote record {|
    string host = "";
    int port = 0;
|};

# Presents a read-only view of the local address.
#
# + host - The local host name/IP
# + port - The local port
public type Local record {|
    string host = "";
    int port = 0;
|};
