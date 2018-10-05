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

documentation { JMS simple topic publisher
    E{{}}
    F{{config}} Simple topic publisher enpoint configuration
}
public type SimpleTopicPublisher object {

    public SimpleTopicPublisherEndpointConfiguration config;

    private Connection? connection;
    private Session? session;
    private TopicPublisher? publisher;

    documentation { Initialize simple topic publisher endpoint
        P{{c}} Simple topic publisher enpoint configuration
    }
    public function init(SimpleTopicPublisherEndpointConfiguration c) {
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

        TopicPublisher topicPublisher = new;
        TopicPublisherEndpointConfiguration publisherConfig = {
            session:newSession,
            topicPattern: c.topicPattern
        };
        topicPublisher.init(publisherConfig);
        self.publisher = topicPublisher;
    }

    documentation { Register simple topic publisher endpoint
        P{{serviceType}} Type descriptor of the service
    }
    public function register(typedesc serviceType) {

    }

    documentation { Start simple topic pubilsher endpoint }
    public function start() {

    }

    documentation { Get simple topic pubilsher actions }
    public function getCallerActions() returns TopicPublisherActions {
        match (publisher) {
            TopicPublisher s => return s.getCallerActions();
            () => {
                error e = {message:"Topic publisher cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Stop simple topic pubilsher endpoint }
    public function stop() {

    }

    documentation { Create JMS text message
        P{{message}} A message body to create a text message
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
    documentation { Create JMS map message
        P{{message}} A message body to create a map message
    }
    public function createMapMessage(map message) returns Message|error {
        match (session) {
            Session s => return s.createMapMessage(message);
            () => {
                error e = {message:"Session cannot be nil"};
                throw e;
            }
        }
    }
};

documentation { Configuration related to simple topic publisher endpoint
    F{{initialContextFactory}} JNDI initial context factory class
    F{{providerUrl}} JNDI provider URL
    F{{connectionFactoryName}}  JNDI name of the connection factory
    F{{acknowledgementMode}} JMS session acknwoledge mode
    F{{properties}} JMS message properties
    F{{topicPattern}} name of the target topic
}
public type SimpleTopicPublisherEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string topicPattern;
};
