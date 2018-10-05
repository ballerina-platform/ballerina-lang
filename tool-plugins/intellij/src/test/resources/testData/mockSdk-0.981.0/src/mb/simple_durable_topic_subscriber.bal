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

documentation { Simple Durable Topic Subscriber endpoint
    E{{}}
    F{{config}} configurations related to the endpoint
}
public type SimpleDurableTopicSubscriber object {

    public SimpleDurableTopicSubscriberEndpointConfiguration config;

    private jms:SimpleDurableTopicSubscriber subscriber;
    private DurableTopicSubscriberActions? consumerActions;

    documentation { Initializes the simple durable topic subscriber endpoint
        P{{c}} Configurations related to the endpoint
    }
    public function init(SimpleDurableTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        self.subscriber.init({
                initialContextFactory:"wso2mbInitialContextFactory",
                providerUrl:getConnectionUrl(c),
                acknowledgementMode: c.acknowledgementMode,
                identifier: c.identifier,
                properties: c.properties,
                messageSelector: c.messageSelector,
                topicPattern: c.topicPattern
            });
        self.consumerActions = new DurableTopicSubscriberActions(self.subscriber.getCallerActions());
    }

    documentation { Binds the endpoint to a service
        P{{serviceType}} type descriptor of the service to bind to
    }
    public function register(typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    documentation { Starts the endpoint. Function is ignored by the subscriber endpoint
    }
    public function start() {
        self.subscriber.start();
    }

    documentation { Retrieves the durable topic subscriber consumer actions
        R{{}} Durable topic subscriber actions
    }
    public function getCallerActions() returns DurableTopicSubscriberActions {
        match (self.consumerActions) {
            DurableTopicSubscriberActions c => return c;
            () => {
                error e = {message:"Durable topic subscriber consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    documentation { Stops the endpoint. Function is ignored by the subscriber endpoint
    }
    public function stop() {
        self.subscriber.stop();
    }
    documentation { Creates a text message that can be sent through any JMS message producer to a queue or topic.
        P{{message}} text content of the message
    }
    public function createTextMessage(string message) returns Message|error {
        var result = self.subscriber.createTextMessage(message);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
        }
    }
};

documentation { Configuration related to simple topic subscriber endpoint
    F{{username}} Valid user to connect to the Ballerina message broker
    F{{password}} Password of the user
    F{{host}} Hostname of the Ballerina message broker
    F{{port}} Hostname of the Ballerina message broker
    F{{clientID}} Used to identify the JMS client
    F{{virtualHost}} Name of the virtual host where the virtual host is a path that acts as a namespace
    F{{connectionFactoryName}} JNDI name of the connection factory
    F{{acknowledgementMode}} JMS session acknowledgement mode. Legal values are "AUTO_ACKNOWLEDGE",
    "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
    F{{messageSelector}} Message selector condition to filter messages
    F{{properties}} JMS message properties
    F{{topicPattern}} Topic name pattern
}
public type SimpleDurableTopicSubscriberEndpointConfiguration record {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    ServiceSecureSocket? secureSocket,
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier,
    map properties;
    string messageSelector;
    string topicPattern;
};

public type DurableTopicSubscriberActions object {

    public jms:SimpleDurableTopicSubscriberActions helper;

    public new(helper) {}

    public function acknowledge(Message message) returns error? {
        return self.helper.acknowledge(message.getJMSMessage());
    }

    public function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        var result = self.helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }

    documentation { Unsubscribes the durable subscriber from topic
    }
    public function unsubscribe() returns error? {
        return self.helper.unsubscribe();
    }

};
