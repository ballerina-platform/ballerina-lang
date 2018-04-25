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

public type QueueSender object {

    public {
        QueueSenderActions producerActions;
        QueueSenderEndpointConfiguration config;
    }

    new() {
        self.producerActions = new;
    }

    public function init(QueueSenderEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => self.initQueueSender(s);
            () => {}
        }
    }

    public native function initQueueSender(Session session);

    public function register(typedesc serviceType) {

    }

    public function start() {

    }

    public function getCallerActions() returns QueueSenderActions {
        return self.producerActions;
    }

    public function stop() {

    }
};

public type QueueSenderEndpointConfiguration {
    Session? session;
    string queueName;
};

public type QueueSenderActions object {

    public native function send(Message m) returns error?;
};
