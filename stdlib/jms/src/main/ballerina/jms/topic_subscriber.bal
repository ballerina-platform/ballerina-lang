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

documentation { JMS topic subscriber
    E{{}}
    F{{consumerActions}} Topic subscriber endpoint actions
    F{{config}} Topic subscriber endpoint configuration
}
public type TopicSubscriber object {

    public TopicSubscriberActions consumerActions;
    public TopicSubscriberEndpointConfiguration config;

    documentation { Initialize topic subscriber endpoint
        P{{c}} Topic subscriber configuration
    }
    public function init(TopicSubscriberEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => {
                self.createSubscriber(s, c.messageSelector);
                log:printInfo("Subscriber created for topic " + c.topicPattern);
            }
            () => {}
        }
    }

    documentation { Register topic subscriber endpoint
        P{{serviceType}} Type descriptor of the service
    }
    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    extern function registerListener(typedesc serviceType, TopicSubscriberActions actions);

    extern function createSubscriber(Session session, string messageSelector);

    documentation { Start topic subscriber endpoint }
    public function start() {

    }

    documentation { Get topic subscriber actions }
    public function getCallerActions() returns TopicSubscriberActions {
        return consumerActions;
    }

    documentation { Stop topic subscriber endpoint }
    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    extern function closeSubscriber(TopicSubscriberActions actions);
};

documentation { Configuration related to topic subscriber endpoint
    F{{session}} Session object used to create topic subscriber
    F{{topicPattern}} Topic name pattern
    F{{messageSelector}} Message selector condition to filter messages
    F{{identifier}} Identifier of topic subscriber endpoint
}
public type TopicSubscriberEndpointConfiguration record {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;
};

documentation { Actions that topic subscriber endpoint could perform }
public type TopicSubscriberActions object {

    documentation { Acknowledges a received message
        P{{message}} JMS message to be acknowledged
    }
    public extern function acknowledge(Message message) returns error?;

    documentation { Synchronously receive a message from the JMS provider
        P{{timeoutInMilliSeconds}} Time to wait until a message is received
        R{{}} Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    }
    public extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
