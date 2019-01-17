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
# + config - Used to store configurations related to JMS TopicPublisher
public type TopicPublisher client object {

    public TopicPublisherEndpointConfiguration config = {};

    # Initialize the TopicPublisher endpoint
    #
    # + c - Configurations related to the TopicPublisher endpoint
    public function __init(TopicPublisherEndpointConfiguration c) {
        self.config = c;
        var session = c.session;
        if (session is Session) {
            var topicPattern = c.topicPattern;
            if (topicPattern is string) {
                self.initTopicPublisher(session);
            } else {
                log:printInfo("Topic publisher is not properly initialized for the topic");
            }
        } else {
            log:printInfo("Topic publisher is not properly initialized for the topic");
        }
    }

    public extern function initTopicPublisher(Session session, Destination? destination = ());

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
    public remote function sendTo(Destination destination, Message message) returns error?;
};

remote function TopicPublisher.sendTo(Destination destination, Message message) returns error? {
    var session = self.config.session;
    if (session is Session) {
        validateTopic(destination);
        self.initTopicPublisher(session, destination = destination);
    } else {
        log:printInfo("Session is (), Topic publisher is not properly initialized");
    }
    return self->send(message);
}

# Configuration related to the TopicPublisher endpoint
#
# + session - Session object used to create TopicPublisher
# + topicPattern - Topic name pattern
public type TopicPublisherEndpointConfiguration record {
    Session? session = ();
    string? topicPattern = ();
    !...;
};
