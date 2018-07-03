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

documentation { Simplified queue receiver endpoint.
    A new connection and a session will be create when this endpoint is initialize. If your requirement is complex
    please refer QueueReceiver endpoint.

    E{{}}
    F{{config}} configurations related to the SimpleQueueReceiver endpoint
}
public type SimpleQueueReceiver object {

    public SimpleQueueReceiverEndpointConfiguration config;

    private Connection? connection;
    private Session? session;
    private QueueReceiver? queueReceiver;

    documentation { Initialize the SimpleQueueReceiver endpoint
        P{{c}} Configurations related to the SimpleQueueReceiver endpoint
    }
    public function init(SimpleQueueReceiverEndpointConfiguration c) {
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

        QueueReceiver receiver = new;
        QueueReceiverEndpointConfiguration queueReceiverConfig = {
            session:newSession,
            queueName: c.queueName,
            messageSelector: c.messageSelector
        };
        receiver.init(queueReceiverConfig);
        self.queueReceiver = receiver;
    }

    documentation { Binds the SimlpeQueueReceiver endpoint to a service
        P{{serviceType}} type descriptor of the service to bind to
    }
    public function register(typedesc serviceType) {
        match (queueReceiver) {
            QueueReceiver c => {
                c.register(serviceType);
            }
            () => {
                error e = {message:"Queue receiver cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Starts the endpoint. Function is ignored by the receiver endpoint }
    public function start() {

    }

    documentation { Retrieves the SimpleQueueReceiver consumer action handler
        R{{}} simple queue receiver action handler
    }
    public function getCallerActions() returns QueueReceiverActions {
        match (queueReceiver) {
            QueueReceiver c => return c.getCallerActions();
            () => {
                error e = {message:"Queue receiver cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Stops consuming messages through QueueReceiver endpoint }
    public function stop() {

    }

    documentation { Creates a JMS message which holds text content
        P{{content}} the text content used to initialize this message
    }
    public function createTextMessage(string content) returns Message|error {
        match (session) {
            Session s => return s.createTextMessage(content);
            () => {
                error e = {message:"Session cannot be null"};
                throw e;
            }
        }
    }
};

documentation { Configurations related to the SimpleQueueReceiver endpoint
    F{{initialContextFactory}} JMS provider specific inital context factory
    F{{providerUrl}} JMS provider specific provider URL used to configure a connection
    F{{connectionFactoryName}} JMS connection factory to be used in creating JMS connections
    F{{acknowledgementMode}} specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
    "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
    F{{messageSelector}} JMS selector statement
    F{{properties}} Additional properties use in initializing the initial context
    F{{queueName}} Name of the target queue
}
public type SimpleQueueReceiverEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector;
    map properties;
    string queueName;
};
