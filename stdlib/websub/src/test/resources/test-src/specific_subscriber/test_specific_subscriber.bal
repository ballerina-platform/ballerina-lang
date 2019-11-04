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

import ballerina/lang.'object as lang;
import ballerina/websub;

public type MockActionEvent record {|
    string action;
|};

public type MockDomainEvent record {|
    string domain;
|};

public type WebhookServerForPayload object {

    *lang:Listener;

    private websub:Listener websubListener;

    public function __init(int port, string? host = ()) {
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_PAYLOAD_KEY,
            payloadKeyResourceMap: {
                "action" : {
                    "created" : ["onCreated", MockActionEvent],
                    "deleted" : ["onDeleted", MockActionEvent],
                    "statuscheck" : ["onStatus", MockActionEvent]
                },
                "domain" : {
                    "issue" : ["onIssue", MockDomainEvent],
                    "feature" : ["onFeature", MockDomainEvent]
                }
            }
        };
        websub:SubscriberListenerConfiguration sseConfig = {
            host: host ?: "",
            extensionConfig: extensionConfig
        };
        self.websubListener = new(port, sseConfig);
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.websubListener.__attach(s, name);
    }

    public function __detach(service s) returns error? {
        return self.websubListener.__detach(s);
    }

    public function __start() returns error? {
        return self.websubListener.__start();
    }

    public function __gracefulStop() returns error? {
        return self.websubListener.__gracefulStop();
    }

    public function __immediateStop() returns error? {
        return self.websubListener.__immediateStop();
    }
};

service keyWebhook1 =
@websub:SubscriberServiceConfig {
    path:"/key"
}
@websub:SpecificSubscriber
service {
    resource function onOpened(websub:Notification notification, MockActionEvent event) {
    }

    resource function onFeature(websub:Notification notification, MockDomainEvent event) {
    }

    resource function onStatus(websub:Notification notification, MockActionEvent event) {
    }

    resource function onReopened(websub:Notification notification, MockActionEvent event) {
    }
};

service keyWebhook2 =
@websub:SubscriberServiceConfig {
    path:"/key"
}
@websub:SpecificSubscriber
service {
    resource function onCreated(websub:Notification notification, MockActionEvent event) {
    }

    resource function onFeature(websub:Notification notification, MockDomainEvent event) {
    }

    resource function onStatus(websub:Notification notification, MockDomainEvent event) {
    }
};

public function testInvalidResourceFunctions() {
    WebhookServerForPayload l = new(8081);
    checkpanic l.__attach(keyWebhook1);
}

public function testInvalidParam() {
    WebhookServerForPayload l = new(8081);
    checkpanic l.__attach(keyWebhook2);
}
