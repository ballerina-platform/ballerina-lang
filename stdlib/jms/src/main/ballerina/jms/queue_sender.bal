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
# + config - Used to store configurations related to a JMS QueueSender
public type QueueSender client object {

    public QueueSenderEndpointConfiguration config = {};

    # Initialize the QueueSender endpoint
    #
    # + c - Configurations related to the QueueSender endpoint
    public function __init(QueueSenderEndpointConfiguration c) {
        self.config = c;
        var session = c.session;
        if (session is Session) {
            var queueName = c.queueName;
            if (queueName is string) {
                self.initQueueSender(session);
            } else {
                log:printInfo("Message producer not properly initialized for queue");
            }
        } else {
            log:printInfo("Message producer not properly initialized for queue");
        }
    }

    extern function initQueueSender(Session session, Destination? destination = ());

    # Sends a message to the JMS provider
    #
    # + message - Message to be sent to the JMS provider
    # + return - Error if unable to send the message to the queue
    public remote extern function send(Message message) returns error?;

    # Sends a message to a given destination of the JMS provider
    #
    # + destination - Destination used for the message sender
    # + message - Message to be sent to the JMS provider
    # + return - Error if sending to the given destination fails
    public remote function sendTo(Destination destination, Message message) returns error?;
};

remote function QueueSender.sendTo(Destination destination, Message message) returns error? {
    var session = self.config.session;
    if (session is Session) {
        validateQueue(destination);
        self.initQueueSender(session, destination = destination);
    } else {
        log:printInfo("Message producer not properly initialized for queue " + destination.destinationName);
    }
    return self->send(message);
}

# Configurations related to a QueueSender object
#
# + session - JMS session object used to create the consumer
# + queueName - Name of the target queue
public type QueueSenderEndpointConfiguration record {
    Session? session = ();
    string? queueName = ();
    !...;
};
