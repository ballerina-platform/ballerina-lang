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

# Simplified queue receiver endpoint.
# A new connection and a session will be create when this endpoint is initialize.
#
# + config - configurations related to the SimpleQueueReceiver endpoint
public type SimpleQueueReceiver object {

    public SimpleQueueListenerEndpointConfiguration config;

    private jms:SimpleQueueReceiver receiver;
    private QueueReceiverActions? consumerActions;

    # Initialize the SimpleQueueReceiver endpoint
    #
    # + c - Configurations related to the SimpleQueueReceiver endpoint
    public function init(SimpleQueueListenerEndpointConfiguration c) {
        self.config = c;
        self.receiver.init({
                initialContextFactory:"bmbInitialContextFactory",
                providerUrl:getConnectionUrl(c),
                connectionFactoryName: c.connectionFactoryName,
                acknowledgementMode: c.acknowledgementMode,
                messageSelector: c.messageSelector,
                properties: c.properties,
                queueName: c.queueName
            });

        self.consumerActions = new QueueReceiverActions(self.receiver.getCallerActions());
    }

    # Binds the SimlpeQueueReceiver endpoint to a service
    #
    # + serviceType - type descriptor of the service to bind to
    public function register(typedesc serviceType) {
        self.receiver.register(serviceType);
    }

    # Starts the endpoint. Function is ignored by the receiver endpoint
    public function start() {
        self.receiver.start();
    }

    # Retrieves the SimpleQueueReceiver consumer action handler
    #
    # + return - simple queue receiver action handler

    public function getCallerActions() returns QueueReceiverActions {
        match (self.consumerActions) {
            QueueReceiverActions c => return c;
            () => {
                error e = {message:"Queue receiver consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    # Stops consuming messages through QueueReceiver endpoint
    public function stop() {
        receiver.stop();
    }

    # Creates a message which holds text content
    #
    # + content - the text content used to initialize this message
    # + return - the `Message` object created using the `string` message. or `error` on failure
    public function createTextMessage(string content) returns Message|error {
        var result = self.receiver.createTextMessage(content);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
        }
    }
};

# Configurations related to SimpleQueueReceiver endpoint
#
# + username - The caller's user name
# + password - The caller's password
# + host - Hostname of the broker node
# + port - AMQP port of the broker node
# + clientID - Identifier used to uniquely identify the client connection
# + virtualHost - target virtualhost
# + secureSocket - Configuration for the TLS options to be used
# + connectionFactoryName - the name of the connection factory
# + acknowledgementMode - specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + messageSelector - JMS selector statement
# + properties - Additional properties use in initializing the initial context
# + queueName - Name of the target queue
public type SimpleQueueListenerEndpointConfiguration record {
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
    string queueName;
    !...
};

# Caller action handler related to SimpleQueueReceiver endpoint
#
# + helper - the jms QueueReceiverActions
public type QueueReceiverActions object {

    public jms:QueueReceiverActions helper;

    public new(helper) {}

    # Acknowledges a received message
    #
    # + message - message to be acknowledged
    # + return - `error` if the acknowledgement fails
    public function acknowledge(Message message) returns error? {
        return helper.acknowledge(message.getJMSMessage());
    }

    # Synchronously receive a message from Ballerina message broker
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - `Message` or nil if the timeout exceededs. Returns an `error` on broker internal error.
    public function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        var result = helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }
};
