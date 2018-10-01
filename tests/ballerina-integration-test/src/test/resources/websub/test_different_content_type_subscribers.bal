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
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8282
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    hub: "https://localhost:9191/websub/hub",
    topic: "http://one.websub.topic.com",
    leaseSeconds: 3000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            string payload = check notification.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriber: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            xml payload = check notification.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriber: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            json payload = check notification.getJsonPayload();
            io:println("JSON WebSub Notification Received by websubSubscriber: ", payload);
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    hub: "https://localhost:9191/websub/hub",
    topic: "http://one.websub.topic.com",
    leaseSeconds: 1000
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            string payload = check notification.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriberTwo: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            xml payload = check notification.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriberTwo: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            json payload = check notification.getJsonPayload();
            io:println("JSON WebSub Notification Received by websubSubscriberTwo: ", payload);
        }
    }
}
