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

# Public Ballerina API - Ballerina RabbitMQ Message Listener.
# To provide a listener to consume messages from RabbitMQ.
#
# + chann - Reference to a Ballerina RabbitMQ Channel.
public type ChannelListener object {

    *AbstractListener;

    private Channel? chann;

    # Initializes a Ballerina ChannelListener object with the given Connection object or connection parameters.
    # Creates a Connection object if only the connection configuration is given.
    #
    # + connectionOrConnectionConfig - Holds a Ballerina RabbitMQ Connection object or the connection parameters.
    public function __init(ConnectionConfiguration|Connection connectionOrConnectionConfig) {
        self.chann = new Channel(connectionOrConnectionConfig);
    }

    # Starts the endpoint. Function is ignored by the ChannelListener.
    #
    # + return - Nil or error upon failure to start.
    public function __start() returns error? {
        //ignore
    }

    # Stops consuming messages through ChannelListener endpoint.
    #
    # + return - Nil or error upon failure to close ChannelListener.
    public function __stop() returns error? {
        return self.stop();
    }

    # Binds the ChannelListener to a service.
    #
    # + serviceType - Type descriptor of the service to bind to.
    # + name - Name of the service.
    # + return - () or error upon failure to register listener.
    public function __attach(service serviceType, string? name = ()) returns error? {
       self.registerListener(serviceType);
    }

    # Binds the ChannelListener to a service.
    #
    # + serviceType - Type descriptor of the service to bind to.
    private function registerListener(service serviceType) = external;

    # Stops consuming messages through listener endpoint.
    #
    # + return - () or error upon failure to close the channel.
    private function stop() returns error? = external;
};

# Represents the list of parameters required to create a subscription.
#
# + queueConfig - Specifies configuration details about the queue to be subscribed to.
public type RabbitMQServiceConfig record {|
    QueueConfiguration queueConfig;
|};

# Service descriptor data generated at compile time.
public annotation<service> ServiceConfig RabbitMQServiceConfig;
