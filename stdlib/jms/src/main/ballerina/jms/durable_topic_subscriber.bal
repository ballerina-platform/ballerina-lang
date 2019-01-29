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

# JMS DurableTopicSubscriber endpoint
#
# + consumerActions - Object that handles network operations related to the subscriber
# + session - Session of the topic subscriber
public type DurableTopicSubscriber object {

    *AbstractListener;

    public DurableTopicSubscriberCaller consumerActions = new;
    public Session? session;

    # Initialize DurableTopicSubscriber endpoint.
    #
    # + c - The JMS Session object or Configurations related to the receiver
    # + topicPattern - Name or the pattern of the topic subscription
    # + messageSelector - JMS selector statement
    # + identifier - Unique identifier for the subscription
    public function __init(Session | ReceiverEndpointConfiguration c, string topicPattern, string identifier, string messageSelector = "") {
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
        var err = self.createSubscriber(self.session, topicPattern, identifier, messageSelector);
        if (err is error) {
            panic err;
        }
    }

    # Binds the durable topic subscriber endpoint to a service
    #
    # + serviceType - Type descriptor of the service
    # + data - Service annotations
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, map<any> data) returns error? {
        return self.registerListener(serviceType, self.consumerActions, data);
    }

    extern function registerListener(service serviceType, DurableTopicSubscriberCaller actions,
 map<any> data) returns error?;

    extern function createSubscriber(Session? session, string topicPattern, string identifier, string messageSelector) returns error?;

    # Starts the endpoint. Function is ignored by the subscriber endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
        return ();
    }

    # Return the subscrber caller actions
    #
    # + return - DurableTopicSubscriber actions handler
    public function getCallerActions() returns DurableTopicSubscriberCaller {
        return self.consumerActions;
    }

    # Ends consuming messages from the DurableTopicSubscriber endpoint
    #
    # + return - Nil or error upon failure to close subscriber
    public function __stop() returns error? {
        return self.closeSubscriber(self.consumerActions);
    }

    extern function closeSubscriber(DurableTopicSubscriberCaller actions) returns error?;
};

# Caller actions related to DurableTopicSubscriber endpoint
public type DurableTopicSubscriberCaller client object {

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - Error upon failure to acknowledge the received message
    public remote extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds, returns an error upon JMS provider internal error
    public remote extern function receive(int timeoutInMilliSeconds = 0) returns (Message | error)?;
};
