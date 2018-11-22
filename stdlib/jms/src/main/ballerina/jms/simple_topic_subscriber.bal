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

# JMS simple topic subscriber
#
# + config - Simple topic subscrirber enpoint configuration
public type SimpleTopicSubscriber object {

    public SimpleTopicSubscriberEndpointConfiguration config = {};
    private Connection? connection;
    private Session? session = ();
    private TopicSubscriber? subscriber = ();

    # Initialize simple topic subscirber endpoint
    #
    # + c - Simple topic subscrirber enpoint configuration
    public function init(SimpleTopicSubscriberEndpointConfiguration c) {
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

        TopicSubscriber topicSubscriber = new;
        TopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: c.topicPattern,
            messageSelector: c.messageSelector
        };
        topicSubscriber.init(consumerConfig);
        self.subscriber = topicSubscriber;
    }

    # Register simple topic subscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {
        var subscriber = self.subscriber;
        if (subscriber is TopicSubscriber) {
            subscriber.register(serviceType);
        } else {
            string errorMessage = "Topic Subscriber cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Start simple topic subscriber endpoint
    public function start() {

    }

    # Get simple topic subscriber actions
    #
    # + return - Topic subscriber actions
    public function getCallerActions() returns TopicSubscriberActions {
        var subscriber = self.subscriber;
        if (subscriber is TopicSubscriber) {
            return subscriber.getCallerActions();
        } else {
            string errorMessage = "Topic Subscriber cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Stop simple topic subsriber endpoint
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
    # + return - a message or nil if the session is nil.
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

# Configuration related to simple topic subscriber endpoint
#
# + initialContextFactory - JNDI initial context factory class
# + providerUrl - JNDI provider URL
# + connectionFactoryName - JNDI name of the connection factory
# + acknowledgementMode - JMS session acknwoledge mode
# + messageSelector - Message selector condition to filter messages
# + properties - JMS message properties
# + topicPattern - Topic name pattern
public type SimpleTopicSubscriberEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector = "";
    map properties = {};
    string topicPattern = "";
    !...
};
