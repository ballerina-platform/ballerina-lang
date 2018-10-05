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

public type Consumer object {

    public function getEndpoint() returns ConsumerTemplate {
        ConsumerTemplate ct = new();
        return ct;
    }
};

public type ConsumerTemplate object {
    public ConsumerActions callerActions;
    public ConsumerEndpointConfiguration config;

    public function init(ConsumerEndpointConfiguration c) {

    }

    public function register(typedesc serviceType) {

    }

    public function start() {

    }

    public function stop() {

    }

    public function getCallerActions() returns ConsumerActions {
        return new;
    }

};

public type ConsumerActions object {

    public function acknowledge(Message message) returns error? {
        return;
    }
};

public type ConsumerEndpointConfiguration record {
    jms:Session? session;
    string identifier;
};
