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

# JMS topic publisher
#
# + producerActions - Topic publisher endpoint actions
# + config - Topic publisher endpoint configuration
public type TopicPublisher object {
    public TopicPublisherActions producerActions;
    public TopicPublisherEndpointConfiguration config;

    # Topic publisher contructor
    new() {
        self.producerActions = new;
    }

    # Initialize topic publisher endpoint
    #
    # + c - Topic publisher endpoint configuration
    public function init(TopicPublisherEndpointConfiguration c) {
        self.config = c;
        self.producerActions.topicPublisher = self;
        match (c.session) {
            Session s => {
                match (c.topicPattern) {
                    string topicPattern => {
                        self.initTopicPublisher(s);
                    }
                    () => {log:printInfo("Topic publisher is not properly initialized for the topic");}
                }
            }
            () => {}
        }
    }

    public extern function initTopicPublisher(Session session, Destination? destination = ());

    # Register topic publisher endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Start topic publisher endpoint
    public function start() {

    }

    # Get topic publisher actions
    # + return - action object of TopicPublisherActions
    public function getCallerActions() returns TopicPublisherActions {
        return self.producerActions;
    }

    # Stop topic publisher endpoint
    public function stop() {

    }
};

# Configuration related to the topic publisher endpoint
#
# + session - Session object used to create topic publisher
# + topicPattern - Topic name pattern
public type TopicPublisherEndpointConfiguration record {
    Session? session;
    string? topicPattern;
    !...
};

# Actions that topic publisher endpoint could perform
# + topicPublisher - JMS topic publisher
public type TopicPublisherActions object {

    public TopicPublisher? topicPublisher;

    # Sends a message to the JMS provider
    #
    # + message - Message to be sent to the JMS provider
    # + return - error upon failure to send the message to the JMS provider
    public extern function send(Message message) returns error?;

    # Sends a message to the JMS provider
    #
    # + destination - destination used for the message sender
    # + message - message to be sent to the JMS provider
    # + return - error upon failure to send the message to the JMS provider
    public function sendTo(Destination destination, Message message) returns error?;
};

function TopicPublisherActions::sendTo(Destination destination, Message message) returns error? {
    match (self.topicPublisher) {
        TopicPublisher topicPublisher => {
            match (topicPublisher.config.session) {
                Session s => {
                    validateTopic(destination);
                    topicPublisher.initTopicPublisher(s, destination = destination);
                }
                () => {}
            }
        }
        () => {log:printInfo("Topic publisher is not properly initialized.");}
    }
    return self.send(message);
}
