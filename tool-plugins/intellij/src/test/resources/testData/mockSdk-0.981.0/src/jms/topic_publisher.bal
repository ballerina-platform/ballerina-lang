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

documentation { JMS topic publisher
    E{{}}
    F{{producerActions}} Topic publisher endpoint actions
    F{{config}} Topic publisher endpoint configuration
}
public type TopicPublisher object {
    public TopicPublisherActions producerActions;
    public TopicPublisherEndpointConfiguration config;

    documentation { Topic publisher contructor }
    new() {
        self.producerActions = new;
    }

    documentation { Initialize topic publisher endpoint
        P{{c}} Topic publisher endpoint configuration
    }
    public function init(TopicPublisherEndpointConfiguration c) {
        self.config = c;
        match (c.session) {
            Session s => self.initTopicPublisher(s);
            () => {}
        }
    }

    public extern function initTopicPublisher(Session session);

    documentation { Register topic publisher endpoint
        P{{serviceType}} Type descriptor of the service
    }
    public function register(typedesc serviceType) {

    }

    documentation { Start topic publisher endpoint }
    public function start() {

    }

    documentation { Get topic publisher actions }
    public function getCallerActions() returns TopicPublisherActions {
        return self.producerActions;
    }

    documentation { Stop topic publisher endpoint }
    public function stop() {

    }
};

documentation { Configuration related to the topic publisher endpoint
    F{{session}} Session object used to create topic publisher
    F{{topicPattern}} Topic name pattern
}
public type TopicPublisherEndpointConfiguration record {
    Session? session;
    string topicPattern;
};

documentation { Actions that topic publisher endpoint could perform }
public type TopicPublisherActions object {

    documentation { Sends a message to the JMS provider
        P{{message}} Message to be sent to the JMS provider
    }
    public extern function send(Message message) returns error?;
};
