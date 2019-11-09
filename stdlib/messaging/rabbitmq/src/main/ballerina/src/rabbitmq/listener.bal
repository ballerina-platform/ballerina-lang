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

# Public Ballerina API - Ballerina RabbitMQ Message Listener.
# To provide a listener to consume messages from RabbitMQ.
#
# + amqpChannel - Reference to a Ballerina RabbitMQ Channel.
public type Listener object {

    *lang:Listener;

    private Channel? amqpChannel;

    # Initializes a Ballerina ChannelListener object with the given Connection object or connection parameters.
    # Creates a Connection object if only the connection configuration is given. Sets global QoS settings,
    # which will be applied to the entire channel.
    #
    # + connectionOrConnectionConfig - Holds a Ballerina RabbitMQ Connection object or the connection parameters.
    # + prefetchCount - Maximum number of messages that the server will deliver, 0 if unlimited.
    #                      Unless explicitly given, this value is 10 by default.
    # + prefetchSize - Maximum amount of content (measured in octets) that the server will deliver, 0 if unlimited.
    public function __init(ConnectionConfiguration|Connection connectionOrConnectionConfig, int? prefetchCount = (),
                                    int? prefetchSize = ()) {
        self.amqpChannel = new Channel(connectionOrConnectionConfig);
        var result = self.setQosSettings(prefetchCount, prefetchSize);
        if (result is error) {
            panic result;
        }
    }

    # Starts the endpoint. Function is ignored by the ChannelListener.
    #
    # + return - Nil or error upon failure to start.
    public function __start() returns error? {
        return self.start();
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    # Stops consuming messages through ChannelListener endpoint.
    #
    # + return - Nil or error upon failure to close ChannelListener.
    public function __immediateStop() returns error? {
        return self.stop();
    }

    # Binds the ChannelListener to a service.
    #
    # + s - Type descriptor of the service to bind to.
    # + name - Name of the service.
    # + return - () or error upon failure to register listener.
    public function __attach(service s, string? name = ()) returns error? {
       return self.registerListener(s);
    }

    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    # Retrieve the Channel which initializes this listener.
    #
    # + return - RabbitMQ Channel object or error if an I/O problem is encountered.
    public function getChannel() returns Channel | error = external;

    private function registerListener(service serviceType) returns error? = external;
    private function detach(service serviceType) returns error? = external;
    private function stop() returns error? = external;
    private function start() returns error? = external;
    private function setQosSettings(int? prefetchCount, int? prefetchSize) returns error? = external;
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
