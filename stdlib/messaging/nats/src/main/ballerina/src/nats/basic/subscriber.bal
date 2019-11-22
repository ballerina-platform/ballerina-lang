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

import ballerina/lang.'object as lang;
import ballerinax/java;

# Represents a connection which will be used for subscription.
public type Listener object {

    *lang:Listener;
    private Connection conn;

    # Creates a new consumer. A new connection will be created if a reference to a connection is not provided.
    #
    # + connection - An already-established connection or configuration to create a new connection.
    public function __init(Connection connection) {
        self.conn = connection;
        consumerInit(self, connection);
    }

    # Binds the NATS consumer to a service.
    #
    # + s - Type descriptor of the service.
    # + name - Name of the service.
    # + return - Returns nil or the error upon failure to register the listener.
    public function __attach(service s, string? name = ()) returns error? {
        return basicRegister(self, s, name);
    }

    public function __detach(service s) returns error? {
        return basicDetach(self, s);
    }

    # Starts the listener in the lifecycle.
    #
    # + return - Error or ().
    public function __start() returns error? {
        return basicStart(self);
    }

    # Gracefully stops the listener in the lifecycle.
    #
    # + return - Error or ().
    public function __gracefulStop() returns error? {
        return basicGracefulStop(self);
    }

    # Forcefully stops the listener in the lifecycle.
    #
    # + return - Error or ().
    public function __immediateStop() returns error? {
        return basicImmediateStop(self);
    }
};

function basicRegister(Listener lis, service serviceType, string? name) returns error? =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.Register"
} external;

function basicDetach(Listener lis, service serviceType) returns error? =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.Detach"
} external;

function basicStart(Listener lis) =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.Start"
} external;

function basicGracefulStop(Listener lis) =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.GracefulStop"
} external;

function basicImmediateStop(Listener lis) =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.ImmediateStop"
} external;

function consumerInit(Listener lis, Connection c) =
@java:Method {
    class: "org.ballerinalang.nats.basic.consumer.Init"
} external;
