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

# Ballerina RabbitMQ Message Listener.
# Provides a listener to consume messages from the RabbitMQ server.
public type Listener object {

    *lang:Listener;

    private Channel amqpChannel;

    # Initializes a Listener object with the given `Connection` object or connection parameters.
    # Creates a `Connection` object if only the connection configuration is given. Sets global QoS settings,
    # which will be applied to the entire `Channel`.
    #
    # + connectionOrConnectionConfig - Holds a Ballerina RabbitMQ `Connection` object or the connection parameters.
    # + prefetchCount - Maximum number of messages that the server will deliver, 0 if unlimited.
    #                      Unless explicitly given, this value is 10 by default.
    # + prefetchSize - Maximum amount of content (measured in octets) that the server will deliver, 0 if unlimited.
    public function __init(ConnectionConfiguration|Connection connectionOrConnectionConfig, int? prefetchCount = (),
                                    int? prefetchSize = ()) {
        self.amqpChannel = new Channel(connectionOrConnectionConfig);
        var result = self.setQosSettings(prefetchCount, prefetchSize);
        externInit(self, self.amqpChannel.getChannel());
        if (result is error) {
            panic result;
        }
    }

    # Attaches the service to the `Listener` endpoint.
    #
    # + s - Type descriptor of the service to bind to.
    # + name - Name of the service.
    # + return - Nil or error upon failure to register service.
    public function __attach(service s, string? name = ()) returns error? {
        return registerListener(self, s);
    }

    # Starts the consuming messages on all attached services.
    #
    # + return - Nil or error upon failure to start.
    public function __start() returns error? {
        return start(self);
    }

    # Stops consuming messages and detaches the service from the `Listener` endpoint.
    #
    # + s - Type descriptor of the service to bind to.
    # + return - Nil or error upon failure to detach service.
    public function __detach(service s) returns error? {
        return detach(self, s);
    }

    public function __gracefulStop() returns error? {
        return stop(self);
    }

    # Stops consuming messages through all consumer services and terminate the connection
    # with the server.
    #
    # + return - Nil or error upon failure to close ChannelListener.
    public function __immediateStop() returns error? {
        return abortConnection(self);
    }

    # Retrieve the `Channel` which initializes this `Listener`.
    #
    # + return - RabbitMQ Channel object or error if an I/O problem is encountered.
    public function getChannel() returns Channel {
        return self.amqpChannel;
    }

    private function setQosSettings(int? prefetchCount, int? prefetchSize) returns error? {
        return nativeSetQosSettings(prefetchCount, prefetchSize, self);
    }
};

# Represents the list of parameters required to create a subscription.
#
# + queueConfig - Specifies configuration details about the queue to be subscribed to.
# + ackMode - Type of acknowledgement mode.
# + prefetchCount - Maximum number of messages that the server will deliver, 0 if unlimited.
#                      Unless explicitly given, this value is 10 by default.
# + prefetchSize - Maximum amount of content (measured in octets) that the server will deliver, 0 if unlimited.
public type RabbitMQServiceConfig record {|
    QueueConfiguration queueConfig;
    AcknowledgementMode ackMode = AUTO_ACK;
    int prefetchCount?;
    int prefetchSize?;
|};

# Service descriptor data generated at compile time.
public annotation RabbitMQServiceConfig ServiceConfig on service;

function externInit(Listener lis, handle amqpChannel) =
@java:Method {
    name: "init",
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function registerListener(Listener lis, service serviceType) returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function start(Listener lis) returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function detach(Listener lis, service serviceType) returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function stop(Listener lis) returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function abortConnection(Listener lis) returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;

function nativeSetQosSettings(int? prefetchCount, int? prefetchSize, Listener lis) returns Error? =
@java:Method {
    name: "setQosSettings",
    class: "org.ballerinalang.messaging.rabbitmq.util.ListenerUtils"
} external;
