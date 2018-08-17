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

documentation { JMS QueueSender Endpoint
    E{{}}
    F{{producerActions}} Handle all the actions related to the endpoint
    F{{config}} Used to store configurations related to a JMS Queue sender
}
public type QueueSender object {

    public QueueSenderActions producerActions;
    public QueueSenderEndpointConfiguration config;

    documentation { Default constructor of the endpoint }
    public new() {
        self.producerActions = new;
    }

    documentation { Initialize the consumer endpoint
        P{{c}} Configurations related to the QueueSender endpoint
    }
    public function init(QueueSenderEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => self.initQueueSender(s);
            () => {}
        }
    }

    extern function initQueueSender(Session session);

    documentation { Registers the endpoint in the service.
        This method is not used since QueueSender is a non-service endpoint.
        P{{serviceType}} type descriptor of the service
    }
    public function register(typedesc serviceType) {

    }

    documentation { Starts the consumer endpoint }
    public function start() {

    }

    documentation { Returns the caller action object of the QueueSender }
    public function getCallerActions() returns QueueSenderActions {
        return self.producerActions;
    }

    documentation { Stops the consumer endpoint }
    public function stop() {

    }
};

documentation { Configurations related to a QueueSender object
    F{{session}} JMS session object used to create the consumer
    F{{queueName}} name of the target queue
}
public type QueueSenderEndpointConfiguration record {
    Session? session;
    string queueName;
};

documentation { JMS QueueSender action handling object }
public type QueueSenderActions object {

    documentation { Sends a message to the JMS provider
        P{{message}} message to be sent to the JMS provider
    }
    public extern function send(Message message) returns error?;
};
