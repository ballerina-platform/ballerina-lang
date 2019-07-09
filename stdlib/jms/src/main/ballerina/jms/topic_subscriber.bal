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

# The JMS TopicListener.
#
# + consumerActions - Handles all the caller actions related to the TopicSubscriber.
# + session - The session of the topic subscriber.
# + messageSelector - The message selector for the topic subscriber.
public type TopicListener object {

    *AbstractListener;

    public TopicSubscriberCaller consumerActions = new;
    public Session session;
    public string messageSelector = "";

    # Initializes the TopicListener.
    #
    # + c - The JMS Session object or the configurations related to the receiver.
    # + topicPattern - The name or the pattern of the topic subscription.
    # + messageSelector - The JMS selector statement.
    # + identifier - The unique identifier for the subscription.
    public function __init(Session|ReceiverEndpointConfiguration c, string? topicPattern = (), string messageSelector =
        "") {
        self.consumerActions.topicListener = self;
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

    # Registers the TopicListener.
    #
    # + s - The service instance.
    # + name - The name of the service.
    # + return - Returns nil or an error upon failure to register the listener.
    public function __attach(service s, string? name = ()) returns error? {
        return self.registerListener(s, self.consumerActions, name);
    }

    function registerListener(service serviceType, TopicSubscriberCaller actions, string? name) returns error? = external;

    function createSubscriber(Session? session, string messageSelector, string|Destination dest) = external;

    # Starts the TopicListener.
    #
    # + return - Returns nil or an error upon failure to start.
    public function __start() returns error? {
        return self.start();
    }

    # Obtains the TopicSubscriber actions handler.
    #
    # + return - The TopicSubscriber actions handler.
    public function getCallerActions() returns TopicSubscriberCaller {
        return self.consumerActions;
    }

    # Stops the TopicListener,
    #
    # + return - Returns nil or an error upon failure to close the subscriber.
    public function __stop() returns error? {
        return self.closeSubscriber(self.consumerActions);
    }

    function closeSubscriber(TopicSubscriberCaller actions) returns error? = external;
    private function start() returns error? = external;
};

# Remote functions that the topic subscriber endpoint could perform.
#
# + topicListener - The JMS TopicListener.
public type TopicSubscriberCaller client object {

    public TopicListener? topicListener = ();

    # Acknowledges a received message.
    #
    # + message - The JMS message to be acknowledged.
    # + return - Returna an error upon failure to acknowledge a received message.
    public remote function acknowledge(Message message) returns error? = external;

    # Synchronously receives a message from the JMS provider.
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received.
    # + return - Returns a message or nil if the timeout exceeds, or returns an error upon an internal error of the JMS provider.
    public remote function receive(int timeoutInMilliSeconds = 0) returns Message|error? = external;

    # Synchronously receives a message from the JMS provider.
    #
    # + destination - Destination to subscribe to.
    # + timeoutInMilliSeconds - Time to wait until a message is received.
    # + return - Returns a message or nil if the timeout exceeds, or returns an error upon an internal error of the JMS provider.
    public remote function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?
    {
        var subscriber = self.topicListener;
        if (subscriber is TopicListener) {
            var session = subscriber.session;
            validateTopic(destination);
            subscriber.createSubscriber(session, subscriber.messageSelector, destination);
            log:printInfo("Subscriber created for topic " + destination.destinationName);
        } else {
            log:printInfo("Topic subscriber is not properly initialized");
        }
        var result = self->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        var returnVal = self.topicListener.closeSubscriber(self);
        return result;
    }
};

function validateTopic(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, message = errorMessage);
        panic topicSubscriberConfigError;
    } else if (destination.destinationType != "topic") {
        string errorMessage = "Destination should should be a topic";
        error topicSubscriberConfigError = error(JMS_ERROR_CODE, message = errorMessage);
        panic topicSubscriberConfigError;
    }
}
