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

import ballerina/jms;
import ballerina/log;
# Ballerina message broker simple topic subscriber
#
# + config - Simple topic subscrirber enpoint configuration
public type SimpleTopicSubscriber object {

    public SimpleTopicSubscriberEndpointConfiguration config;

    private jms:SimpleTopicSubscriber subscriber;
    private TopicSubscriberActions? consumerActions;

    # Initialize simple topic subscriber
    #
    # + c - Simple topic subscrirber enpoint configuration
    public function init(SimpleTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        self.subscriber.init({
                initialContextFactory:"bmbInitialContextFactory",
                providerUrl:getConnectionUrl(c),
                connectionFactoryName: c.connectionFactoryName,
                acknowledgementMode: c.acknowledgementMode,
                messageSelector: c.messageSelector,
                properties: c.properties,
                topicPattern: c.topicPattern
            }
        );
        self.consumerActions = new TopicSubscriberActions(self.subscriber.getCallerActions());
    }

    # Register simple topic subscriber endpoint
    #
    # + serviceType - Type descriptor of the service
    public function register(typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    # Start simple topic subscriber endpoint
    public function start() {
        self.subscriber.start();
    }

    # Get simple topic subscriber actions
    #
    # + return - the caller action object of the `TopicPublisher`
    public function getCallerActions() returns TopicSubscriberActions {
        match (self.consumerActions) {
            TopicSubscriberActions c => return c;
            () => {
                error e = {message:"Topic subscriber consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    # Stop simple topic subscriber endpoint
    public function stop() {
        self.subscriber.stop();
    }

    # Create JMS text message
    #
    # + message - A message body to create a text message
    # + return - the `Message` object created using the `string` message. or `error` on failure
    public function createTextMessage(string message) returns Message|error {
        var result = self.subscriber.createTextMessage(message);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
        }
    }
};

# Configuration related to simple topic subscriber endpoint
#
# + username - Valid user to connect to the Ballerina message broker
# + password - Password of the user
# + host - Hostname of the Ballerina message broker
# + port - Hostname of the Ballerina message broker
# + clientID - Used to identify the JMS client
# + virtualHost - Name of the virtual host where the virtual host is a path that acts as a namespace
# + secureSocket - Configuration for the TLS options to be used
# + connectionFactoryName - JNDI name of the connection factory
# + acknowledgementMode - JMS session acknowledgement mode. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + messageSelector - Message selector condition to filter messages
# + properties - JMS message properties
# + topicPattern - Topic name pattern
public type SimpleTopicSubscriberEndpointConfiguration record {
    string username = "admin";
    string password = "admin";
    string host = "localhost";
    int port = 5672;
    string clientID = "ballerina";
    string virtualHost = "default";
    ServiceSecureSocket? secureSocket;
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector;
    map properties;
    string topicPattern;
    !...
};

# Actions that topic subscriber endpoint could perform
#
# + helper - the jms TopicSubscriberActions
public type TopicSubscriberActions object {

    public jms:TopicSubscriberActions helper;

    public new(helper) {

    }

    # Acknowledges a received message
    #
    # + message - Message to be acknowledged
    # + return - `error` on failure to acknowledge
    public function acknowledge(Message message) returns error? {
        return self.helper.acknowledge(message.getJMSMessage());
    }

    # Synchronously receive a message from Ballerina message broker
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceededs. Returns an error on broker internal error.
    public function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        var result = self.helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }
};
