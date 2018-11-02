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

import ballerina/config;
import ballerina/io;
import ballerina/http;
import ballerina/websub;

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopic.com",
    hub: "http://websubpubhub.com"
}
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriber {
    onIntentVerification (endpoint caller, websub:IntentVerificationRequest verRequest) {
    }

    onNotification (websub:Notification notification) {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberTwo {
    onIntentVerification (http:Listener caller, websub:Notification notification) {
    }

    onNotification (websub:IntentVerificationRequest verRequest) {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberThree {
    onNotificationTwo (websub:IntentVerificationRequest verRequest) {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberFour {
    onIntentVerification (endpoint caller, websub:IntentVerificationRequest verRequest) {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberFive {
    onIntentVerification (endpoint caller) {
    }

    onNotification () {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberSix {
    onIntentVerification (websub:IntentVerificationRequest verRequest) {
    }

    onNotification (websub:Notification notification) {
    }
}

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopictwo.com",
    hub: "http://websubpubhubtwo.com"
}
service<websub:Service> websubSubscriberSeven {
    onIntentVerification (endpoint caller, websub:IntentVerificationRequest verRequest, string s) {
    }

    onNotification (endpoint caller, websub:Notification notification) {
    }
}
