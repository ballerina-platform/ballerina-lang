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

documentation { Simple Durable Topic Subscriber endpoint
    Simplified endpoint to consume from a topic without the explicit creation for JMS connection and session
    E{{}}
    F{{config}} configurations related to the endpoint
}
public type SimpleDurableTopicSubscriber object {

    public {
        SimpleDurableTopicSubscriberEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        DurableTopicSubscriber? subscriber;
    }

    documentation { Initializes the simple durable topic subscriber endpoint
        P{{config}} Configurations related to the endpoint
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

    documentation { Binds the endpoint to a service
        P{{serviceType}} type descriptor of the service to bind to
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

    documentation { Starts the endpoint. Function is ignored by the subscriber endpoint
    }
    public function start() {
        // Ignore
    }

    documentation { Retrieves the durable topic subscriber consumer actions
        R{{}} Durable topic subscriber actions
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

    documentation { Stops the endpoint. Function is ignored by the subscriber endpoint
    }
    public function stop() {
        // Ignore
    }

    documentation { Creates a text message that can be sent through any JMS message producer to a queue or topic.
        P{{message}} text content of the message
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

    documentation { Unsubscribes the durable subscriber from topic
    }
    public function unsubscribe() returns error? {
        match (self.session) {
            Session s => {
                var result = s.unsubscribe(self.config.identifier);
                match (result) {
                    error e => {
                        log:printError("Error occurred while unsubscribing with subscription id "
                                + self.config.identifier, err = e);
                        return e;
                    }
                    () => return;
                }
            }
            () => {
                log:printInfo("JMS session not set.");
                return;
            }
        }
    }
};

documentation { Configurations of the simple durable topic subscriber endpoint
    F{{initialContextFactory}} JMS initial context factory name
    F{{providerUrl}} Connection url of the JMS provider
    F{{connectionFactoryName}} Name of the JMS connection factory created
    F{{acknowledgementMode}} Sets the acknowledgment mode for the underlying session. String representation of the
    JMS acknowledgment mode needs to be provided.
    F{{identifier}} Unique identifier for the subscriber
    F{{properties}} Custom properties related to JMS provider
    F{{messageSelector}} JMS selector statement
    F{{topicPattern}} Name or the pattern of the topic subscription
}
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
