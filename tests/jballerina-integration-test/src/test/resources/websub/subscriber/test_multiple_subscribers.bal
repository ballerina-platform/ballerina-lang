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
import ballerina/websub;

listener websub:Listener websubEP = new websub:Listener(23383);

// Following listner has no services attached with and it should not fail the listener start
listener websub:Listener websubEndPointWithNoAttachedServices = new websub:Listener(23384);

@websub:SubscriberServiceConfig {
    path:"/websub",
    target: ["http://localhost:23191/websub/hub", "http://three.websub.topic.com"],
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber on websubEP {
    resource function onNotification (websub:Notification notification) {
        var payload = notification.getJsonPayload();
        if (payload is json) {
            io:println("WebSub Notification Received by One: " + payload.toJsonString());
        } else {
            panic payload;
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    target: "http://localhost:23080/publisherTwo/discover",
    leaseSeconds: 1200,
    secret: "SwklSSf42DLA"
}
service websubSubscriberTwo on websubEP {
    resource function onNotification (websub:Notification notification) {
        var payload = notification.getJsonPayload();
        if (payload is json) {
            io:println("WebSub Notification Received by Two: " + payload.toJsonString());
        } else {
            panic payload;
        }
    }
}
