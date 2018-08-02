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

documentation { Queue Receiver endpoint
    E{{}}
    F{{consumerActions}} handles all the caller actions related to the queue receiver endpoint
    F{{config}} configurations related to the QueueReceiver
}
public type QueueReceiver object {

    public QueueReceiverActions consumerActions;
    public QueueReceiverEndpointConfiguration config;

    documentation { Initializes the QueueReceiver endpoint
        P{{c}} Configurations related to the QueueReceiver endpoint
    }
    public function init(QueueReceiverEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => {
                self.createQueueReceiver(s, c.messageSelector);
                log:printInfo("Message receiver created for queue " + c.queueName);
            }
            () => { log:printInfo("Message receiver not properly initialised for queue " + c.queueName); }
        }

    }

    documentation { Binds the queue receiver endpoint to a service
        P{{serviceType}} type descriptor of the service to bind to
    }
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    extern function registerListener(typedesc serviceType, QueueReceiverActions actions);

    extern function createQueueReceiver(Session session, string messageSelector);

    documentation { Starts the endpoint. Function is ignored by the receiver endpoint }
    public function start() {
        // Ignore
    }

    documentation { Retrieves the QueueReceiver consumer action handler
        R{{}} queue receiver action handler
    }
    public function getCallerActions() returns QueueReceiverActions {
        return consumerActions;
    }

    documentation { Stops consuming messages through QueueReceiver endpoint}
    public function stop() {
        self.closeQueueReceiver(consumerActions);
    }

    extern function closeQueueReceiver(QueueReceiverActions actions);
};

documentation { Configurations related to the QueueReceiver endpoint
    F{{session}} JMS session object
    F{{queueName}} Name of the queue
    F{{messageSelector}} JMS selector statement
    F{{identifier}} unique identifier for the subscription
}
public type QueueReceiverEndpointConfiguration record {
    Session? session;
    string queueName;
    string messageSelector;
    string identifier;
};

documentation { Caller actions related to queue receiver endpoint }
public type QueueReceiverActions object {

    documentation { Acknowledges a received message
        P{{message}} JMS message to be acknowledged
    }
    public extern function acknowledge(Message message) returns error?;

    documentation { Synchronously receive a message from the JMS provider
        P{{timeoutInMilliSeconds}} time to wait until a message is received
        R{{}} Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    }
    public extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
