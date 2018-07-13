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

# JMS topic publisher
#
# + producerActions - Topic publisher endpoint actions
# + config - Topic publisher endpoint configuration
public type TopicPublisher object {
    public TopicPublisherActions producerActions;
    public TopicPublisherEndpointConfiguration config;

    # Topic publisher contructor
    new() {
        self.producerActions = new;
    }

    # Initialize topic publisher endpoint
    #
    # + c - Topic publisher endpoint configuration
    public function init(TopicPublisherEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => self.initTopicPublisher(s);
            () => {}
        }
    }

    public native function initTopicPublisher(Session session);

    # Register topic publisher endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Start topic publisher endpoint
    public function start() {

    }

    # Get topic publisher actions
    public function getCallerActions() returns TopicPublisherActions {
        return self.producerActions;
    }

    # Stop topic publisher endpoint
    public function stop() {

    }
};

# Configuration related to the topic publisher endpoint
#
# + session - Session object used to create topic publisher
# + topicPattern - Topic name pattern
public type TopicPublisherEndpointConfiguration record {
    Session? session;
    string topicPattern;
};

# Actions that topic publisher endpoint could perform
public type TopicPublisherActions object {

    # Sends a message to the JMS provider
    #
    # + message - Message to be sent to the JMS provider
    public native function send(Message message) returns error?;
};
