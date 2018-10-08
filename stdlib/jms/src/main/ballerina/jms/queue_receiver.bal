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

# Queue Receiver endpoint
#
# + consumerActions - handles all the caller actions related to the queue receiver endpoint
# + config - configurations related to the QueueReceiver
public type QueueReceiver object {

    public QueueReceiverActions consumerActions;
    public QueueReceiverEndpointConfiguration config;

    # Initializes the QueueReceiver endpoint
    #
    # + c - Configurations related to the QueueReceiver endpoint
    public function init(QueueReceiverEndpointConfiguration c) {
        self.config = c;
        self.consumerActions.queueReceiver = self;
        match (c.session) {
            Session s => {
                match (c.queueName) {
                    string queueName => {
                        self.createQueueReceiver(s, c.messageSelector);
                        log:printInfo("Message receiver created for queue " + queueName);
                    }
                    () => {}
                }
            }
            () => { log:printInfo("Message receiver is not properly initialised for queue"); }
        }
    }

    # Binds the queue receiver endpoint to a service
    #
    # + serviceType - type descriptor of the service to bind to
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    extern function registerListener(typedesc serviceType, QueueReceiverActions actions);

    extern function createQueueReceiver(Session session, string messageSelector, Destination? destination = ());

    # Starts the endpoint. Function is ignored by the receiver endpoint
    public function start() {
        // Ignore
    }

    # Retrieves the QueueReceiver consumer action handler
    #
    # + return - queue receiver action handler
    public function getCallerActions() returns QueueReceiverActions {
        return consumerActions;
    }

    # Stops consuming messages through QueueReceiver endpoint
    public function stop() {
        self.closeQueueReceiver(consumerActions);
    }

    extern function closeQueueReceiver(QueueReceiverActions actions);
};

# Configurations related to the QueueReceiver endpoint
#
# + session - JMS session object
# + queueName - Name of the queue
# + messageSelector - JMS selector statement
# + identifier - unique identifier for the subscription
public type QueueReceiverEndpointConfiguration record {
    Session? session;
    string? queueName;
    string messageSelector;
    string identifier;
    !...
};

# Caller actions related to queue receiver endpoint.
#
# + queueReceiver - queue receiver endpoint
public type QueueReceiverActions object {

    public QueueReceiver? queueReceiver;

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds. Returns an error on jms provider internal error
    public extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;

    # Synchronously receive a message from a given destination
    #
    # + destination - destination to subscribe to
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or () if the timeout exceededs. Returns an error on jms provider internal error.
    public function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?;
};

function QueueReceiverActions::receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|
        error)? {
    match (self.queueReceiver) {
        QueueReceiver queueReceiver => {
            match (queueReceiver.config.session) {
                Session s => {
                    validateQueue(destination);
                    queueReceiver.createQueueReceiver(s, queueReceiver.config.messageSelector, destination = destination
                    );
                }
                () => {}
            }
        }
        () => { log:printInfo("Message receiver is not properly initialised for queue " +
                destination.destinationName); }
    }
    var result = self.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
    self.queueReceiver.closeQueueReceiver(self);
    return result;
}

function validateQueue(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        error queueReceiverConfigError = { message: errorMessage };
        throw queueReceiverConfigError;
    } else if (destination.destinationType != "queue") {
        string errorMessage = "Destination should should be a queue";
        error queueReceiverConfigError = { message: errorMessage };
        throw queueReceiverConfigError;
    }
}
