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

import ballerina/http;
import ballerina/io;
import ballerina/websub;

@final string MOCK_HEADER = "MockHeader";

public type WebhookListenerConfiguration record {
    string host = "";
    int port = 0;
};

public type MockActionEvent record {
    string action;
};

public type MockDomainEvent record {
    string domain;
};

endpoint WebhookListenerForPayload listenerOneEP {
    port:8585
};

@websub:SubscriberServiceConfig {
    path:"/key"
}
service<WebhookServiceForPayload> keyWebhook bind listenerOneEP {
    onCreated(websub:Notification notification, MockActionEvent event) {
        io:println("Created Notification Received, action: ", event.action);
    }

    onFeature(websub:Notification notification, MockDomainEvent event) {
        io:println("Feature Notification Received, domain: ", event.domain);
    }

    onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

endpoint WebhookListenerForHeader listenerTwoEP {
    port:8686
};

@websub:SubscriberServiceConfig {
    path:"/header"
}
service<WebhookServiceForHeader> headerWebhook bind listenerTwoEP {
    onIssue(websub:Notification notification, MockActionEvent event) {
        io:println("Issue Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    onCommit(websub:Notification notification, MockActionEvent event) {
        io:println("Commit Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

endpoint WebhookListenerForHeaderAndPayload listenerThreeEP {
    port:8787
};

@websub:SubscriberServiceConfig {
    path:"/headerAndPayload"
}
service<WebhookServiceForHeaderAndPayload> headerAndPayloadWebhook bind listenerThreeEP {
    onIssueCreated(websub:Notification notification, MockActionEvent event) {
        io:println("Issue Created Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    onFeaturePull(websub:Notification notification, MockDomainEvent event) {
        io:println("Feature Pull Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " domain: ", event.domain);
    }

    onHeaderOnly(websub:Notification notification, MockActionEvent event) {
        io:println("HeaderOnly Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    onKeyOnly(websub:Notification notification, MockActionEvent event) {
        io:println("KeyOnly Notification Received, header value: ", notification.getHeader(MOCK_HEADER),
            " action: ", event.action);
    }

    onStatus(websub:Notification notification, MockActionEvent event) {
        // do nothing - test start up
    }
}

/////////////////// Specific Webhook for dispatching by key ///////////////////
public type WebhookServiceForPayload object {
    public function getEndpoint() returns (WebhookListenerForPayload) {
        WebhookListenerForPayload ep = new;
        return ep;
    }
};

public type WebhookListenerForPayload object {

    public WebhookListenerConfiguration webhookListenerConfig = {};

    private websub:Listener websubListener;

    public new() {
        self.websubListener = new;
    }

    public function init(WebhookListenerConfiguration config) {
        self.webhookListenerConfig = config;
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_PAYLOAD_KEY,
            payloadKeyResourceMap: {
                "action" : {
                    "created" : ("onCreated", MockActionEvent),
                    "deleted" : ("onDeleted", MockActionEvent),
                    "statuscheck" : ("onStatus", MockActionEvent)
                },
                "domain" : {
                    "issue" : ("onIssue", MockDomainEvent),
                    "feature" : ("onFeature", MockDomainEvent)
                }
            }
        };
        websub:SubscriberServiceEndpointConfiguration sseConfig = { host: config.host, port: config.port,
            extensionConfig: extensionConfig };
        self.websubListener.init(sseConfig);
    }

    public function register(typedesc serviceType) {
        self.websubListener.register(serviceType);
    }

    public function start() {
        self.websubListener.start();
    }

    public function getCallerActions() returns (http:Connection) {
        return self.websubListener.getCallerActions();
    }

    public function stop() {
        self.websubListener.stop();
    }
};

/////////////////// Specific Webhook for dispatching by header ///////////////////
public type WebhookServiceForHeader object {
    public function getEndpoint() returns (WebhookListenerForHeader) {
        WebhookListenerForHeader ep = new;
        return ep;
    }
};

public type WebhookListenerForHeader object {

    public WebhookListenerConfiguration webhookListenerConfig = {};

    private websub:Listener websubListener;

    public new() {
        self.websubListener = new;
    }

    public function init(WebhookListenerConfiguration config) {
        self.webhookListenerConfig = config;
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_HEADER,
            topicHeader: MOCK_HEADER,
            headerResourceMap: {
                "issue" : ("onIssue", MockActionEvent),
                "commit" : ("onCommit", MockActionEvent),
                "status" : ("onStatus", MockActionEvent)
            }
        };
        websub:SubscriberServiceEndpointConfiguration sseConfig = { host: config.host, port: config.port,
            extensionConfig: extensionConfig };
        self.websubListener.init(sseConfig);
    }

    public function register(typedesc serviceType) {
        self.websubListener.register(serviceType);
    }

    public function start() {
        self.websubListener.start();
    }

    public function getCallerActions() returns (http:Connection) {
        return self.websubListener.getCallerActions();
    }

    public function stop() {
        self.websubListener.stop();
    }
};

/////////////////// Specific Webhook for dispatching by header and payload ///////////////////
public type WebhookServiceForHeaderAndPayload object {
    public function getEndpoint() returns (WebhookListenerForHeaderAndPayload) {
        WebhookListenerForHeaderAndPayload ep = new;
        return ep;
    }
};

public type WebhookListenerForHeaderAndPayload object {

    public WebhookListenerConfiguration webhookListenerConfig = {};

    private websub:Listener websubListener;

    public new() {
        self.websubListener = new;
    }

    public function init(WebhookListenerConfiguration config) {
        self.webhookListenerConfig = config;
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_HEADER_AND_PAYLOAD,
            topicHeader: MOCK_HEADER,
            headerResourceMap: {
                "headeronly" : ("onHeaderOnly", MockActionEvent),
                "status" : ("onStatus", MockActionEvent )
            },
            payloadKeyResourceMap: {
                "action" : {
                    "keyonly" : ("onKeyOnly", MockActionEvent)
                },
                "domain" : {
                    "domainkeyonly" : ("onDomainKeyOnly", MockDomainEvent)
                }
            },
            headerAndPayloadKeyResourceMap: {
                "issue" : {
                    "action" : {
                        "created" : ("onIssueCreated", MockActionEvent),
                        "deleted" : ("onIssueDeleted", MockActionEvent)
                    }
                },
                "pull" : {
                    "domain" : {
                        "bugfix" : ("onBugFixPull", MockDomainEvent),
                        "feature" : ("onFeaturePull", MockDomainEvent)
                    }
                }
            }
        };
        websub:SubscriberServiceEndpointConfiguration sseConfig = { host: config.host, port: config.port,
            extensionConfig: extensionConfig };
        self.websubListener.init(sseConfig);
    }

    public function register(typedesc serviceType) {
        self.websubListener.register(serviceType);
    }

    public function start() {
        self.websubListener.start();
    }

    public function getCallerActions() returns (http:Connection) {
        return self.websubListener.getCallerActions();
    }

    public function stop() {
        self.websubListener.stop();
    }
};
