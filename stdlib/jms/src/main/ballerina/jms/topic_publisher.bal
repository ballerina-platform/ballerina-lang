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

# JMS TopicPublisher endpoint
#
# + session - Session of the topic publisher
public type TopicPublisher client object {

    public Session? session;

    # Initialize the TopicPublisher endpoint
    #
    # + c - The JMS Session object or Configurations related to the receiver
    # + topicPattern - Topic name pattern
    public function __init(Session|SenderEndpointConfiguration c, string? topicPattern = ()) {
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
            self.initTopicPublisher(self.session, topicPattern);
        }
    }

    public extern function initTopicPublisher(Session? session, string|Destination dest);

    # Sends a message to the JMS provider
    #
    # + message - Message to be sent to the JMS provider
    # + return - Error upon failure to send the message to the JMS provider
    public remote extern function send(Message message) returns error?;

    # Sends a message to the JMS provider
    #
    # + destination - Destination used for the message sender
    # + message - Message to be sent to the JMS provider
    # + return - Error upon failure to send the message to the JMS provider
    public remote function sendTo(Destination destination, Message message) returns error? {
        validateTopic(destination);
        self.initTopicPublisher(self.session, destination);
        return self->send(message);
    }
};
