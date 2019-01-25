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

# JMS TopicSubscriber endpoint
#
# + consumerActions - Handles all the caller actions related to the TopicSubscriber endpoint
# + session - Session of the topic subscriber
# + messageSelector - The message selector for the topic subscriber
public type TopicSubscriber object {

    *AbstractListener;

    public TopicSubscriberCaller consumerActions = new;
    public Session? session;
    public string messageSelector = "";

    # Initialize the TopicSubscriber endpoint
    #
    # + c - The JMS Session object or Configurations related to the receiver
    # + topicPattern - Name or the pattern of the topic subscription
    # + messageSelector - JMS selector statement
    # + identifier - Unique identifier for the subscription
    public function __init(Session|ReceiverEndpointConfiguration c, string? topicPattern = (), string messageSelector =
        "") {
        self.consumerActions.topicSubscriber = self;
        self.messageSelector = messageSelector;
        if (c is Session) {
            self.session = c;
        } else {
            Connection conn = new({
                    initialContextFactory: c.initialContextFactory,
                    providerUrl: c.providerUrl,
                    connectionFactoryName: c.connectionFactoryName,
                    properties: c.properties
                });
            self.session = new Session(conn, {
                    acknowledgementMode: c.acknowledgementMode
                });
        }
        if (topicPattern is string) {
            self.createSubscriber(self.session, messageSelector, topicPattern);
        }
    }

    # Register TopicSubscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    # + data - Service annotations
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, map<any> data) returns error? {
        return self.registerListener(serviceType, self.consumerActions, data);
    }

    extern function registerListener(service serviceType, TopicSubscriberCaller actions, map<any> data) returns error?;

    extern function createSubscriber(Session? session, string messageSelector, string|Destination dest);

    # Start TopicSubscriber endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
        return ();
    }

    # Get TopicSubscriber actions handler
    #
    # + return - TopicSubscriber actions handler
    public function getCallerActions() returns TopicSubscriberCaller {
        return self.consumerActions;
    }

    # Stop TopicSubscriber endpoint
    #
    # + return - Nil or error upon failure to close subscriber
    public function __stop() returns error? {
        return self.closeSubscriber(self.consumerActions);
    }

    extern function closeSubscriber(TopicSubscriberCaller actions) returns error?;
};

# Remote functions that topic subscriber endpoint could perform
#
# + topicSubscriber - JMS TopicSubscriber
public type TopicSubscriberCaller client object {

    public TopicSubscriber? topicSubscriber = ();

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error on failure to acknowledge a received message
    public remote extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds, returns an error on JMS provider internal error.
    public remote extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;

    # Synchronously receive a message from the JMS provider
    #
    # + destination - Destination to subscribe to
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds, returns an error on JMS provider internal error
    public remote function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?
    {
        var subscriber = self.topicSubscriber;
        if (subscriber is TopicSubscriber) {
            var session = subscriber.session;
            if (session is Session) {
                validateTopic(destination);
                subscriber.createSubscriber(session, subscriber.messageSelector, destination);
                log:printInfo("Subscriber created for topic " + destination.destinationName);
            } else {
                log:printInfo("Session is (), Topic subscriber is not properly initialized");
            }
        } else {
            log:printInfo("Topic subscriber is not properly initialized");
        }
        var result = self->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        var returnVal = self.topicSubscriber.closeSubscriber(self);
        return result;
    }
};

function validateTopic(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        map<any> errorDetail = {
            message: errorMessage
        };
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic topicSubscriberConfigError;
    } else if (destination.destinationType != "topic") {
        string errorMessage = "Destination should should be a topic";
        map<any> errorDetail = {
            message: errorMessage
        };
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic topicSubscriberConfigError;
    }
}
