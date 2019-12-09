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

# Represents socket client and related remote functions.
#
# + remotePort - the remote port number to which this socket is connected
# + localPort - the local port number to which this socket is bound
# + remoteAddress - the remote IP address string in textual presentation to which the socket is connected
# + localAddress - the local IP address string in textual presentation to which the socket is bound
# + id - a unique identifier to identify each client
public type Client client object {

    private ClientConfig? config = ();
    public int remotePort = 0;
    public int localPort = 0;
    public string? remoteAddress = ();
    public string? localAddress = ();
    public int id = 0;

    public function __init(ClientConfig? clientConfig) {
        if (clientConfig is ClientConfig) {
            self.config = clientConfig;
            var initResult = initClientEndpoint(self, clientConfig);
            if (initResult is error) {
                panic initResult;
            }
            var startResult = startClient(self);
            if (startResult is error) {
                panic startResult;
            }
        }
        return ();
    }

    # Writes given data to the client socket.
    #
    # + content - - the content that wish to send to the client socket
    # + return - - number of bytes got written or an error if encounters an error while writing
    public remote function write(byte[] content) returns int|Error {
        return externWrite(self, content);
    }

    # Reads data from the client socket. If the data has the specified length, then wait until that number of bytes
    # are received from the client. Else, return the data available in the OS buffer.
    # In the case of the connection being closed by the client, then return either -1 or the data
    # that is currently available in the buffer.
    # Number of bytes returned will be < 0 if the client closes the connection.
    #
    # + length - - Positive integer. Represents the number of bytes which should be read
    # + return - - Content as a byte array and the number of bytes read or an error if encounters an error while reading
    public remote function read(public int length = -100) returns @tainted [byte[], int]|ReadTimedOutError {
        return externRead(self, length);
    }

    # Closes the client socket connection.
    #
    # + return - - an error if encounters an error while closing the connection or returns nil otherwise
    public remote function close() returns Error? {
        return closeClient(self);
    }

    # Shutdowns the further read from socket.
    #
    # + return - an error if encounters an error while shutdown the read from socket or returns nil otherwise
    public remote function shutdownRead() returns Error? {
        return externShutdownRead(self);
    }

    # Shutdowns the further write from socket.
    #
    # + return - an error if encounters an error while shutdown the write from socket or returns nil otherwise
    public remote function shutdownWrite() returns Error? {
        return externShutdownWrite(self);
    }
};

# Configuration for socket client endpoint.
#
# + host - Target service URL
# + port - Port number of the remote service
# + readTimeoutInMillis - Socket read timeout value to be used in milliseconds. Default is 300000 milliseconds (5 minutes)
# + callbackService - The callback service for the client. Resources in this service gets called on receipt of messages from the server.
public type ClientConfig record {|
    string host;
    int port;
    int readTimeoutInMillis = 300000;
    service callbackService?;
|};

function initClientEndpoint(Client clientObj, ClientConfig clientConfig) returns error? =
@java:Method {
    name: "initEndpoint",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function startClient(Client clientObj) returns error? =
@java:Method {
    name: "start",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function externWrite(Client clientObj, byte[] content) returns int|Error =
@java:Method {
    name: "write",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function externRead(Client clientObj, int length) returns [byte[], int]|ReadTimedOutError =
@java:Method {
    name: "read",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function closeClient(Client clientObj) returns Error? =
@java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function externShutdownRead(Client clientObj) returns Error? =
@java:Method {
    name: "shutdownRead",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;

function externShutdownWrite(Client clientObj) returns Error? =
@java:Method {
    name: "shutdownWrite",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ClientActions"
} external;
