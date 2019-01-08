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

import ballerina/log;

# JMS Simplified TopicPublisher endpoint
#
# + config - Used to store configurations related to a JMS SimpleTopicPublisher
public type SimpleTopicPublisher client object {

    public SimpleTopicPublisherEndpointConfiguration config = {};
    private Connection? connection;
    private Session? session = ();
    private TopicPublisher publisher;

    # Initialize the SimpleTopicPublisher endpoint
    #
    # + c - Configurations related to the SimpleTopicPublisher endpoint
    public function __init(SimpleTopicPublisherEndpointConfiguration c) {
        self.config = c;
        Connection conn = new({
                initialContextFactory: self.config.initialContextFactory,
                providerUrl: self.config.providerUrl,
                connectionFactoryName: self.config.connectionFactoryName,
                properties: self.config.properties
            });
        self.connection = conn;

        Session newSession = new(conn, {
                acknowledgementMode: self.config.acknowledgementMode
            });
        self.session = newSession;

        self.publisher = new ({
                session: newSession,
                topicPattern: c.topicPattern
        });
    }

    # Create JMS text message
    #
    # + message - Message body to create a text message
    # + return - Message or nil if the session is nil
    public function createTextMessage(string message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createTextMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map<any> errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
    # Create JMS map message
    #
    # + message - Message body to create a map message
    # + return - Message or nil if the session is nil
    public function createMapMessage(map<any> message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createMapMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map<any> errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Sends a message to the JMS provider
    #
    # + message - Message to be sent to the JMS provider
    # + return - Error upon failure to send the message to the JMS provider
    public remote function send(Message message) returns error? {
        return self.publisher->send(message);
    }

    # Sends a message to the JMS provider
    #
    # + destination - Destination used for the message sender
    # + message - Message to be sent to the JMS provider
    # + return - Error upon failure to send the message to the JMS provider
    public remote function sendTo(Destination destination, Message message) returns error? {
        return self.publisher->sendTo(destination, message);
    }
};

# Configurations related to the SimpleQueueSender endpoint
#
# + initialContextFactory - JMS provider specific inital context factory
# + providerUrl - JMS provider specific provider URL used to configure a connection
# + connectionFactoryName - JMS connection factory to be used in creating JMS connections
# + acknowledgementMode - Specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + properties - Additional properties used when initializing the initial context
# + topicPattern - Name of the target queue
public type SimpleTopicPublisherEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map<any> properties = {};
    string topicPattern = "";
    !...;
};
