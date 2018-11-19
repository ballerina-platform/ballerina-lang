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

    public TopicSubscriberActions consumerActions = new;
    public TopicSubscriberEndpointConfiguration config = {};

    # Initialize topic subscriber endpoint
    #
    # + c - Topic subscriber configuration
    public function init(TopicSubscriberEndpointConfiguration c) {
        self.config = c;
        self.consumerActions.topicSubscriber = self;
        var session = c.session;
        if (session is Session) {
             var topicPattern = c.topicPattern;
             if (topicPattern is string) {
                 self.createSubscriber(session, c.messageSelector);
                 log:printInfo("Subscriber created for topic " + topicPattern);
             } else {
                 log:printInfo("Topic subscriber is not properly initialized for topic");
             }
        } else {
            log:printInfo("Topic subscriber is not properly initialized for topic");
        }
    }

    # Register topic subscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, self.consumerActions);
    }

    extern function registerListener(typedesc serviceType, TopicSubscriberActions actions);

    extern function createSubscriber(Session session, string messageSelector, Destination? destination = ());

    # Start topic subscriber endpoint
    public function start() {

    }

    # Get topic subscriber actions
    #
    # + return - Topic subscriber actions
    public function getCallerActions() returns TopicSubscriberActions {
        return self.consumerActions;
    }

    # Stop topic subscriber endpoint
    public function stop() {
        self.closeSubscriber(self.consumerActions);
    }

    extern function closeSubscriber(TopicSubscriberActions actions);
};

# Configuration related to topic subscriber endpoint
#
# + session - Session object used to create topic subscriber
# + topicPattern - Topic name pattern
# + messageSelector - Message selector condition to filter messages
# + identifier - Identifier of topic subscriber endpoint
public type TopicSubscriberEndpointConfiguration record {
    Session? session = ();
    string? topicPattern = ();
    string messageSelector = "";
    string identifier = "";
    !...
};

# Actions that topic subscriber endpoint could perform
#
# + topicSubscriber - JMS topic subscriber
public type TopicSubscriberActions object {

    public TopicSubscriber? topicSubscriber = ();

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error on failure to acknowledge a received message
    public extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;

    # Synchronously receive a message from the JMS provider
    #
    # + destination - destination to subscribe to
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?;
};

function TopicSubscriberActions.receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|
        error)? {
    var subscriber = self.topicSubscriber;
    if (subscriber is TopicSubscriber) {
          var session = subscriber.config.session;
          if (session is Session) {
            validateTopic(destination);
            subscriber.createSubscriber(session, subscriber.config.messageSelector, destination = destination);
            log:printInfo("Subscriber created for topic " + destination.destinationName);
          } else {
            log:printInfo("Session is (), Topic subscriber is not properly initialized");
          }
    } else {
        log:printInfo("Topic subscriber is not properly initialized");
    }
    var result = self.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
    self.topicSubscriber.closeSubscriber(self);
    return result;
}

function validateTopic(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        map errorDetail = { message: errorMessage };
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic topicSubscriberConfigError;
    } else if (destination.destinationType != "topic") {
        string errorMessage = "Destination should should be a topic";
        map errorDetail = { message: errorMessage };
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic topicSubscriberConfigError;
    }
}
