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

# Represents the JMS session.
#
# + config - Stores the configurations related to a JMS session.
public type Session object {

    private SessionConfiguration config;
    private Connection conn;

    # The default constructor of the JMS session.
    public function __init(Connection connection, SessionConfiguration c) {
        self.config = c;
        self.conn = connection;
        self.initEndpoint(connection);
    }

    function initEndpoint(Connection connection) = external;

    # Unsubscribes a durable subscription that has been created by a client.
    # It is erroneous for a client to delete a durable subscription while there is an active (not closed) consumer
    # for the subscription, or while a consumed message being part of a pending transaction or has not been
    # acknowledged in the session.
    #
    # + subscriptionId - The name, which is used to identify the subscription.
    # + return - Cancels the subscription.
    public function unsubscribe(string subscriptionId) returns error? = external;

    # Creates a JMS Queue, which can be used as temporary response destination.
    #
    # + return - Returns the JMS destination for a temporary queue or an error if it fails.
    public function createTemporaryQueue() returns Destination|error = external;

    # Creates a JMS Topic, which can be used as a temporary response destination.
    #
    # + return - Returns the JMS destination for a temporary topic or an error if it fails.
    public function createTemporaryTopic() returns Destination|error = external;

    # Creates a JMS Queue, which can be used with a message producer.
    #
    # + queueName - The name of the Queue.
    # + return - Returns the JMS destination for a queue or an error if it fails.
    public function createQueue(string queueName) returns Destination|error = external;

    # Creates a JMS Topic, which can be used with a message producer.
    #
    # + topicName - The name of the Topic.
    # + return - Returns the JMS destination for a topic or an error if it fails.
    public function createTopic(string topicName) returns Destination|error = external;
};

# The Configurations that are related to a JMS session.
#
# + acknowledgementMode - Specifies the session mode that will be used. Valid values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED", and "DUPS_OK_ACKNOWLEDGE".
public type SessionConfiguration record {|
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
|};
