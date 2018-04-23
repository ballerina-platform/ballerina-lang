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

public type SimpleQueueSender object {

    public {
        SimpleQueueSenderEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        QueueSender? sender;
    }

    public function init(SimpleQueueSenderEndpointConfiguration config) {
        self.config = config;
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
            queueName:config.queueName
        };
        queueSender.init(senderConfig);
        self.sender = queueSender;
    }

    public function register(typedesc serviceType) {

    }

    public function start() {

    }

    public function getCallerActions() returns QueueSenderActions {
        match (sender) {
            QueueSender s => return s.getCallerActions();
            () => {
                error e = {message:"Queue sender cannot be nil"};
                throw e;
            }
        }
    }

    public function stop() {
    }

    public function createTextMessage(string message) returns Message|error {
        match (session) {
            Session s => return s.createTextMessage(message);
            () => {
                error e = {message:"Session cannot be nil"};
                throw e;
            }
        }
    }
};

public type SimpleQueueSenderEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string queueName;
};
