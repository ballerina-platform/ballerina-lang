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

# Represents socket client and related remote functions.
#
# + remotePort - the remote port number to which this socket is connected
# + localPort - the local port number to which this socket is bound
# + remoteAddress - the remote IP address string in textual presentation to which the socket is connected
# + localAddress - the local IP address string in textual presentation to which the socket is bound
# + id - a unique identifier to identify each client
public type Client client object {

    private ClientConfig config;
    public int remotePort = 0;
    public int localPort = 0;
    public string? remoteAddress = ();
    public string? localAddress = ();
    public int id = 0;

    public function __init(ClientConfig? clientConfig) {
        if (clientConfig is ClientConfig) {
            self.config = clientConfig;
            var initResult = self.initEndpoint(clientConfig);
            if (initResult is error) {
                panic initResult;
            }
            var startResult = self.start();
            if (startResult is error) {
                panic startResult;
            }
        }
        return ();
    }

    extern function initEndpoint(ClientConfig clientConfig) returns error?;

    extern function start() returns error?;

    # Writes given data to the client socket.
    #
    # + content - - the content that wish to send to the client socket
    # + return - - number of bytes got written or an error if encounters an error while writing
    public remote extern function write(byte[] content) returns int|error;

    # Closes the client socket connection.
    #
    # + return - - an error if encounters an error while closing the connection or returns nil otherwise
    public remote extern function close() returns error?;

    # Shutdowns the further read from socket.
    #
    # + return - an error if encounters an error while shutdown the read from socket or returns nil otherwise
    public remote extern function shutdownRead() returns error?;

    # Shutdowns the further write from socket.
    #
    # + return - an error if encounters an error while shutdown the write from socket or returns nil otherwise
    public remote extern function shutdownWrite() returns error?;
};

# Configuration for socket client endpoint.
#
# + host - Target service URL
# + port - Port number of the remote service
# + callbackService - The callback service for the client. Resources in this service gets called on receipt of messages from the server.
public type ClientConfig record {
    string host;
    int port;
    service callbackService?;
    !...;
};
