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
documentation { Ballerina message broker simple topic subscriber
    E{{}}
    F{{config}} Simple topic subscrirber enpoint configuration
}
public type SimpleTopicSubscriber object {

    public SimpleTopicSubscriberEndpointConfiguration config;

    private jms:SimpleTopicSubscriber subscriber;
    private TopicSubscriberActions? consumerActions;

    documentation { Initialize simple topic subscriber
        P{{c}} Simple topic subscrirber enpoint configuration
    }
    public function init(SimpleTopicSubscriberEndpointConfiguration c) {
        self.config = c;
        self.subscriber.init({
                initialContextFactory:"bmbInitialContextFactory",
                providerUrl:getConnectionUrl(c),
                connectionFactoryName: c.connectionFactoryName,
                acknowledgementMode: c.acknowledgementMode,
                messageSelector: c.messageSelector,
                properties: c.properties,
                topicPattern: c.topicPattern
            }
        );
        self.consumerActions = new TopicSubscriberActions(self.subscriber.getCallerActions());
    }

    documentation { Register simple topic subscriber endpoint
        P{{serviceType}} Type descriptor of the service
    }
    public function register(typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    documentation { Start simple topic subscriber endpoint }
    public function start() {
        self.subscriber.start();
    }

    documentation { Get simple topic subscriber actions }
    public function getCallerActions() returns TopicSubscriberActions {
        match (self.consumerActions) {
            TopicSubscriberActions c => return c;
            () => {
                error e = {message:"Topic subscriber consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    documentation { Stop simple topic subscriber endpoint }
    public function stop() {
        self.subscriber.stop();
    }

    documentation { Create JMS text message
        P{{message}} A message body to create a text message
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
public type SimpleTopicSubscriberEndpointConfiguration record {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    ServiceSecureSocket? secureSocket,
    string connectionFactoryName = "ConnectionFactory",
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    string messageSelector,
    map properties,
    string topicPattern,
};

documentation { Actions that topic subscriber endpoint could perform }
public type TopicSubscriberActions object {

    public jms:TopicSubscriberActions helper;

    public new(helper) {

    }

    documentation { Acknowledges a received message
        P{{message}} Message to be acknowledged
    }
    public function acknowledge(Message message) returns error? {
        return self.helper.acknowledge(message.getJMSMessage());
    }

    documentation { Synchronously receive a message from Ballerina message broker
        P{{timeoutInMilliSeconds}} Time to wait until a message is received
        R{{}} Returns a message or nill if the timeout exceededs. Returns an error on broker internal error.
    }
    public function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        var result = self.helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }
};
