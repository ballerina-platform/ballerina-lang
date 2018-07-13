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

# JMS topic subscriber
#
# + consumerActions - Topic subscriber endpoint actions
# + config - Topic subscriber endpoint configuration
public type TopicSubscriber object {

    public TopicSubscriberActions consumerActions;
    public TopicSubscriberEndpointConfiguration config;

    # Initialize topic subscriber endpoint
    #
    # + c - Topic subscriber configuration
    public function init(TopicSubscriberEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => {
                self.createSubscriber(s, c.messageSelector);
                log:printInfo("Subscriber created for topic " + c.topicPattern);
            }
            () => {}
        }
    }

    # Register topic subscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    native function registerListener(typedesc serviceType, TopicSubscriberActions actions);

    native function createSubscriber(Session session, string messageSelector);

    # Start topic subscriber endpoint
    public function start() {

    }

    # Get topic subscriber actions
    public function getCallerActions() returns TopicSubscriberActions {
        return consumerActions;
    }

    # Stop topic subscriber endpoint
    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    native function closeSubscriber(TopicSubscriberActions actions);
};

# Configuration related to topic subscriber endpoint
#
# + session - Session object used to create topic subscriber
# + topicPattern - Topic name pattern
# + messageSelector - Message selector condition to filter messages
# + identifier - Identifier of topic subscriber endpoint
public type TopicSubscriberEndpointConfiguration record {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;
};

# Actions that topic subscriber endpoint could perform
public type TopicSubscriberActions object {

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    public native function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public native function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
