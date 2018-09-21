// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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



#
# + byteChannel - ByteChannel which will represent the socket connection
# + remotePort - Remote server connection port
# + localPort - Local port the socket connection should bound
# + remoteAddress - IP/Host of the remote server
# + localAddress - Local interface the connection should bound
public type Socket object {

    @readonly public ByteChannel byteChannel;
    @readonly public int remotePort;
    @readonly public int localPort;
    @readonly public string remoteAddress;
    @readonly public string localAddress;

    public new() {
        init();
    }

    # Initializes a socket.
    extern function init();

    # Binds socket to a local port.
    #
    # + port - The port number.
    # + interface - The interface that wish to bind.
    # + return - An error if could not bind to the port
    public extern function bindAddress(@sensitive int port, @sensitive string? interface = ()) returns error?;

    # Open a connection with remote server.
    #
    # + host - The interface that wish to bind.
    # + port - The port number.
    # + return - An error if could not connect with the remote server.
    public extern function connect(@sensitive string host, @sensitive int port) returns error?;

    # Closes a socket connection.
    #
    # + return - An error if the connection could not be closed properly
    public extern function close() returns error?;

    # Shutdown the connection from reading. In this case content cannot be read from the server.
    #
    # + return - An error if the connection could not be shutdown properly
    public extern function shutdownInput() returns error?;

    # Shutdown the connection from writing. In this case content cannot be written to the server.
    #
    # + return - An error if the connection could not be shutdown properly
    public extern function shutdownOutput() returns error?;
};

# Represents a TCP server socket.
public type ServerSocket object {

    public new() {
        init();
    }

    # Initializes a server socket.
    extern function init();

    # Binds socket to a local port.
    #
    # + port - The port number that server start.
    # + interface - The interface that wish to bind.
    # + return - An error if could not bind to the port
    public extern function bindAddress(@sensitive int port, @sensitive string? interface = ()) returns error?;

    # This blocking function will wait until new client socket connect.
    #
    # + return - An error if could not create new socket.
    public extern function accept() returns Socket|error;

    # Closes a socket connection.
    #
    # + return - An error if the connection could not be closed properly
    public extern function close() returns error?;
};

# SocketProperties represents the properties which are used to configure TCP connection.
#
# + localPort - Local port the socket client should bind
# + keyStoreFile - Relative/absolute path to locate keystore file
# + keyStorePassword - Keystore password
# + trustStoreFile - Relative/absolute path to locate truststore file
# + trustStorePassword - Truststore password
# + certPassword - Password of the certificate
# + sslEnabledProtocols - Protocols supported for SSL (i.e TLSv1.2,TLSv1.1,TLSv1)
# + ciphers - Encrypt/decrypt algorithms (i.e RSA, SHA-256)
# + sslProtocol - Supported SSL protocols (i.e SSL, TLS)
public type SocketProperties record {
    int localPort;
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    !...
};
