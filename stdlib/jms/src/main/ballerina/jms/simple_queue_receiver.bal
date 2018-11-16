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

# Simplified queue receiver endpoint.
# A new connection and a session will be create when this endpoint is initialize. If your requirement is complex
# please refer QueueReceiver endpoint.
#
# + config - configurations related to the SimpleQueueReceiver endpoint
public type SimpleQueueReceiver object {

    public SimpleQueueReceiverEndpointConfiguration config = {};

    private Connection? connection;
    private Session? session = ();
    private QueueReceiver? queueReceiver = ();

    # Initialize the SimpleQueueReceiver endpoint
    #
    # + c - Configurations related to the SimpleQueueReceiver endpoint
    public function init(SimpleQueueReceiverEndpointConfiguration c) {
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

        QueueReceiver receiver = new;
        QueueReceiverEndpointConfiguration queueReceiverConfig = {
            session: newSession,
            queueName: c.queueName,
            messageSelector: c.messageSelector
        };
        receiver.init(queueReceiverConfig);
        self.queueReceiver = receiver;
    }

    # Binds the SimlpeQueueReceiver endpoint to a service
    #
    # + serviceType - type descriptor of the service to bind to
    public function register(typedesc serviceType) {
        var queueReceiver = self.queueReceiver;
        if (queueReceiver is QueueReceiver) {
            queueReceiver.register(serviceType);
        } else {
            string errorMessage = "Queue receiver cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Starts the endpoint. Function is ignored by the receiver endpoint
    public function start() {

    }

    # Retrieves the SimpleQueueReceiver consumer action handler
    #
    # + return - simple queue receiver action handler
    public function getCallerActions() returns QueueReceiverActions {
        var queueReceiver = self.queueReceiver;
        if (queueReceiver is QueueReceiver) {
            return queueReceiver.getCallerActions();
        } else {
            string errorMessage = "Queue receiver cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }

    # Stops consuming messages through QueueReceiver endpoint
    public function stop() {

    }

    # Creates a JMS message which holds text content
    #
    # + content - the text content used to initialize this message
    # + return - the created message, or nil if the session is nil
    public function createTextMessage(string content) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createTextMessage(content);
        } else {
            string errorMessage = "Session cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
};

# Configurations related to the SimpleQueueReceiver endpoint
#
# + initialContextFactory - JMS provider specific inital context factory
# + providerUrl - JMS provider specific provider URL used to configure a connection
# + connectionFactoryName - JMS connection factory to be used in creating JMS connections
# + acknowledgementMode - specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + messageSelector - JMS selector statement
# + properties - Additional properties use in initializing the initial context
# + queueName - Name of the target queue
public type SimpleQueueReceiverEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector = "";
    map properties = {};
    string queueName = "";
    !...
};
