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
import ballerina/mime;
import ballerina/websub;

listener websub:Listener websubEP = new websub:Listener(23282);

@websub:SubscriberServiceConfig {
    path:"/websub",
    target: ["http://localhost:23191/websub/hub", "http://one.websub.topic.com"],
    leaseSeconds: 3000,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber on websubEP {
    resource function onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            var payload = notification.getTextPayload();
            if (payload is string) {
                io:println("Text WebSub Notification Received by websubSubscriber: ", payload);
            } else {
                panic payload;
            }
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            var payload = notification.getXmlPayload();
            if (payload is xml) {
                io:println("XML WebSub Notification Received by websubSubscriber: ", payload);
            } else {
                panic payload;
            }
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            var payload = notification.getJsonPayload();
            if (payload is json) {
                io:println("JSON WebSub Notification Received by websubSubscriber: ", payload.toJsonString());
            } else {
                panic payload;
            }
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    target: ["http://localhost:23191/websub/hub", "http://one.websub.topic.com"],
    leaseSeconds: 1000
}
service websubSubscriberTwo on websubEP {
    resource function onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            var payload = notification.getTextPayload();
            if (payload is string) {
                io:println("Text WebSub Notification Received by websubSubscriberTwo: ", payload);
            } else {
                panic payload;
            }
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            var payload = notification.getXmlPayload();
            if (payload is xml) {
                io:println("XML WebSub Notification Received by websubSubscriberTwo: ", payload);
            } else {
                panic payload;
            }
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            var payload = notification.getJsonPayload();
            if (payload is json) {
                io:println("JSON WebSub Notification Received by websubSubscriberTwo: ", payload.toJsonString());
            } else {
                panic payload;
            }
        }
    }
}
