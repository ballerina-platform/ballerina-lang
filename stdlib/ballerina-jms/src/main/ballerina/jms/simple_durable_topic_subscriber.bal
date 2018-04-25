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

public type SimpleDurableTopicSubscriber object {

    public {
        SimpleDurableTopicSubscriberEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        DurableTopicSubscriber? subscriber;
    }

    public function init(SimpleDurableTopicSubscriberEndpointConfiguration config) {
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

        DurableTopicSubscriber topicSubscriber = new;
        DurableTopicSubscriberEndpointConfiguration consumerConfig = {
            session:newSession,
            topicPattern:config.topicPattern,
            messageSelector:config.messageSelector,
            identifier:config.identifier
        };
        topicSubscriber.init(consumerConfig);
        self.subscriber = topicSubscriber;
    }

    public function register(typedesc serviceType) {
        match (subscriber) {
            DurableTopicSubscriber c => {
                c.register(serviceType);
            }
            () => {
                error e = {message:"Topic Subscriber cannot be nil"};
                throw e;
            }
        }
    }

    public function start() {

    }

    public function getCallerActions() returns DurableTopicSubscriberActions {
        match (subscriber) {
            DurableTopicSubscriber c => return c.getCallerActions();
            () => {
                error e = {message:"Topic subscriber cannot be nil"};
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

public type SimpleDurableTopicSubscriberEndpointConfiguration {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier,
    map properties;
    string messageSelector;
    string topicPattern;
};
