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

documentation { Simplified queue receiver endpoint.
    A new connection and a session will be create when this endpoint is initialize.

    E{{}}
    F{{config}} configurations related to the SimpleQueueReceiver endpoint
}
public type SimpleQueueReceiver object {

    public SimpleQueueListenerEndpointConfiguration config;

    private jms:SimpleQueueReceiver receiver;
    private QueueReceiverActions? consumerActions;

    documentation { Initialize the SimpleQueueReceiver endpoint
        P{{c}} Configurations related to the SimpleQueueReceiver endpoint
    }
    public function init(SimpleQueueListenerEndpointConfiguration c) {
        self.config = c;
        self.receiver.init({
                initialContextFactory:"bmbInitialContextFactory",
                providerUrl:getConnectionUrl(c),
                connectionFactoryName: c.connectionFactoryName,
                acknowledgementMode: c.acknowledgementMode,
                messageSelector: c.messageSelector,
                properties: c.properties,
                queueName: c.queueName
            });

        self.consumerActions = new QueueReceiverActions(self.receiver.getCallerActions());
    }

    documentation { Binds the SimlpeQueueReceiver endpoint to a service
        P{{serviceType}} type descriptor of the service to bind to
    }
    public function register(typedesc serviceType) {
        self.receiver.register(serviceType);
    }

    documentation { Starts the endpoint. Function is ignored by the receiver endpoint }
    public function start() {
        self.receiver.start();
    }

    documentation { Retrieves the SimpleQueueReceiver consumer action handler
        R{{}} simple queue receiver action handler
    }
    public function getCallerActions() returns QueueReceiverActions {
        match (self.consumerActions) {
            QueueReceiverActions c => return c;
            () => {
                error e = {message:"Queue receiver consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    documentation { Stops consuming messages through QueueReceiver endpoint }
    public function stop() {
        receiver.stop();
    }

    documentation { Creates a message which holds text content
        P{{content}} the text content used to initialize this message
    }
    public function createTextMessage(string content) returns Message|error {
        var result = self.receiver.createTextMessage(content);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
        }
    }
};

documentation { Configurations related to SimpleQueueReceiver endpoint
    F{{username}} The caller's user name
    F{{password}} The caller's password
    F{{host}} Hostname of the broker node
    F{{port}} AMQP port of the broker node
    F{{clientID}} Identifier used to uniquely identify the client connection
    F{{virtualHost}} target virtualhost
    F{{acknowledgementMode}} specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
    "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
    F{{messageSelector}} JMS selector statement
    F{{properties}} Additional properties use in initializing the initial context
    F{{queueName}} Name of the target queue

}
public type SimpleQueueListenerEndpointConfiguration record {
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
    string queueName,
};

documentation { Caller action handler related to SimpleQueueReceiver endpoint }
public type QueueReceiverActions object {

    public jms:QueueReceiverActions helper;

    public new(helper) {}

    documentation { Acknowledges a received message
        P{{message}} message to be acknowledged
    }
    public function acknowledge(Message message) returns error? {
        return helper.acknowledge(message.getJMSMessage());
    }

    documentation { Synchronously receive a message from Ballerina message broker
        P{{timeoutInMilliSeconds}} time to wait until a message is received
        R{{}} Returns a message or nill if the timeout exceededs. Returns an error on broker internal error.
    }
    public function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        var result = helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }
};
