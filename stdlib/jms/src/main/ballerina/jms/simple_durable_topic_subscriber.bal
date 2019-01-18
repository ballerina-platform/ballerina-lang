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

# JMS Simplified DurableTopicSubscriber endpoint
# Simplified endpoint to consume from a topic without the explicit creation for JMS connection and session
#
# + config - Used to store configurations related to a JMS SimpleDurableTopicSubscriber
public type SimpleDurableTopicSubscriber object {

    *AbstractListener;

    public SimpleDurableTopicSubscriberEndpointConfiguration config = {};
    private Connection? connection = ();
    private Session? session = ();
    private DurableTopicSubscriber subscriber;
    private SimpleDurableTopicSubscriberCaller consumerActions;

    # Initializes the SimpleDurableTopicSubscriber endpoint
    #
    # + c - Configurations related to the SimpleDurableTopicSubscriber endpoint
    public function __init(SimpleDurableTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        Connection conn = new({
                initialContextFactory: self.config.initialContextFactory,
                providerUrl: self.config.providerUrl,
                connectionFactoryName: self.config.connectionFactoryName,
                properties: self.config.properties
            });
        self.connection = conn;

        Session newSession = new(conn, {
                acknowledgementMode: self.config.acknowledgementMode
            });
        self.session = newSession;

        DurableTopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: c.topicPattern,
            messageSelector: c.messageSelector,
            identifier: c.identifier
        };
        self.subscriber = new(consumerConfig);
        self.consumerActions = new SimpleDurableTopicSubscriberCaller(self.subscriber.getCallerActions(), newSession,
            c.identifier);
    }

    # Binds the endpoint to a service
    #
    # + serviceType - Type descriptor of the service to bind to
    # + data - Service annotations
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, map<any> data) returns error? {
        return self.subscriber.registerListener(serviceType, self.subscriber.consumerActions, data);
    }

    # Starts the endpoint. Function is ignored by the subscriber endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
        // Ignore
        return ();
    }

    # Retrieves the durable topic subscriber consumer actions
    #
    # + return - Durable topic subscriber actions
    public function getCallerActions() returns SimpleDurableTopicSubscriberCaller {
        return self.consumerActions;
    }

    # Stops the endpoint. Function is ignored by the subscriber endpoint
    #
    # + return - Nil or error upon failure to close subscriber
    public function __stop() returns error? {
        // Ignore
        return self.subscriber.closeSubscriber(self.subscriber.consumerActions);
    }

    # Creates a text message that can be sent through any JMS message producer to a queue or topic
    #
    # + message - Text content of the message
    # + return - Message or nil if the session is nil
    public function createTextMessage(string message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createTextMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map<any> errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
};

# Configurations of the simple durable topic subscriber endpoint
#
# + initialContextFactory - JMS initial context factory name
# + providerUrl - Connection url of the JMS provider
# + connectionFactoryName - Name of the JMS connection factory created
# + acknowledgementMode - Sets the acknowledgment mode for the underlying session. String representation of the
#                         JMS acknowledgment mode needs to be provided.
# + identifier - Unique identifier for the subscriber
# + properties - Custom properties related to JMS provider
# + messageSelector - JMS selector statement
# + topicPattern - Name or the pattern of the topic subscription
public type SimpleDurableTopicSubscriberEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier = "";
    map<any> properties = {};
    string messageSelector = "";
    string topicPattern = "";
    !...;
};


# Caller actions related to durable topic subscriber endpoint
public type SimpleDurableTopicSubscriberCaller client object {

    private DurableTopicSubscriberCaller helper;
    private Session session;
    private string identifier;

    public function __init(DurableTopicSubscriberCaller subscriberActions, Session session, string id) {
        self.helper = subscriberActions;
        self.session = session;
        self.identifier = id;
    }

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - Error upon failure to acknowledge the received message
    public remote function acknowledge(Message message) returns error? {
        return self.helper->acknowledge(message);
    }

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on JMS provider internal error.
    public remote function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        return self.helper->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
    }

    # Unsubscribes the durable subscriber from topic
    #
    # + return - Returns an error on JMS provider internal error
    public remote function unsubscribe() returns error? {
        return self.session.unsubscribe(self.identifier);
    }
};
