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

# JMS simple topic publisher
#
# + config - Simple topic publisher enpoint configuration
public type SimpleTopicPublisher object {

    public SimpleTopicPublisherEndpointConfiguration config = {};

    private Connection? connection;
    private Session? session = ();
    private TopicPublisher? publisher = ();

    # Initialize simple topic publisher endpoint
    #
    # + c - Simple topic publisher enpoint configuration
    public function init(SimpleTopicPublisherEndpointConfiguration c) {
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

        TopicPublisher topicPublisher = new;
        TopicPublisherEndpointConfiguration publisherConfig = {
            session: newSession,
            topicPattern: c.topicPattern
        };
        topicPublisher.init(publisherConfig);
        self.publisher = topicPublisher;
    }

    # Register simple topic publisher endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Start simple topic pubilsher endpoint
    public function start() {

    }

    # Get simple topic pubilsher actions
    #
    # + return - Topic publisher actions
    public function getCallerActions() returns TopicPublisherActions {
        var publisher = self.publisher;
        if (publisher is TopicPublisher) {
            return publisher.getCallerActions();
        } else {
            string errorMessage = "Topic publisher cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Stop simple topic pubilsher endpoint
    public function stop() {

    }

    # Create JMS text message
    #
    # + message - A message body to create a text message
    # + return - a message or nil if the session is nil
    public function createTextMessage(string message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createTextMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
    # Create JMS map message
    #
    # + message - A message body to create a map message
    # + return - a message or nil if the session is nil
    public function createMapMessage(map message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createMapMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
};

# Configuration related to simple topic publisher endpoint
#
# + initialContextFactory - JNDI initial context factory class
# + providerUrl - JNDI provider URL
# + connectionFactoryName - JNDI name of the connection factory
# + acknowledgementMode - JMS session acknwoledge mode
# + properties - JMS message properties
# + topicPattern - name of the target topic
public type SimpleTopicPublisherEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties = {};
    string topicPattern = "";
    !...
};
