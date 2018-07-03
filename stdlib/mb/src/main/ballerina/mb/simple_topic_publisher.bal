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

documentation { Simplified topic publisher
    A new connection and a session will be create when this endpoint is initialize.

    E{{}}
    F{{config}} configurations related to the SimpleTopicPublisher endpoint
}
public type SimpleTopicPublisher object {

    public SimpleTopicPublisherEndpointConfiguration config;

    private jms:SimpleTopicPublisher? publisher;
    private TopicPublisherActions? producerActions;

    documentation { Initialize SimpleTopicPublisher endpoint
        P{{c}} Configurations related to SimpleTopicPublisher endpoint
    }
    public function init(SimpleTopicPublisherEndpointConfiguration c) {
        endpoint jms:SimpleTopicPublisher ep {
            initialContextFactory:"bmbInitialContextFactory",
            providerUrl:getConnectionUrl(c),
            connectionFactoryName:"ConnectionFactory",
            acknowledgementMode: c.acknowledgementMode,
            properties: c.properties,
            topicPattern: c.topicPattern
        };
        self.publisher = ep;
        self.producerActions = new TopicPublisherActions(ep);
        self.config = c;
    }

    documentation { Registers the endpoint in the service.
        This method is generally not used since SimpleTopicPublisher is a non-service endpoint.
        P{{serviceType}} type descriptor of the service
    }
    public function register(typedesc serviceType) {

    }

    documentation { Start simple topic pubilsher endpoint }
    public function start() {

    }

    documentation { Get simple topic publisher actions }
    public function getCallerActions() returns TopicPublisherActions {
        match (self.producerActions) {
            TopicPublisherActions s => return s;
            () => {
                error e = {message:"Topic publisher connector cannot be nil"};
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
        match (self.publisher) {
            jms:SimpleTopicPublisher s => {
                var result = s.createTextMessage(message);
                match (result) {
                    jms:Message m => return new Message(m);
                    error e => return e;
                }
            }
            () => {
                error e = {message:"topic publisher cannot be nil"};
                throw e;
            }
        }

    }
};

documentation { Caller action handler related to SimpleQueuePublisher endpoint }
public type TopicPublisherActions object {

    private jms:SimpleTopicPublisher publisher;

    new(publisher) {}

    documentation { Sends a message to Ballerina message broker
        P{{message}} message to be sent to Ballerina message broker
    }
    public function send(Message message) returns error? {
        endpoint jms:SimpleTopicPublisher publisherEP = self.publisher;
        var result = publisherEP->send(message.getJMSMessage());
        return result;
    }
};

documentation { Configurations related to SimpleQueueSender endpoint
    F{{username}} The caller's user name
    F{{password}} The caller's password
    F{{host}} Hostname of the broker node
    F{{port}} AMQP port of the broker node
    F{{clientID}} Identifier used to uniquely identify the client connection
    F{{virtualHost}} target virtualhost
    F{{acknowledgementMode}} specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
    "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
    F{{properties}} Additional properties use in initializing the initial context
    F{{topicPattern}} name of the target topic
}
public type SimpleTopicPublisherEndpointConfiguration record {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    ServiceSecureSocket? secureSocket,
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    map properties,
    string topicPattern,
};
