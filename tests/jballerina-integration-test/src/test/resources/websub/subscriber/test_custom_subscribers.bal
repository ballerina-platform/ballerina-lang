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

import ballerina/io;
import ballerina/lang.'object as lang;
import ballerina/websub;

const string MOCK_HEADER = "MockHeader";

public type WebhookListenerConfiguration record {
    string host = "";
};

public type MockActionEvent record {|
    string action;
|};

public type MockDomainEvent record {|
    string domain;
|};

@websub:SubscriberServiceConfig {
    path:"/key"
}
service keyWebhook on new WebhookServerForPayload(23585) {
    resource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest verRequest) {
        io:println("Intent verification request received");
        checkpanic caller->accepted();
    }

    resource function onCreated(websub:Notification notification, MockActionEvent event) {
        io:println("Created Notification Received, action: ", event.action);
    }

    resource function onFeature(websub:Notification notification, MockDomainEvent event) {
        io:println("Feature Notification Received, domain: ", event.domain);
    }

    resource function onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

@websub:SubscriberServiceConfig {
    path:"/header"
}
service headerWebhook on new WebhookServerForHeader(23686) {
    resource function onIssue(websub:Notification notification, MockActionEvent event) {
        io:println("Issue Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    resource function onCommit(websub:Notification notification, MockActionEvent event) {
        io:println("Commit Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    resource function onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

@websub:SubscriberServiceConfig {
    path:"/headerAndPayload"
}
service headerAndPayloadWebhook on new WebhookServerForHeaderAndPayload(23787) {
    resource function onIssueCreated(websub:Notification notification, MockActionEvent event) {
        io:println("Issue Created Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    resource function onFeaturePull(websub:Notification notification, MockDomainEvent event) {
        io:println("Feature Pull Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " domain: ", event.domain);
    }

    resource function onHeaderOnly(websub:Notification notification, MockActionEvent event) {
        io:println("HeaderOnly Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    resource function onKeyOnly(websub:Notification notification, MockActionEvent event) {
        io:println("KeyOnly Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    resource function onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

/////////////////// Specific Webhook for dispatching by key ///////////////////
public type WebhookServerForPayload object {

    *lang:Listener;

    private websub:Listener websubListener;

    public function init(int port, WebhookListenerConfiguration? config = ()) {
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
        string host = config is () ? "" : config.host;
        websub:SubscriberListenerConfiguration sseConfig = {
            host: host,
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
        return ();
    }

    public function __immediateStop() returns error? {
        return self.websubListener.__immediateStop();
    }
};

/////////////////// Specific Webhook for dispatching by header ///////////////////
public type WebhookServerForHeader object {

    *lang:Listener;

    private websub:Listener websubListener;

    public function init(int port, WebhookListenerConfiguration? config = ()) {
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_HEADER,
            topicHeader: MOCK_HEADER,
            headerResourceMap: {
                "issue" : ["onIssue", MockActionEvent],
                "commit" : ["onCommit", MockActionEvent],
                "status" : ["onStatus", MockActionEvent]
            }
        };
        string host = config is () ? "" : config.host;
        websub:SubscriberListenerConfiguration sseConfig = {
            host: host,
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
        return ();
    }

    public function __immediateStop() returns error? {
        return self.websubListener.__immediateStop();
    }
};

/////////////////// Specific Webhook for dispatching by header and payload ///////////////////
public type WebhookServerForHeaderAndPayload object {

    *lang:Listener;

    private websub:Listener websubListener;

    public function init(int port, WebhookListenerConfiguration? config = ()) {
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_HEADER_AND_PAYLOAD,
            topicHeader: MOCK_HEADER,
            headerResourceMap: {
                "headeronly" : ["onHeaderOnly", MockActionEvent],
                "status" : ["onStatus", MockActionEvent]
            },
            payloadKeyResourceMap: {
                "action" : {
                    "keyonly" : ["onKeyOnly", MockActionEvent]
                },
                "domain" : {
                    "domainkeyonly" : ["onDomainKeyOnly", MockDomainEvent]
                }
            },
            headerAndPayloadKeyResourceMap: {
                "issue" : {
                    "action" : {
                        "created" : ["onIssueCreated", MockActionEvent],
                        "deleted" : ["onIssueDeleted", MockActionEvent]
                    }
                },
                "pull" : {
                    "domain" : {
                        "bugfix" : ["onBugFixPull", MockDomainEvent],
                        "feature" : ["onFeaturePull", MockDomainEvent]
                    }
                }
            }
        };
        string host = config is () ? "" : config.host;
        websub:SubscriberListenerConfiguration sseConfig = {
            host: host,
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
        return ();
    }

    public function __immediateStop() returns error? {
        return self.websubListener.__immediateStop();
    }
};
