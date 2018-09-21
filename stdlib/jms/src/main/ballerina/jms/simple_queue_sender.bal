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

# Simplified queue sender object
# A new connection and a session will be create when this endpoint is initialize. If your requirement is complex
# please refer QueueSender endpoint.
#
# + config - configurations related to the SimpleQueueSender endpoint
public type SimpleQueueSender object {

    public SimpleQueueSenderEndpointConfiguration config;

    private Connection? connection;
    private Session? session;
    private QueueSender? sender;

    # Initialize the SimpleQueueSender endpoint
    #
    # + c - Configurations related to the SimpleQueueSender endpoint
    public function init(SimpleQueueSenderEndpointConfiguration c) {
        self.config = c;
        Connection conn = new({
                initialContextFactory:config.initialContextFactory,
                providerUrl:config.providerUrl,
                connectionFactoryName:config.connectionFactoryName,
                properties:config.properties
            });
        self.connection = conn;

        Session newSession = new(conn, {
                acknowledgementMode:config.acknowledgementMode
            });
        self.session = newSession;

        QueueSender queueSender = new;
        QueueSenderEndpointConfiguration senderConfig = {
            session:newSession,
            queueName: c.queueName
        };
        queueSender.init(senderConfig);
        self.sender = queueSender;
    }

    # Registers the endpoint in the service.
    # This method is not used since SimpleQueueSender is a non-service endpoint.
    #
    # + serviceType - type descriptor of the service
    public function register(typedesc serviceType) {

    }

    # Starts the SimpleQueueSender endpoint
    public function start() {

    }

    # Returns the caller action object of the SimpleQueueSender
    #
    # + return - Simple queue sender actions
    public function getCallerActions() returns QueueSenderActions {
        match (sender) {
            QueueSender s => return s.getCallerActions();
            () => {
                error e = {message:"Queue sender cannot be nil"};
                throw e;
            }
        }
    }

    # Stops the  SimpleQueueSender endpoint
    public function stop() {
    }

    # Creates a JMS message which holds text content
    #
    # + content - the text content used to initialize this message
    # + return - a message or nil if the session is nil
    public function createTextMessage(string content) returns Message|error {
        match (session) {
            Session s => return s.createTextMessage(content);
            () => {
                error e = {message:"Session cannot be nil"};
                throw e;
            }
        }
    }
};

# Configurations related to the SimpleQueueSender endpoint
#
# + initialContextFactory - JMS provider specific inital context factory
# + providerUrl - JMS provider specific provider URL used to configure a connection
# + connectionFactoryName - JMS connection factory to be used in creating JMS connections
# + acknowledgementMode - specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
# + properties - Additional properties use in initializing the initial context
# + queueName - Name of the target queue
public type SimpleQueueSenderEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string queueName;
    !...
};
