// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/artemis;

listener artemis:Listener artemisListener = new({host:"localhost", port:61616});

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue1"
    }
}
service artemisConsumer on artemisListener {
    resource function xyz(artemis:Message message, string data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue2"
    }
}
service artemisConsumer2 on artemisListener {
    resource function xyz(artemis:Message message, json data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue3"
    }
}
service artemisConsumer3 on artemisListener {
    resource function xyz(artemis:Message message, xml data) returns error? {
    }
}

type Person record {
    string name;
    int age;
};

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue4"
    }
}
service artemisConsumer4 on artemisListener {
    resource function xyz(artemis:Message message, Person data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue6"
    }
}
service artemisConsumer6 on artemisListener {
    resource function xyz(artemis:Message message, byte[] data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue7"
    }
}
service artemisConsumer7 on artemisListener {
    resource function xyz(artemis:Message message, map<string> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue8"
    }
}
service artemisConsumer8 on artemisListener {
    resource function xyz(artemis:Message message, map<int> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue9"
    }
}
service artemisConsumer9 on artemisListener {
    resource function xyz(artemis:Message message, map<float> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue10"
    }
}
service artemisConsumer10 on artemisListener {
    resource function xyz(artemis:Message message, map<byte> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue11"
    }
}
service artemisConsumer11 on artemisListener {
    resource function xyz(artemis:Message message, map<byte[]> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue12"
    }
}
service artemisConsumer12 on artemisListener {
    resource function xyz(artemis:Message message, map<boolean> data) returns error? {
    }
}

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue13"
    }
}
service artemisConsumer13 on artemisListener {
    resource function xyz(artemis:Message message) returns error? {
    }
}
