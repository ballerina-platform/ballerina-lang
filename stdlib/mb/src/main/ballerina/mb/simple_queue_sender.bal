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

documentation { Simplified queue sender endpoint.
    A new connection and a session will be create when this endpoint is initialize.

    E{{}}
    F{{config}} configurations related to the SimpleQueueSender endpoint
}
public type SimpleQueueSender object {

    public SimpleQueueSenderEndpointConfiguration config;

    private jms:SimpleQueueSender? sender;
    private QueueSenderActions? producerActions;

    documentation { Initialize the SimpleQueueSender endpoint
        P{{c}} Configurations related to the SimpleQueueSender endpoint
    }
    public function init(SimpleQueueSenderEndpointConfiguration c) {
        endpoint jms:SimpleQueueSender queueSender {
            initialContextFactory:"bmbInitialContextFactory",
            providerUrl:getConnectionUrl(c),
            connectionFactoryName:"ConnectionFactory",
            acknowledgementMode: c.acknowledgementMode,
            properties: c.properties,
            queueName: c.queueName
        };
        self.sender = queueSender;
        self.producerActions = new QueueSenderActions(queueSender);
        self.config = c;
    }

    documentation { Registers the endpoint in the service.
        This method is not used since SimpleQueueSender is a non-service endpoint.
        P{{serviceType}} type descriptor of the service
    }
    public function register(typedesc serviceType) {

    }

    documentation { Starts the SimpleQueueSender endpoint }
    public function start() {

    }

    documentation { Returns the caller action object of the SimpleQueueSender }
    public function getCallerActions() returns QueueSenderActions {
        match (self.producerActions) {
            QueueSenderActions s => return s;
            () => {
                error e = {message:"Queue sender connector cannot be nil"};
                throw e;
            }
        }
    }

    documentation { Stops the  SimpleQueueSender endpoint }
    public function stop() {

    }

    documentation { Creates a message which holds text content
        P{{content}} the text content used to initialize this message
    }
    public function createTextMessage(string content) returns Message|error {
        match (self.sender) {
            jms:SimpleQueueSender s => {
                var result = s.createTextMessage(content);
                match (result) {
                    jms:Message m => return new Message(m);
                    error e => return e;
                }
            }
            () => {
                error e = {message:"Session cannot be nil"};
                throw e;
            }
        }
    }
};

documentation { Caller action handler related to SimpleQueueSender endpoint }
public type QueueSenderActions object {

    private jms:SimpleQueueSender sender;

    new(sender) {}

    documentation { Sends a message to Ballerina message broker
        P{{message}} message to be sent to Ballerina message broker
    }
    public function send(Message message) returns error? {
        endpoint jms:SimpleQueueSender senderEP = self.sender;
        var result = senderEP->send(message.getJMSMessage());
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
    F{{queueName}} Name of the target queue
}
public type SimpleQueueSenderEndpointConfiguration record {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    ServiceSecureSocket? secureSocket,
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    map properties,
    string queueName,
};
