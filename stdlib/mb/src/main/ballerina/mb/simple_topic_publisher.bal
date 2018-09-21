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

# Simplified topic publisher
# A new connection and a session will be create when this endpoint is initialize.
#
# + config - configurations related to the SimpleTopicPublisher endpoint
public type SimpleTopicPublisher object {

    public SimpleTopicPublisherEndpointConfiguration config;

    private jms:SimpleTopicPublisher? publisher;
    private TopicPublisherActions? producerActions;

    # Initialize SimpleTopicPublisher endpoint
    #
    # + c - Configurations related to SimpleTopicPublisher endpoint
    public function init(SimpleTopicPublisherEndpointConfiguration c) {
        endpoint jms:SimpleTopicPublisher ep {
            initialContextFactory:"bmbInitialContextFactory",
            providerUrl:getConnectionUrl(c),
            connectionFactoryName:"ConnectionFactory",
            acknowledgementMode: c.acknowledgementMode,
            properties: c.properties,
            topicPattern: c.topicPattern
        };
        self.publisher = ep;
        self.producerActions = new TopicPublisherActions(ep);
        self.config = c;
    }

    # Registers the endpoint in the service.
    # This method is generally not used since SimpleTopicPublisher is a non-service endpoint.
    # + serviceType - type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Start simple topic pubilsher endpoint
    public function start() {

    }

    # Get simple topic publisher actions
    #
    # + return - the caller action object of the `TopicPublisher`
    public function getCallerActions() returns TopicPublisherActions {
        match (self.producerActions) {
            TopicPublisherActions s => return s;
            () => {
                error e = {message:"Topic publisher connector cannot be nil"};
                throw e;
            }
        }
    }

    # Stop simple topic pubilsher endpoint
    public function stop() {

    }

    # Create JMS text message
    #
    # + message - A message body to create a text message
    # + return - the `Message` object created using the `string` message. or `error` on failure
    public function createTextMessage(string message) returns Message|error {
        match (self.publisher) {
            jms:SimpleTopicPublisher s => {
                var result = s.createTextMessage(message);
                match (result) {
                    jms:Message m => return new Message(m);
                    error e => return e;
                }
            }
            () => {
                error e = {message:"topic publisher cannot be nil"};
                throw e;
            }
        }

    }
};

# Caller action handler related to SimpleQueuePublisher endpoint
public type TopicPublisherActions object {

    private jms:SimpleTopicPublisher publisher;

    new(publisher) {}

    # Sends a message to Ballerina message broker
    #
    # + message - message to be sent to Ballerina message broker
    # + return - `error` on failure to send the `Message`
    public function send(Message message) returns error? {
        endpoint jms:SimpleTopicPublisher publisherEP = self.publisher;
        var result = publisherEP->send(message.getJMSMessage());
        return result;
    }
};

# Configurations related to SimpleQueueSender endpoint
#
# + username - The caller's user name
# + password - The caller's password
# + host - Hostname of the broker node
# + port - AMQP port of the broker node
# + clientID - Identifier used to uniquely identify the client connection
# + virtualHost - target virtualhost
# + secureSocket - Configuration for the TLS options to be used
# + acknowledgementMode - specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + properties - Additional properties use in initializing the initial context
# + topicPattern - name of the target topic
public type SimpleTopicPublisherEndpointConfiguration record {
    string username = "admin";
    string password = "admin";
    string host = "localhost";
    int port = 5672;
    string clientID = "ballerina";
    string virtualHost = "default";
    ServiceSecureSocket? secureSocket;
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string topicPattern;
    !...
};
