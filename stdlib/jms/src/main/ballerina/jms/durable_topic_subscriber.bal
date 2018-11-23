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

    *AbstractListener;

    //public DurableTopicSubscriberActions consumerActions = new;
    public DurableTopicSubscriberEndpointConfiguration config = {};

    public function __init(DurableTopicSubscriberEndpointConfiguration config) {
         self.config = config;
         init(config);
    }

    # Initialize durable topic subscriber endpoint
    #
    # + c - Configurations for a durable topic subscriber
    function init(DurableTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        var session = c.session;
        if (session is Session) {
            self.createSubscriber(session, c.messageSelector);
            log:printInfo("Durable subscriber created for topic " + c.topicPattern);
        } else {
            log:printInfo("Session is (), Cannot create a subscriber without a session");
        }
    }

    # Binds the durable topic subscriber endpoint to a service
    #
    # + s - service data which should be attached to listner
    # + data - annotation data which is defined in the service
    public function __attach(service s, map<any> data) returns error? {
        self.registerListener(s, data);
    }

    extern function registerListener(service serviceType, map<any> data);

    extern function createSubscriber(Session session, string messageSelector);

    # Starts the endpoint. Function is ignored by the subscriber endpoint
    public function __start() returns error?{
    }

    # Return the subscrber caller actions
    #
    # + return - durable topic subscriber actions
    public function getCallerActions() returns DurableTopicSubscriberActions {
        return self.consumerActions;
    }

    # Ends consuming messages from the durable topic subscriber endpoint
    public function __stop() returns error? {
        self.closeSubscriber(self.consumerActions);
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
    Session? session = ();
    string topicPattern = "";
    string messageSelector = "";
    string identifier = "";
    !...
};

# Caller actions related to durable topic subscriber endpoint
public type DurableTopicSubscriber client object {

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public client extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public client extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
