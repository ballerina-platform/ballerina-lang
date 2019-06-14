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

# Queue Receiver endpoint
#
# + consumerActions - handles all the caller actions related to the QueueListener
# + session - Session of the queue receiver
# + messageSelector - The message selector for the queue receiver
# + identifier - Unique identifier for the reciever
public type QueueListener object {

    *AbstractListener;

    public QueueReceiverCaller consumerActions = new;
    public Session session;
    public string messageSelector = "";
    public string identifier = "";

    # Initializes the QueueListener
    #
    # + c - The JMS Session object or Configurations related to the receiver
    # + queueName - Name of the queue
    # + messageSelector - The message selector for the queue
    # + identifier - Unique identifier for the receiver
    public function __init(Session|ReceiverEndpointConfiguration c, string? queueName = (), string messageSelector = "",
                           string identifier = "") {
        self.consumerActions.queueListener = self;
        if (c is Session) {
            self.session = c;
        } else {
            Connection conn = new({
                    initialContextFactory: c.initialContextFactory,
                    providerUrl: c.providerUrl,
                    connectionFactoryName: c.connectionFactoryName,
                    properties: c.properties
                });
            self.session = new Session(conn, {
                    acknowledgementMode: c.acknowledgementMode
                });
        }
        if (queueName is string) {
            self.createQueueReceiver(self.session, messageSelector, queueName);
            log:printInfo("Message receiver created for queue " + queueName);
        }
    }

    # Binds the queue receiver endpoint to a service
    #
    # + serviceType - The service instance
    # + name - Name of the service
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, string? name = ()) returns error? {
        return self.registerListener(serviceType, self.consumerActions, name);
    }

    function registerListener(service serviceType, QueueReceiverCaller actions, string? name) returns error? = external;

    function createQueueReceiver(Session? session, string messageSelector, string|Destination dest) = external;

    # Starts the endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
        return self.start();
    }

    private function start() returns error? = external;

    # Retrieves the QueueReceiver
    #
    # + return - the QueueReceiver
    public function getCallerActions() returns QueueReceiverCaller {
        return self.consumerActions;
    }

    # Stops consuming messages through QueueListener
    #
    # + return - Nil or error upon failure to close queue receiver
    public function __stop() returns error? {
        self.closeQueueReceiver(self.consumerActions);
        return ();
    }

    function closeQueueReceiver(QueueReceiverCaller actions) = external;
};

# Caller actions related to queue receiver endpoint.
#
# + queueListener - the QueueListener
public type QueueReceiverCaller client object {

    public QueueListener? queueListener = ();

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public remote function acknowledge(Message message) returns error? = external;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds, returns an error on JMS provider internal error
    public remote function receive(int timeoutInMilliSeconds = 0) returns Message|error? = external;

    # Synchronously receive a message from a given destination
    #
    # + destination - destination to subscribe to
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or () if the timeout exceeds, returns an error on JMS provider internal error
    public remote function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?
    {
        var queueListener = self.queueListener;
        if (queueListener is QueueListener) {
            var session = queueListener.session;
            validateQueue(destination);
            queueListener.createQueueReceiver(session, queueListener.messageSelector, destination);
        } else {
            log:printInfo("Message receiver is not properly initialized for queue " + destination.destinationName);
        }
        var result = self->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        self.queueListener.closeQueueReceiver(self);
        return result;
    }
};

function validateQueue(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        map<anydata> errorDetail = {
            message: errorMessage
        };
        error queueReceiverConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic queueReceiverConfigError;
    } else if (destination.destinationType != "queue") {
        string errorMessage = "Destination should should be a queue";
        map<anydata> errorDetail = {
            message: errorMessage
        };
        error queueReceiverConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic queueReceiverConfigError;
    }
}
