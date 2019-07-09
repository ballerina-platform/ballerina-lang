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
import ballerina/'lang\.object as lang;

# The JMS DurableTopicListener endpoint.
#
# + consumerActions - The object, which handles the network operations related to the subscriber.
# + session - Session of the topic listener.
public type DurableTopicListener object {

    *lang:AbstractListener;

    public DurableTopicSubscriberCaller consumerActions = new;
    public Session session;

    # Initializes the DurableTopicListener endpoint.
    #
    # + c - The JMS session object or the configurations related to the receiver.
    # + topicPattern - Name or the pattern of the topic subscription.
    # + messageSelector - The JMS selector statement.
    # + identifier - The unique identifier for the subscription.
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

    # Binds the durable topic subscriber endpoint to a service.
    #
    # + s - Type descriptor of the service.
    # + name - The name of the service.
    # + return - Returns nil or an error upon failure to register the listener.
    public function __attach(service s, string? name = ()) returns error? {
        return self.registerListener(s);
    }

    function registerListener(service serviceType) returns error? = external;

    function createSubscriber(Session session, string topicPattern, string identifier, string messageSelector)
        returns error? = external;

    # Starts the endpoint. The function is ignored by the subscriber endpoint.
    #
    # + return - Returns nil or an error upon failure to start.
    public function __start() returns error? {
        return self.start();
    }
    private function start() returns error? = external;

    # Returns the subscrber caller actions.
    #
    # + return - DurableTopicListener actions handler.
    public function getCallerActions() returns DurableTopicSubscriberCaller {
        return self.consumerActions;
    }

    # Stops consuming messages from the DurableTopicListener.
    #
    # + return - Returns nil or an error upon failure to close the subscriber.
    public function __stop() returns error? {
        return self.closeSubscriber();
    }

    function closeSubscriber() returns error? = external;
};

# Caller actions related to the DurableTopicSubscriber.
public type DurableTopicSubscriberCaller client object {

    # Synchronously receives a message from the JMS provider.
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received.
    # + return - Returns a message or nil if the timeout exceeds, or returns an error upon an internal error of the JMS provider.
    public remote function receive(int timeoutInMilliSeconds = 0) returns Message|error? = external;
};
