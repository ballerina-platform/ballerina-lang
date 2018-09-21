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

# JMS QueueSender Endpoint
#
# + producerActions - Handle all the actions related to the endpoint
# + config - Used to store configurations related to a JMS Queue sender
public type QueueSender object {

    public QueueSenderActions producerActions;
    public QueueSenderEndpointConfiguration config;

    # Default constructor of the endpoint
    public new() {
        self.producerActions = new;
    }

    # Initialize the consumer endpoint
    #
    # + c - Configurations related to the QueueSender endpoint
    public function init(QueueSenderEndpointConfiguration c) {
        self.config = c;
        self.producerActions.queueSender = self;
        match (c.session) {
            Session s => {
                match (c.queueName) {
                    string queueName => {
                        self.initQueueSender(s);
                    }
                    () => {}
                }
            }
            () => {log:printInfo("Message producer not properly initialised for queue");}
        }
    }

    extern function initQueueSender(Session session, Destination? destination = ());

    # Registers the endpoint in the service.
    # This method is not used since QueueSender is a non-service endpoint.
    #
    # + serviceType - type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Starts the consumer endpoint
    public function start() {

    }

    # Returns the caller action object of the QueueSender
    #
    # + return - Queue sender actions
    public function getCallerActions() returns QueueSenderActions {
        return self.producerActions;
    }

    # Stops the consumer endpoint
    public function stop() {

    }
};

# Configurations related to a QueueSender object
#
# + session - JMS session object used to create the consumer
# + queueName - name of the target queue
public type QueueSenderEndpointConfiguration record {
    Session? session;
    string? queueName;
    !...
};

# JMS QueueSender action handling object
#
# + queueSender - Queue sender endpoint
public type QueueSenderActions object {

    public QueueSender? queueSender;

    # Sends a message to the JMS provider
    #
    # + message - message to be sent to the JMS provider
    # + return - error if unable to send the message to the queue
    public extern function send(Message message) returns error?;

    # Sends a message to a given destination of the JMS provider
    #
    # + destination - destination used for the message sender
    # + message - message to be sent to the JMS provider
    # + return - error if sending fails to the given destination
    public function sendTo(Destination destination, Message message) returns error?;
};

function QueueSenderActions::sendTo(Destination destination, Message message) returns error? {
    match (self.queueSender) {
        QueueSender queueSender => {
            match (queueSender.config.session) {
                Session s => {
                    validateQueue(destination);
                    queueSender.initQueueSender(s, destination = destination);
                }
                () => {}
            }
        }
        () => {log:printInfo("Message producer not properly initialised for queue " + destination.destinationName);}
    }
    return self.send(message);
}