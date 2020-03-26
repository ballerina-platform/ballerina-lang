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

# Represents service endpoint where socket server service registered and start.
#
public type Listener object {

    *lang:Listener;

    # Initializes the TCP socket listener with a port and an optional listener configuration.
    #
    # + port - The port number to which this listener is attached and will listen
    # + config - This optional listener configuration is used to provide properties like bind interface and read timeout
    public function __init(int port, ListenerConfig? config = ()) {
        var result = initServer(self, port, config ?: {});
        if (result is error) {
            panic result;
        }
    }

    # Starts the registered service.
    #
    # + return - Returns an error if an error occurs while starting the server or returns nil otherwise.
    public function __start() returns error? {
        return startService(self);
    }

    # Stops the registered service. Behaviours of this and the `__immediateStop()` function are similar.
    #
    # + return - Returns an error if an error occurs while stopping the server or returns nil otherwise.
    public function __gracefulStop() returns error? {
        return externStop(self, true);
    }

    # Stops the registered service. Behaviours of this and the `__gracefulStop()` function are similar.
    #
    # + return - Returns an error if an error occurs while stopping the server or returns nil otherwise
    public function __immediateStop() returns error? {
        return externStop(self, false);
    }

    # Gets called every time a service attaches itself to this listener. Also, happens at the initialization of  the module.
    #
    # + s - The type of the service to be registered
    # + name - Name of the service
    # + return - Returns an error if an error occurs while attaching the service or returns nil otherwise
    public function __attach(service s, string? name = ()) returns error? {
        return externRegister(self, s);
    }

    # Gets called every time a service detaches itself from this listener
    #
    # + s - The type of the service to be detached
    # + return - Returns an error if an error occurs while detaching the service or returns nil otherwise
    public function __detach(service s) returns error? {
    // Socket listener operations are strictly bound to the attached service. In fact, a listener doesn't support
    // multiple services. Therefore, an already attached service is not removed during the detachment.
    }
};

# Represents the socket server configuration.
#
# + interface - the interface that server with to bind
# + readTimeoutInMillis - Socket read timeout value to be used in milliseconds. Default is 300000 milliseconds (5 minutes)
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
