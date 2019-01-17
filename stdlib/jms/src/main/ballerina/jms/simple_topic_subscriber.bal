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

# JMS Simplified TopicSubscriber endpoint
#
# + config - Used to store configurations related to JMS SimpleTopicSubscriber
public type SimpleTopicSubscriber object {

    *AbstractListener;

    public SimpleTopicSubscriberEndpointConfiguration config = {};
    private Connection? connection;
    private Session? session = ();
    private TopicSubscriber subscriber;

    # Initialize SimpleTopicSubscirber endpoint
    #
    # + c - Configurations related to the SimpleTopicSubscriber endpoint
    public function __init(SimpleTopicSubscriberEndpointConfiguration c) {
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

        TopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: c.topicPattern,
            messageSelector: c.messageSelector
        };
        self.subscriber = new(consumerConfig);
    }

    # Register SimpleTopicSubscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    # + data - Service annotations
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, map<any> data) returns error? {
          return self.subscriber.registerListener(serviceType, self.subscriber.consumerActions, data);
    }

    # Start SimpleTopicSubscriber endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
         return ();
    }

    # Get SimpleTopicSubscriber actions handler
    #
    # + return - TopicSubscriber actions handler
    public function getCallerActions() returns TopicSubscriberCaller {
        return self.subscriber.getCallerActions();
    }

    # Stop SimpleTopicSubsriber endpoint
    #
    # + return - Nil or error upon failure to close subscriber
    public function __stop() returns error? {
        return self.subscriber.closeSubscriber(self.subscriber.consumerActions);
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
};

# Configuration related to simple topic subscriber endpoint
#
# + initialContextFactory - JMS provider specific inital context factory
# + providerUrl - JMS provider specific provider URL used to configure a connection
# + connectionFactoryName - JMS connection factory to be used in creating JMS connections
# + acknowledgementMode - Specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + messageSelector - Message selector condition to filter messages
# + properties - Additional properties used when initializing the initial context
# + topicPattern - Name of the target topic
public type SimpleTopicSubscriberEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector = "";
    map<any> properties = {};
    string topicPattern = "";
    !...;
};
