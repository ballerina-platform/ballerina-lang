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
import ballerina/'lang\.object as lang;

# The Queue Receiver endpoint.
#
# + consumerActions - Handles all the caller actions related to the QueueListener.
# + session - Session of the queue receiver.
# + messageSelector - The message selector for the queue receiver.
# + identifier - Unique identifier for the reciever.
public type QueueListener object {

    *lang:AbstractListener;

    public QueueReceiverCaller consumerActions = new;
    public Session session;
    public string messageSelector = "";
    public string identifier = "";

    # Initializes the QueueListener.
    #
    # + c - The JMS Session object or configurations related to the receiver.
    # + queueName - Name of the queue.
    # + messageSelector - The message selector for the queue.
    # + identifier - The unique identifier for the receiver.
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

    # Binds the queue receiver endpoint to a service.
    #
    # + s - The service instance.
    # + name - Name of the service.
    # + return - Returns nil or an error upon failure to register the listener.
    public function __attach(service s, string? name = ()) returns error? {
        return self.registerListener(s);
    }

    function registerListener(service serviceType) returns error? = external;

    function createQueueReceiver(Session session, string messageSelector, string|Destination dest) = external;

    # Starts the endpoint.
    #
    # + return - Returns nil or an error upon failure to start.
    public function __start() returns error? {
        return self.start();
    }

    private function start() returns error? = external;

    # Retrieves the QueueReceiver.
    #
    # + return - the QueueReceiver
    public function getCallerActions() returns QueueReceiverCaller {
        return self.consumerActions;
    }

    # Stops consuming messages through the QueueListener.
    #
    # + return - Returns nil or an error upon failure to close the queue receiver.
    public function __stop() returns error? {
        self.closeQueueReceiver(self.consumerActions);
        return ();
    }

    function closeQueueReceiver(QueueReceiverCaller actions) = external;
};

# The Caller actions related to queue receiver endpoint.
#
# + queueListener - the QueueListener
public type QueueReceiverCaller client object {

    public QueueListener? queueListener = ();

    # Synchronously receives a message from the JMS provider.
    #
    # + timeoutInMilliSeconds - Time to wait until a message is received.
    # + return - Returns a message or nil if the timeout exceeds, or returns an error upon an internal error of the JMS
    #             provider.
    public remote function receive(int timeoutInMilliSeconds = 0) returns Message|error? = external;

    # Synchronously receives a message from a given destination.
    #
    # + destination - Destination to subscribe to.
    # + timeoutInMilliSeconds - Time to wait until a message is received.
    # + return - Returns a message or () if the timeout exceeds, or returns an error upon an internal error of the JMS
    #             provider.
    public remote function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?
    {
        var queueListener = self.queueListener;
        if (queueListener is QueueListener) {
            var session = queueListener.session;
            validateQueue(destination);
            queueListener.createQueueReceiver(session, queueListener.messageSelector, destination);
        } else {
            log:printInfo("Message receiver is not properly initialized for queue " + destination.getName());
        }
        var result = self->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        if (queueListener is QueueListener) {
            queueListener.closeQueueReceiver(self);
        } else {
            log:printInfo("Could not close the queue receiver");
        }
        return result;
    }
};

function validateQueue(Destination destination) {
    if (destination.getName() == "") {
        string errorMessage = "Destination name cannot be empty";
        error queueReceiverConfigError = error(JMS_ERROR_CODE, message = errorMessage);
        panic queueReceiverConfigError;
    } else if (destination.getType() != "queue") {
        string errorMessage = "Destination should should be a queue";
        error queueReceiverConfigError = error(JMS_ERROR_CODE, message = errorMessage);
        panic queueReceiverConfigError;
    }
}
