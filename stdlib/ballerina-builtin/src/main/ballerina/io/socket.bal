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


documentation {
    Represents a TCP socket.

    F{{channel}} - ByteChannel which will represent the socket connection
    F{{port}} - Remote server connection port
    F{{localPort}} - Local port the socket connection should bound
    F{{address}} - IP/Host of the remote server
    F{{localAddress}} - Local interface the connection should bound
}
public type Socket object {

    public {
        @readonly ByteChannel channel;
        @readonly int port;
        @readonly int localPort;
        @readonly string address;
        @readonly string localAddress;
    }

    documentation{
        Closes a socket connection

        R{{}} - An error if the connection could not be closed properly
    }
    public native function close() returns error?;

    documentation{
        Shutdown the connection from reading. In this case content cannot be read from the server

        R{{}} - An error if the connection could not be shutdown properly
    }
    public native function shutdownInput() returns error?;

    documentation{
        Shutdown the connection from writing. In this case content cannot be written to the server

        R{{}} - An error if the connection could not be shutdown properly
    }
    public native function shutdownOutput() returns error?;
};

documentation {
    SocketProperties represents the properties which are used to configure TCP connection.

    F{{localPort}} - Local port the socket client should bind
    F{{keyStoreFile}} - Relative/absolute path to locate keystore file
    F{{keyStorePassword}} - Keystore password
    F{{trustStoreFile}} - Relative/absolute path to locate truststore file
    F{{trustStorePassword}} - Truststore password
    F{{certPassword}} - Password of the certificate
    F{{sslEnabledProtocols}} - Protocols supported for SSL (i.e TLSv1.2,TLSv1.1,TLSv1)
    F{{ciphers}} - Encrypt/decrypt algorithms (i.e RSA, SHA-256)
    F{{sslProtocol}} - Supported SSL protocols (i.e SSL, TLS)
}
public type SocketProperties {
    int localPort;
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
};
