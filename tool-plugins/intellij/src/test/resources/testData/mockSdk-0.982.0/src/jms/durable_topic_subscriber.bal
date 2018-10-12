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

# Durable Topic Subscriber
#
# + consumerActions - Object that handles network operations related to the subscriber
# + config - Configurations related to the subscriber
public type DurableTopicSubscriber object {

    public DurableTopicSubscriberActions consumerActions;
    public DurableTopicSubscriberEndpointConfiguration config;

    # Initialize durable topic subscriber endpoint
    #
    # + c - Configurations for a durable topic subscriber
    public function init(DurableTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => {
                self.createSubscriber(s, c.messageSelector);
                log:printInfo("Durable subscriber created for topic " + c.topicPattern);
            }
            () => {}
        }
    }

    # Binds the durable topic subscriber endpoint to a service
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    extern function registerListener(typedesc serviceType, DurableTopicSubscriberActions actions);

    extern function createSubscriber(Session session, string messageSelector);

    # Starts the endpoint. Function is ignored by the subscriber endpoint
    public function start() {
    }

    # Return the subscrber caller actions
    #
    # + return - durable topic subscriber actions
    public function getCallerActions() returns DurableTopicSubscriberActions {
        return consumerActions;
    }

    # Ends consuming messages from the durable topic subscriber endpoint
    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    extern function closeSubscriber(DurableTopicSubscriberActions actions);
};

# Configurations related to the durable topic subscriber endpoint
#
# + session - JMS session object
# + topicPattern - Name or the pattern of the topic subscription
# + messageSelector - JMS selector statement
# + identifier - unique identifier for the subscription
public type DurableTopicSubscriberEndpointConfiguration record {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;
    !...
};

# Caller actions related to durable topic subscriber endpoint
public type DurableTopicSubscriberActions object {

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
