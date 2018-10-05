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

documentation { JMS simple topic subscriber
    E{{}}
    F{{config}} Simple topic subscrirber enpoint configuration
}
public type SimpleTopicSubscriber object {

    public SimpleTopicSubscriberEndpointConfiguration config;

    private Connection? connection;
    private Session? session;
    private TopicSubscriber? subscriber;

    documentation { Initialize simple topic subscirber endpoint
        P{{c}} Simple topic subscrirber enpoint configuration
    }
    public function init(SimpleTopicSubscriberEndpointConfiguration c) {
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

        TopicSubscriber topicSubscriber = new;
        TopicSubscriberEndpointConfiguration consumerConfig = {
            session:newSession,
            topicPattern: c.topicPattern,
            messageSelector: c.messageSelector
        };
        topicSubscriber.init(consumerConfig);
        self.subscriber = topicSubscriber;
    }

    documentation { Register simple topic subscriber endpoint
        P{{serviceType}} Type descriptor of the service
    }
    public function register(typedesc serviceType) {
        match (subscriber) {
            TopicSubscriber c => {
                c.register(serviceType);
            }
            () => {
                error e = {message:"Topic Subscriber cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Start simple topic subscriber endpoint }
    public function start() {

    }

    documentation { Get simple topic subscriber actions }
    public function getCallerActions() returns TopicSubscriberActions {
        match (subscriber) {
            TopicSubscriber c => return c.getCallerActions();
            () => {
                error e = {message:"Topic subscriber cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Stop simple topic subsriber endpoint }
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

documentation { Configuration related to simple topic subscriber endpoint
    F{{initialContextFactory}} JNDI initial context factory class
    F{{providerUrl}} JNDI provider URL
    F{{connectionFactoryName}}  JNDI name of the connection factory
    F{{acknowledgementMode}} JMS session acknwoledge mode
    F{{messageSelector}}  Message selector condition to filter messages
    F{{properties}} JMS message properties
    F{{topicPattern}} Topic name pattern
}
public type SimpleTopicSubscriberEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector;
    map properties;
    string topicPattern;
};
