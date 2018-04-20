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

package ballerina.io;

documentation {
    Represetns a TCP socket.
}
public type Socket object {
    public {
        ByteChannel channel;
        int port;
        int localPort;
        string address;
        string localAddress;
    }

    @Description {value:"Close the socket connection with the remote server"}
    @Return {value:"Returns an error if socket could not be closed"}
    public native function close() returns error?;

    @Description {value:"Shutdown the connection for reading"}
    @Return {value:"Returns an error if socket could not be shutdown for reading"}
    public native function shutdownInput() returns error?;

    @Description {value:"Shutdown the connection for writing"}
    @Return {value:"Returns an error if socket could not be shutdown for writing"}
    public native function shutdownOutput() returns error?;
};

documentation {
    SocketProperties structs represents the properties which are used to configure TCP connection.
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
