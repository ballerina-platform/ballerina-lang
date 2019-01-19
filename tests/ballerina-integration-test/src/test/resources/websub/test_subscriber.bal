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
import ballerina/mime;
import ballerina/http;
import ballerina/websub;
import ballerina/crypto;

listener websub:Listener websubEP = new websub:Listener(8181, config = { host: "0.0.0.0" });

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    topic: "http://one.websub.topic.com",
    hub: config:getAsString("test.hub.url")
}
service websubSubscriber on websubEP {
    resource function onNotification (websub:Notification notification) {
        var payload = notification.getJsonPayload();
        if (payload is json) {
            io:println("WebSub Notification Received: " + payload.toString());
        } else {
            panic payload;
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    topic: "http://one.websub.topic.com",
    hub: config:getAsString("test.hub.url"),
    leaseSeconds: 3650,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriberTwo on websubEP {
    resource function onIntentVerification (websub:Caller caller, websub:IntentVerificationRequest request) {
        http:Response response = request.buildSubscriptionVerificationResponse("http://one.websub.topic.com");
        if (response.statusCode == 202) {
            io:println("Intent verified explicitly for subscription change request");
        } else {
            io:println("Intent verification denied explicitly for subscription change request");
        }
        _ = caller->respond(<http:Response>crypto:unsafeMarkUntainted(response));
    }

    resource function onNotification (websub:Notification notification) {
        var payload = notification.getPayloadAsString();
        if (payload is string) {
            io:println("WebSub Notification Received by Two: " + payload);
        } else {
            panic payload;
        }
    }
}

