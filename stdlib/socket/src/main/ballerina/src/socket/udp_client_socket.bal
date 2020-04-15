// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Initializes the UDP client based on the provided configurations.
#
# + localPort - The local port number to which this socket is bound
# + localAddress - The local IP address string in textual presentation to which the socket is bound
# + interface - The network interface to bind
# + id - A unique identifier to identify each client
public type UdpClient client object {

    private Address? localAddress = ();
    public int localPort = 0;
    public string? interface = ();
    public int id = 0;

    # Initializes the UDP client based on the provided configurations.
    #
    # + localAddress - Local binding of the interface and port
    # + config - The configurations for the UDP client
    public function __init(Address? localAddress = (), UdpClientConfig? config = ()) {
        UdpClientConfig configuration = config ?: {};
        self.localAddress = localAddress;
        var initResult = initUdpClientEndpoint(self, localAddress, configuration);
        if (initResult is error) {
            panic initResult;
        }
        if (localAddress is Address) {
            self.localAddress = localAddress;
        }
    }

# Sends the given data to the specified remote client.
# ```ballerina
# int|socket:Error result = socketClient->sendTo(c1, {host: "localhost", port: 48826});
# ```
#
# + content - The content to be sent to the client socket
# + address - The address of the remote client socket
# + return - The number of bytes got written or else a `socket:Error` if the given data can't be sent
    public remote function sendTo(byte[] content, Address address) returns int|Error {
        return udpClientSendTo(self, content, address);
    }

# Reads data from the remote client. If the data has the specified length, then it waits until that number of bytes
# are received from the client. Else, it returns the data available in the OS buffer or waits until the data is received.
# If the request length is lesser than the data in the buffer, then the rest will be discarded.
# ```ballerina
# [byte[], int, Address]|ReadTimedOutError result = socketClient->receiveFrom();
# ```
#
# + length - Represents the number of bytes, which should be read
# + return - The content as a byte array, the number of bytes read, the address of the sender,
#            or else a `socket:Error` if the data can't be read from the client
    public remote function receiveFrom(int length = -100) returns [byte[], int, Address]|ReadTimedOutError {
        return externReceiveFrom(self, length);
    }

# Closes the client socket connection.
# ```ballerina
# socket:Error? closeResult = socketClient->close();
# ```
#
# + return - A `socket:Error` if it can't close the connection or else `()`
    public remote function close() returns Error? {
        return closeUdpClient(self);
    }
};

# This represent the IP socket address.
#
# + host - The hostname of the Socket Address
# + port - The port number of the Socket Address
public type Address record {|
    string host?;
    int port;
|};

# Configurations for the UDP client.
#
# + readTimeoutInMillis - The socket reading timeout value to be used in milliseconds. If this is not set,
#                         the default value of 300000 milliseconds (5 minutes) will be used.
public type UdpClientConfig record {|
    int readTimeoutInMillis = 300000;
|};

function initUdpClientEndpoint(UdpClient udpClient, Address? localAddress, UdpClientConfig config) returns error? =
@java:Method {
    name: "initEndpoint",
    class: "org.ballerinalang.stdlib.socket.endpoint.udp.ClientActions"
} external;

function closeUdpClient(UdpClient udpClient) returns Error? =
@java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.socket.endpoint.udp.ClientActions"
} external;

function externReceiveFrom(UdpClient udpClient, int length) returns [byte[], int, Address]|ReadTimedOutError =
@java:Method {
    name: "receiveFrom",
    class: "org.ballerinalang.stdlib.socket.endpoint.udp.ClientActions"
} external;

function udpClientSendTo(UdpClient udpClient, byte[] content, Address address) returns int|Error =
@java:Method {
    name: "sendTo",
    class: "org.ballerinalang.stdlib.socket.endpoint.udp.ClientActions"
} external;
