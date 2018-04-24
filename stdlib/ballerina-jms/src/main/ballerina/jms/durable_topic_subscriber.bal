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

public type DurableTopicSubscriber object {

    public {
        DurableTopicSubscriberActions consumerActions;
        DurableTopicSubscriberEndpointConfiguration config;
    }

    public function init(DurableTopicSubscriberEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => {
                self.createSubscriber(s, config.messageSelector);
                log:printInfo("Durable subscriber created for topic " + config.topicPattern);
            }
            () => {}
        }
    }

    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    native function registerListener(typedesc serviceType, DurableTopicSubscriberActions consumerActions);

    native function createSubscriber(Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions() returns DurableTopicSubscriberActions {
        return consumerActions;
    }

    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    native function closeSubscriber(DurableTopicSubscriberActions consumerActions);
};

public type DurableTopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;

};

public type DurableTopicSubscriberActions object {

    public native function acknowledge(Message message) returns error?;

    public native function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;
};
