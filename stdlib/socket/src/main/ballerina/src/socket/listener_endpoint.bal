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
import ballerina/lang.'object as lang;

# Represents the socket listener on which the socket listener service is registered and started.
public type Listener object {

    *lang:Listener;

    # Creates a new socket Listener.
    #
    # + port - The port number of the remote service
    # + config - Configurations related to the `socket:Listener`
    public function __init(int port, ListenerConfig? config = ()) {
        var result = initServer(self, port, config ?: {});
        if (result is error) {
            panic result;
        }
    }

# Starts the `socket:Listener`.
# ```ballerina
# socket:error? result = socketListener.__start();
# ```
#
# + return - `()` or else a `socket:Error` upon failure to start the listener
    public function __start() returns error? {
        return startService(self);
    }

# Stops the `socket:Listener` gracefully.
# ```ballerina
# socket:error? result = socketListener.__gracefulStop();
# ```
#
# + return - `()` or else a `socket:Error` upon failure to stop the listener
    public function __gracefulStop() returns error? {
        return externStop(self, true);
    }

# Stops the `socket:Listener` forcefully.
# ```ballerina
# socket:error? result = socketListener.__immediateStop();
# ```
#
# + return - `()` or else a `socket:Error` upon failure to stop the listener
    public function __immediateStop() returns error? {
        return externStop(self, false);
    }

# Binds a service to the `socket:Listener`.
# ```ballerina
# socket:error? result = socketListener.__attach(helloService);
# ```
#
# + s - Type descriptor of the service
# + name - Name of the service
# + return - `()` or else a `socket:Error` upon failure to register the listener
    public function __attach(service s, string? name = ()) returns error? {
        return externRegister(self, s);
    }

# Stops consuming messages and detaches the service from the `socket:Listener`.
# ```ballerina
# socket:error? result = socketListener.__detach(helloService);
# ```
#
# + s - Type descriptor of the service
# + return - `()` or else a `socket:Error` upon failure to detach the service
    public function __detach(service s) returns error? {
    // Socket listener operations are strictly bound to the attached service. In fact, a listener doesn't support
    // multiple services. Therefore, an already attached service is not removed during the detachment.
    }
};

# Represents the socket server configuration.
#
# + interface - The interface for the server to be bound
# + readTimeoutInMillis - The socket reading timeout value to be used in milliseconds. If this is not set,
#                         the default value of 300000 milliseconds (5 minutes) will be used.
public type ListenerConfig record {|
    string? interface = ();
    int readTimeoutInMillis = 300000;
|};

function initServer(Listener lis, int port, ListenerConfig config) returns error? =
@java:Method {
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ServerActions"
} external;

function externRegister(Listener lis, service s) returns error? =
@java:Method {
    name: "register",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ServerActions"
} external;

function startService(Listener lis) returns error? =
@java:Method {
    name: "start",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ServerActions"
} external;

function externStop(Listener lis, boolean graceful) returns error? =
@java:Method {
    name: "stop",
    class: "org.ballerinalang.stdlib.socket.endpoint.tcp.ServerActions"
} external;
