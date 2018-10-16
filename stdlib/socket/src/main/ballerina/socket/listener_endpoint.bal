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

# Represents service endpoint where socket server service registered and start.
#
# + remotePort - the remote port number to which this socket is connected
# + localPort - the local port number to which this socket is bound
# + remoteAddress - the remote IP address string in textual presentation to which the socket is connected
# + localAddress - the local IP address string in textual presentation to which the socket is bound
# + id - a unique identification to identify each connection between server and the client
public type Listener object {

    private CallerAction callerAction;
    @readonly public int remotePort;
    @readonly public int localPort;
    @readonly public string remoteAddress;
    @readonly public string localAddress;
    @readonly public int id;

    public extern function init(ListenerEndpointConfiguration config);

    public extern function register(typedesc serviceType);

    public extern function start();

    public extern function getCallerActions() returns CallerAction;
};

# Represents the socket server endpoint configuration.
#
# + interface - the interface that server with to bind
# + port - the port that server wish to bind
public type ListenerEndpointConfiguration record {
    string? interface;
    int port;
    !...
};
