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
# + session - Session of the queue sender
public type QueueSender client object {

    public Session? session;

    # Initialize the QueueSender endpoint
    #
    # + c - The JMS Session object or Configurations related to the receiver
    # + queueName - Name of the target queue
    public function __init(Session|SenderEndpointConfiguration c, string? queueName = ()) {
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
        if (queueName is string) {
            self.initQueueSender(self.session, queueName);
        }
    }

    extern function initQueueSender(Session? session, string|Destination dest);

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
    public remote function sendTo(Destination destination, Message message) returns error? {
        validateQueue(destination);
        self.initQueueSender(self.session, destination);
        return self->send(message);
    }
};
